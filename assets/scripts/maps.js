var map;

class DroneMap {

    static initialize() {
        DroneMap.locked = true;
        DroneMap.selectedIsCurrent = false;

        DroneMap.map = map = new google.maps.Map(document.getElementById("map-canvas"), {
            zoom: 16,
            center: new google.maps.LatLng(MAP_START_POSITION[0], MAP_START_POSITION[1]),
            mapTypeId: google.maps.MapTypeId.HYBRID
        });

        DroneMap.MARKERS = {
            "DRONE": new google.maps.Marker({
                //position: latlng,
                map: DroneMap.map,
                icon: {
                    url: "./assets/images/drone-marker.png",
                    scaledSize: new google.maps.Size(50, 50),
                    origin: new google.maps.Point(0, 0),
                    anchor: new google.maps.Point(25, 50)
                }
            })
        };

        DroneMap.DIVS = {
            "LOCK_TOGGLE_ICON": document.getElementById("map-lock-toggle-icon"),
            "SETTINGS_SECTION": document.getElementById("settings-section-maps")
        }

        DroneMap.COOKIES = {
            "LOCKED_STATE": {
                name: "map-locked-state",
                default: true
            }
        }

        DroneMap.ICONS = {
            "LOCK": "fas fa-lock",
            "UNLOCK": "fas fa-unlock",
        }

        DroneMap.SETTINGS_KEY = {
            "GOOGLE_MAPS_API": "googlemaps.api.key",
            "MAP_VIEW_TYPE": "map.view-type"
        }

        DroneMap.prepareSettingsSection();
        DroneMap.initializeFlighPlan();
        DroneMap.registerListeners();
        DroneMap.subscribeToSocket();
        DroneMap.restoreLockState();

        DroneHistoryMap.initialize();
    }

    static prepareSettingsSection() {
        let html = "";

        html += "<div class=\"row no-gutters border rounded overflow-hidden flex-md-row mb-4 shadow-sm h-md-250 position-relative\" style=\"margin: 8px;\">\n";
        html += "    <div class=\"col p-4 d-flex flex-column position-static\" id=\"google-api-key-container\">\n";
        html += "        <strong class=\"d-inline-block mb-2 text-primary translatable\" data-i18n=\"settings.maps.api-key\">?</strong>\n";
        html += "        <div class=\"input-group mb-3\">\n";
        html += "            <input id=\"apiKeyInput\" type=\"text\" class=\"form-control\" placeholder=\"abcdefghijklmnopqrstuvwxyz\" aria-describedby=\"setting-maps-api-key\">\n";
        html += "            <div class=\"input-group-append\">\n";
        html += "                <button type=\"button\" class=\"btn btn-success translatable\" data-i18n=\"settings.x.button.apply\" id=\"setting-maps-api-key\" onclick=\"SettingsManager.pushChanges();\">?</button>\n";
        html += "            </div>\n";
        html += "        </div>\n";
        html += "    </div>\n";
        html += "</div>\n";
        html += "<div class=\"row no-gutters border rounded overflow-hidden flex-md-row mb-4 shadow-sm h-md-250 position-relative\" style=\"margin: 8px;\">\n";
        html += "    <div class=\"col p-4 d-flex flex-column position-static\" id=\"map-type-container\">\n";
        html += "        <strong class=\"d-inline-block mb-2 text-primary translatable\" data-i18n=\"settings.maps.view-type\">?</strong>\n";
        html += "        <div class=\"images-selector\">\n";
        html += "            <div class=\"container\">\n";
        html += "                <div class=\"row\">\n";

        for (let type of MAPS_VIEW_TYPE_ARRAY) {
            let imageUrl = MAPS_VIEW_TYPE_IMAGE_URL.replace("%type%", type);

            html += "<div class=\"img-zone col\">\n";
            html += "    <p class=\"translatable\" data-i18n=\"settings.maps.view-type." + type + "\">?</p>\n";
            html += "    <img src=\"" + imageUrl + "\" class=\"map-type images-selector\" data-type=\"" + type.toLowerCase() + "\">\n";
            html += "</div>\n";
        }

        html += "                </div>\n";
        html += "            </div>\n";
        html += "        </div>\n";
        html += "        <button type=\"button\" class=\"btn btn-success translatable\" data-i18n=\"settings.x.button.apply\" style=\"margin: 8px;\" onclick=\"SettingsManager.pushChanges();\">?</button>\n";
        html += "    </div>\n";
        html += "</div>\n";

        //debugger;
        DroneMap.DIVS.SETTINGS_SECTION.innerHTML = html;

        let forEachMapViewImage = function(callback) {
            let elementHtml = document.getElementsByClassName('map-type');
            for (let image of elementHtml) {
                callback(image, elementHtml);
            }
        }

        forEachMapViewImage(function(item, elements) {
            item.selected = function() {
                return this.classList.contains("selected");
            }

            item.select = function() {
                this.classList.add("selected");
            }

            item.deselect = function() {
                item.classList.remove("selected");
            }

            item.addEventListener('click', function() {
                for (let item of elements) { /* Unselect */
                    item.deselect();
                }

                this.select();
            });
        })

        i18n.applyOn(DroneMap.DIVS.SETTINGS_SECTION);

        SettingsManager.add(new SettingsItem("google-api-key-container", DroneMap.SETTINGS_KEY.GOOGLE_MAPS_API, "abcdefghijklmnopqrstuvwxyz", function(div) {
            return div.getElementsByTagName("input")[0].value;
        }, function(div, value, isDefault, isChanged) {
            if (!isChanged) {
                div.getElementsByTagName("input")[0].value = value;
            }
        }, function(value) {
            // TODO
        }));

        SettingsManager.add(new SettingsItem("map-type-container", DroneMap.SETTINGS_KEY.MAP_VIEW_TYPE, MAPS_VIEW_TYPE_ARRAY[0], function(div) {
            let selected = MAPS_VIEW_TYPE_ARRAY[0];

            forEachMapViewImage(function(item, elements) {
                if (item.selected()) {
                    selected = item.dataset.type;
                }
            });

            return selected.toLowerCase();
        }, function(div, value, isDefault, isChanged) {
            if (!isChanged) {
                forEachMapViewImage(function(item, elements) {
                    if (item.dataset.type == value.toLowerCase()) {
                        for (let subitem of elements) { /* Unselect */
                            subitem.deselect();
                        }

                        item.select();
                    }
                });
            }
        }, function(value) {
            let valid = false;

            /* Verification process */
            for (let mapTypeIdField in google.maps.MapTypeId) {
                let mapTypeId = google.maps.MapTypeId[mapTypeIdField];

                if (value == mapTypeId) {
                    valid = true;
                    break;
                }
            }

            if (valid) {
                DroneMap.map.setOptions({
                    mapTypeId: value
                });
            } else {
                console.warn("Map: \"" + value + "\" seems to not be a valid map type.");
            }
        }));
    }

