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
            }),
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

        DroneMap.DIVS = {
            "LOCK_TOGGLE_ICON": document.getElementById("map-lock-toggle-icon")
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

        DroneMap.cachedFlightPositionHistory = {};

        DroneMap.initializeFlighPlan();
        DroneMap.registerListeners();
        DroneMap.subscribeToSocket();
        DroneMap.restoreLockState();
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
            let targetHistoryArray = DroneMap.cachedFlightPositionHistory[json.flight];
            if (targetHistoryArray == null) {
                targetHistoryArray = [];
            }
            targetHistoryArray.push(json.position);
            DroneMap.cachedFlightPositionHistory[json.flight] = targetHistoryArray;

            let latLng = DroneMap.createPositionObject(json.position.latitude, json.position.longitude);

            DroneMap.moveDroneMarker(latLng);
            DroneMap.appendFlightPlanCoordinates(latLng);

            console.log("Flight: received new position (lat/lon): " + latLng.lat() + "/" + latLng.lng() + " (" + new Date(json.position.time) + ")");
        });

        DroneSocket.subscribe(["flight.starting"], function(identifier, json) {
            DroneMap.clearFlightPlanCoordinates();
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

    static fillCachedHistory(flights, current) {
        for (let flight of flights) {
            let name = flight.name;
            let local_file = flight.local_file;
            let positions = flight.positions;

            DroneMap.cachedFlightPositionHistory[local_file] = positions;
            console.log("Map: Cached " + positions.length + " position(s) for flight \"" + name + "\" (local file: " + local_file + ")");

            console.old_log(current);
            if (current == local_file) {
                DroneMap.displayHistory(local_file);
            }
        }
    }

    static displayHistory(flightLocalFile, clearBefore = true) {
        let positions = DroneMap.cachedFlightPositionHistory[flightLocalFile];

        if (positions != null) {
            if (clearBefore) {
                DroneMap.clearFlightPlanCoordinates();
            }

            for (let position of positions) {
                DroneMap.appendFlightPlanCoordinates(DroneMap.createPositionObject(position.latitude, position.longitude));
            }
        }
    }

    static createPositionObject(latitude, longitude) {
        return new google.maps.LatLng(latitude, longitude);
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

    static displayCoordinates(position) {
        var lat = position.lat().toFixed(4);
        var lng = position.lng().toFixed(4);

        console.log("Latitude: " + lat + " // Longitude: " + lng);
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