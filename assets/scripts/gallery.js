class Gallery {

    static initialize() {
        Gallery.DIVS = {
            FLIGHTS: document.getElementById("gallery-flights"),
            PICTURES: document.getElementById("gallery-pictures")
        }

        Gallery.currentFlightLocalFile = null;
        Gallery.displayedFlightLocalFile = null;
        Gallery.cachedPictures = null;

        Gallery.subscribeToSocket();

        PictureViewer.initialize();
    }

    static subscribeToSocket() {
        DroneSocket.subscribe(["picture.download.finished"], function(identifier, json) {
            if (Gallery.currentFlightLocalFile != null && Gallery.currentFlightLocalFile == Gallery.displayedFlightLocalFile) {
                Gallery.addPicture(json.file);
            }
        });

        DroneSocket.subscribe(["flight.starting", "flight.finished"], function(identifier, json) {
            Gallery.fillGallery();
        });
    }

    static fillGallery() {
        Gallery.DIVS.FLIGHTS.innerHTML = HTML_PART_LOADER;

        DroneApi.call(ENDPOINT_FLIGHTS, function(json, success) {
            //console.log(json);

            if (!success) {
                log("Failed to fetch flights.");
                return true;
            }

            let firstIsCurrent = json.current.active;
            let flights = [];

            if (firstIsCurrent) {
                let currentFlight = json.current.flight;

                flights.push(currentFlight);
                Gallery.currentFlightLocalFile = currentFlight.local_file;
            }
            flights.extend(json.all);
            //console.log(flights);
            
			DroneHistoryMap.fillCachedPositionHistory(flights, firstIsCurrent ? flights[0].local_file : null);

            let html = "";
            for (let index = 0; index < flights.length; index++) {
                let flight = flights[index];
                //console.log(index);

                let active = index == 0 && firstIsCurrent;
                let mutedClassPart = !active ? " class=\"text-muted\"" : "";
                let durationPart, durationTranslatablePart;
                
                if (flight.end == 0) {
                    durationTranslatablePart = "flight.list.lasted.running";

                    if (flight.rushed) {
                        durationTranslatablePart = "flight.list.lasted.rushed";
                    }

                    durationPart = i18n.get(durationTranslatablePart);
                } else {
                    durationPart = new Date(flight.end - flight.start).toSimpleHourString();
                }
               

                html += "<a data-flight=\"" + flight.local_file + "\" onclick=\"Gallery.selectFlight(this);\" href=\"#\" class=\"gallery-flight-item list-group-item list-group-item-action\">";
                html += "	<div class=\"d-flex w-100 justify-content-between\">\n";
                html += "		<h5 class=\"mb-1\">" + flight.name + "</h5>\n";
                html += "		<small>" + new Date(flight.start).toLocaleString() + "</small>\n";
                html += "	</div>\n";
                html += "	<p class=\"same-line translatable\" data-i18n=\"flight.list.lasted\">" + i18n.get("flight.list.lasted") + "</p> : <p class=\"same-line" + (durationTranslatablePart != null ? " translatable" : "") + "\"" + (durationTranslatablePart != null ? " data-i18n=\"" + durationTranslatablePart + "\"" : "") + ">" + durationPart + "</p>.";
                // html += " <small" + mutedClassPart + ">Nombre de photo prises:</small>\n";
                // html += " <span class=\"badge badge-primary badge-pill\">" + flight.pictures.length + "</span>\n";
                html += "</a>\n";
            }

            html += "<a data-flight=\"" + FLIGHT_DEFAULT_UNKNOWN_NAME + "\" onclick=\"Gallery.selectFlight(this);\" href=\"#\" class=\"gallery-flight-item list-group-item list-group-item-action\">";
            html += "	<div class=\"d-flex w-100 justify-content-between\">\n";
            html += "		<h5 class=\"mb-1 translatable\" data-i18n=\"flight.list.item.others\">" + i18n.get("flight.list.item.others") + "</h5>\n";
            html += "	</div>\n";
            html += "</a>\n";

            Gallery.DIVS.FLIGHTS.innerHTML = html;

            let elements = Gallery.DIVS.FLIGHTS.getElementsByClassName("gallery-flight-item");
            //console.log(elements);
            if (elements.length > 0) {
                Gallery.selectFlight(elements[0]);
            }
        });
    }

    static unselectAllFlights() {
        Gallery.displayedFlightLocalFile = null;

        let elements = Gallery.DIVS.FLIGHTS.getElementsByClassName("gallery-flight-item");

        for (let element of elements) {
            element.classList.remove("active");
        }
    }

    static selectFlight(div) {
        //console.log(div);

        Gallery.unselectAllFlights();
        Gallery.DIVS.PICTURES.innerHTML = HTML_PART_LOADER;

        DroneApi.call(ENDPOINT_PICTURE_LIST, function(json, success) {
            // console.log(json);

            if (!success) {
                log("Failed to fetch pictures list.");
                return true;
            }

            Gallery.cachedPictures = json;

            Gallery.unselectAllFlights(); /* Just to be sure */

            div.classList.add("active");

            let flightLocalFile = Gallery.displayedFlightLocalFile = div.dataset.flight;
            let pictures = [];

            if (json[flightLocalFile] != undefined) {
                pictures.extend(json[flightLocalFile]);
            }

            //console.log(flightLocalFile);

            Gallery.DIVS.PICTURES.innerHTML = "";

            if (pictures.length > 0) {
                for (let index = 0; index < pictures.length; index++) {
                    let picture = pictures[index];

                    Gallery.addPicture(picture, flightLocalFile);
                }
            } else {
                Gallery.DIVS.PICTURES.innerHTML = i18n.get("gallery.text.no-picture");
            }
        });
    }

    static addPicture(picture, flightLocalFile = null) {
        let html = Gallery.DIVS.PICTURES.innerHTML;

        let nodes = Gallery.DIVS.PICTURES.childNodes;
        if (nodes.length == 1 && nodes[0].tagName != "DIV") {
            html = "";
        }

        let elementHtml = "";
        elementHtml += "<div class=\"picture-container\">\n";
        elementHtml += "	<img class=\"picture\" src=\"" + API_URL + picture.remote + "\">\n";
        elementHtml += "	<div class=\"middle\">\n";
        elementHtml += "		<a data-latitude=\"" + picture.position.latitude + "\" data-longitude=\"" + picture.position.longitude + "\" " + (flightLocalFile != null ? "data-flight=\"" + flightLocalFile + "\"" : "") + " onclick=\"Gallery.showOnMap(this);\" href=\"#\">\n";
        elementHtml += "			<button type=\"button\" class=\"btn btn-success translatable\" data-i18n=\"gallery.picture.show-position\">Success</button>\n";
        elementHtml += "		</a>\n";
        elementHtml += "	</div>\n";
        elementHtml += "</div>\n";

        html = elementHtml + html;

        Gallery.DIVS.PICTURES.innerHTML = html;
        i18n.applyOn(Gallery.DIVS.PICTURES);
    }

    static showOnMap(element) {
        let latitude = element.dataset.latitude;
        let longitude = element.dataset.longitude;

        let flightLocalFile = element.dataset.flight;
        
        let lngLat = DroneMap.createPositionObject(latitude, longitude);
        DroneMap.askSetMapCenter(lngLat);
        DroneHistoryMap.MARKERS.PICTURE.setPosition(lngLat); // TODO

        if (flightLocalFile != FLIGHT_DEFAULT_UNKNOWN_NAME) {
            DroneHistoryMap.displayHistory(flightLocalFile);
            HistoryDashboard.displayCarousel(Gallery.cachedPictures[flightLocalFile]);
        } else { /* A "lost" picture */
            DroneMap.clearFlightPlanCoordinates();
        }


        Gallery.display(false);
    }

    static display(state) {
        let modal = document.getElementById("modal-gallery");
        let display = "none";

        if (state === true) {
            display = "block";
        }

        modal.style.display = display;
    }

}

class PictureViewer {

    static initialize() {
        PictureViewer.MODAL = {
            "VIEWER": document.getElementById("modal-picture-viewer")
        }

        PictureViewer.DIVS = {
            "ELEMENT": document.getElementById("picture-viewer-image-element")
        }
    }

    static show(imageUrl) {
        if (imageUrl == null) {
            console.warn("PictureViewer: Tried to show a picture without a valid image url.");
            return;
        }

        PictureViewer.DIVS.ELEMENT.src = imageUrl;
        PictureViewer.display(true);
    }


    static display(state) {
        PictureViewer.MODAL.VIEWER.style.display = (state === true ? "block" : "none");
    }


}