    static initializeFlighPlan() {
        DroneMap.flightPath = new google.maps.Polyline({
            path: [],
            geodesic: true,
            strokeColor: '#FF0000',
            strokeOpacity: 1.0,
            strokeWeight: 2
        });

        DroneMap.flightPath.setMap(DroneMap.map);
    }

    static registerListeners() {
        google.maps.event.addListener(DroneMap.map, 'click', function(event) {
            DroneMap.map.setCenter(event.latLng);
            DroneMap.MARKERS.DRONE.setPosition(event.latLng);

            DroneMap.appendFlightPlanCoordinates(event.latLng);
        });

        google.maps.event.addListener(DroneMap.map, 'keypress', function(event) {
            event = event || window.event;

            console.log(event.keyCode);
        });
    }

    static subscribeToSocket() {
        DroneSocket.subscribe(["flight.point.new"], function(identifier, json) {
            let latLng = DroneMap.createPositionObject(json.position.latitude, json.position.longitude);

            DroneMap.moveDroneMarker(latLng);
            DroneMap.appendFlightPlanCoordinates(latLng);

            console.log("Flight: received new position (lat/lon): " + latLng.lat() + "/" + latLng.lng() + " (" + new Date(json.position.time) + ")");
        });

        DroneSocket.subscribe(["flight.starting"], function(identifier, json) {
            if (DroneHistoryMap.running) {
                DroneMap.clearFlightPlanCoordinates();
            }
        });
    }

    static restoreLockState() {
        let cookieLockedState = Cookies.get(DroneMap.COOKIES.LOCKED_STATE.name);

        if (cookieLockedState == null) {
            cookieLockedState = DroneMap.COOKIES.LOCKED_STATE.default;
        } else {
            cookieLockedState = cookieLockedState.toBoolean();
        }

        console.log("Map: Restored lock state to: " + cookieLockedState);

        DroneMap.lockMapControls(cookieLockedState);
    }

    static createPositionObject(latitude, longitude) {
        return new google.maps.LatLng(latitude, longitude);
    }

    static createPositionObjectFromJson(json) {
        return DroneMap.createPositionObject(json.latitude, json.longitude);
    }

    static moveDroneMarker(latLng) {
        DroneMap.askSetMapCenter(latLng);
        DroneMap.MARKERS.DRONE.setPosition(latLng);
    }

    static askSetMapCenter(latLng) {
        if (DroneMap.locked) {
            DroneMap.map.setCenter(latLng);
        }
    }

    static appendFlightPlanCoordinates(latLng) {
        DroneMap.flightPath.getPath().push(latLng);

        console.log("Added point to flight plan (" + latLng.lat() + " , " + latLng.lng() + ").");
    }

    static clearFlightPlanCoordinates() {
        DroneMap.flightPath.getPath().clear();

        console.log("Map: Cleared flight plan.");
    }

    static formatCoordinates(position) {
        var lat = position.lat().toFixed(4);
        var lng = position.lng().toFixed(4);

        return [lat, lng];
    }

    static toggleLock() {
        DroneMap.lockMapControls(!DroneMap.locked);
    }

    static lockMapControls(lockState) {
        Cookies.set(DroneMap.COOKIES.LOCKED_STATE.name, lockState);
        DroneMap.locked = lockState;

        let iconElement = DroneMap.DIVS.LOCK_TOGGLE_ICON;
        iconElement.removeAttribute("class");
        iconElement.setAttribute("class", DroneMap.ICONS[lockState ? "LOCK" : "UNLOCK"]);

        DroneMap.map.setOptions({
            keyboardShortcuts: !lockState,
            scrollwheel: !lockState,
            gestureHandling: lockState ? "none" : "auto",
            panControl: !lockState,

            /* The enabled/disabled state of the Fullscreen control. */
            fullscreenControl: !lockState,

            /* The enabled/disabled state of the Zoom control. */
            zoomControl: !lockState
        });
    }

}

class DroneHistoryMap {

    static initialize() {
        DroneHistoryMap.running = false;
        DroneHistoryMap.cachedFlightPositionHistory = {};
        DroneHistoryMap.cachedPictureMarkers = [];
        DroneHistoryMap.currentFlight = null;

        DroneHistoryMap.DIVS = {
            "BACK_TO_CURRENT_ICON_LINK": document.getElementById("map-lock-toggle-icon-link")
        }

        DroneHistoryMap.MARKERS = {
            "PICTURE": new google.maps.Marker({
                //position: latlng,
                map: DroneMap.map,
                icon: {
                    url: "https://www.pilotpen.fr/pub/media/catalog/product/cache/image/755x566/beff4985b56e3afdbeabfc89641a4582/4/9/4902505511097-4902505511097_zoom_01.jpg",
                    scaledSize: new google.maps.Size(50, 50),
                    origin: new google.maps.Point(0, 0),
                    anchor: new google.maps.Point(25, 50)
                }
            }),
        };

        DroneHistoryMap.subscribeToSocket();
        DroneHistoryMap.changeIconLinkVisibilityState(false);
    }

    static subscribeToSocket() {
        DroneSocket.subscribe(["flight.point.new"], function(identifier, json) {
            let targetHistoryArray = DroneHistoryMap.cachedFlightPositionHistory[json.flight.local_file];
            if (targetHistoryArray == null) {
                targetHistoryArray = [];
            }
            targetHistoryArray.push(json.position);
            DroneHistoryMap.cachedFlightPositionHistory[json.flight.local_file] = targetHistoryArray;
        });
    }

    static fillCachedPositionHistory(flights, current) {
        DroneHistoryMap.currentFlight = current != FLIGHT_DEFAULT_UNKNOWN_NAME ? current : null;

        for (let flight of flights) {
            let name = flight.name;
            let local_file = flight.local_file;
            let positions = flight.positions;

            DroneHistoryMap.cachedFlightPositionHistory[local_file] = positions;
            console.log("Map: Cached " + positions.length + " position(s) for flight \"" + name + "\" (local file: " + local_file + ")");

            if (current == local_file) {
                DroneHistoryMap.displayHistory(local_file, true, true);
            }
        }
    }

    static displayHistory(flightLocalFile, clearBefore = true, restorationOnly = false) {
        let positions = DroneHistoryMap.cachedFlightPositionHistory[flightLocalFile];

        if (positions != null) {
            if (clearBefore) {
                DroneMap.clearFlightPlanCoordinates();
            }

            for (let position of positions) {
                DroneMap.appendFlightPlanCoordinates(DroneMap.createPositionObject(position.latitude, position.longitude));
            }

            if (!restorationOnly) {
                DroneHistoryMap.run();
            }
        } else {
            console.warn("Map: Tried to show history of a flight without an history.");
        }
    }

    static run() {
        DroneEvent.fire(EVENT_HISTORY_MODE_OPEN);
        DroneHistoryMap.running = true;

        if (DroneHistoryMap.currentFlight != null) {
            DroneHistoryMap.changeIconLinkVisibilityState(true);
        }

        // TODO Carousel
    }

    static backToCurrent() {
        if (!DroneHistoryMap.running) {
            console.warn("Map History: Tried to went back to current but history is not running.");
            return;
        }

        DroneEvent.fire(EVENT_HISTORY_MODE_CLOSE);
        DroneHistoryMap.running = false;
        DroneHistoryMap.changeIconLinkVisibilityState(false);
    }

    static changeIconLinkVisibilityState(state) {
        DroneHistoryMap.DIVS.BACK_TO_CURRENT_ICON_LINK.style.display = state === true ? "block" : "none";
    }

    static createPictureMarker(label, callback = null) {
        let marker = new google.maps.Marker({
            //position: latlng,
            map: DroneMap.map,
            label: label
        });

        if (callback != null) {
            marker.addListener('click', callback);
        }

        DroneHistoryMap.cachedPictureMarkers.push(marker);

        return marker;
    }

}