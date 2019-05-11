class Dashboard {

    static initialize() {
        FlightDashboard.initialize();
        HistoryDashboard.initialize();
    }

}

class FlightDashboard {

    static initialize() {
        FlightDashboard.DIVS = {
            "CONTAINER": document.getElementById("flight-dashboard"),
            "ATTITUDE": document.getElementById("orientation-widget-attitude"),
            "ORIENTATION": document.getElementById("orientation-widget-orientation")
        }

        FlightDashboard.JQELEMENT = {
            "ATTITUDE": $.flightIndicator('#orientation-widget-attitude', 'attitude', {
                roll: 0,
                pitch: 0,
                size: 200,
                showBox: false
            }),
            "HEADING": $.flightIndicator('#orientation-widget-heading', 'heading', {
                heading: 0,
                showBox: false
            })
        }

        FlightDashboard.running = false;

        FlightDashboard.subscribeToApi();
        FlightDashboard.subscribeToSocket();
        FlightDashboard.subscribeToEvent();
    }

    static subscribeToApi() {
        DroneApi.listen(ENDPOINT_FLIGHTS, function(endpoint, json, success, restartWanted) {
            let running = FlightDashboard.running = safeWalk(json, ["current", "active"], false);

            FlightDashboard.display(running);
        });
    }

    static subscribeToSocket() {
        DroneSocket.subscribe(["flight.starting"], function(identifier, json) {
            FlightDashboard.running = true;

            FlightDashboard.display(true);
        });
        DroneSocket.subscribe(["flight.finished"], function(identifier, json) {
            FlightDashboard.running = false;

            FlightDashboard.display(false);
        });

        DroneSocket.subscribe(["qualities.new"], function(identifier, json) {
            let orientation = json.new_values[ORIENTATION_PHYSICAL_QUALITY_NAME];

            if (orientation == null || orientation.length == 0) {
                return;
            }

            let parts = orientation[0].content.split(":");

            let yaw = parseInt(parts[0]) || Infinity;
            let pitch = parseInt(parts[1]) || Infinity;
            let roll = parseInt(parts[2]) || Infinity;

            if (yaw != Infinity) {
                FlightDashboard.JQELEMENT.HEADING.setHeading(yaw);
            }

            if (pitch != Infinity) {
                FlightDashboard.JQELEMENT.ATTITUDE.setPitch(pitch);
            }

            if (roll != Infinity) {
                FlightDashboard.JQELEMENT.ATTITUDE.setRoll(roll);
            }
        });
    }

    static subscribeToEvent() {
        DroneEvent.on(EVENT_HISTORY_MODE_OPEN, function() {
            FlightDashboard.display(false);
        });

        DroneEvent.on(EVENT_HISTORY_MODE_CLOSE, function() {
            if (FlightDashboard.running) {
                FlightDashboard.display(true);
            }
        });
    }

    static debug() {
        let increment = 0;

        setInterval(function() {
            increment++;

            FlightDashboard.JQELEMENT.ATTITUDE.setRoll(30 * Math.sin(increment / 10));
            FlightDashboard.JQELEMENT.ATTITUDE.setPitch(50 * Math.sin(increment / 20));

            FlightDashboard.JQELEMENT.HEADING.setHeading(increment);
        }, 50);
    }

    static display(state) {
        FlightDashboard.DIVS.CONTAINER.style.display = (state === true ? "" : "none");
    }

}

class HistoryDashboard {

    static initialize() {
        HistoryDashboard.DIVS = {
            "CONTAINER": document.getElementById("history-dashboard")
        }

        HistoryDashboard.CURRENT_CAROUSEL = {
            valid: false,
            pictures: null
        }

        HistoryDashboard.subscribeToApi();
        HistoryDashboard.subscribeToEvent();
    }

    static subscribeToApi() {
        DroneApi.listen(ENDPOINT_FLIGHTS, function(endpoint, json, success, restartWanted) {
            let running = FlightDashboard.running = safeWalk(json, ["current", "active"], false);

            HistoryDashboard.display(!running);
        });
    }

    static subscribeToEvent() {
        DroneEvent.on(EVENT_HISTORY_MODE_OPEN, function() {
            HistoryDashboard.display(true);
        });

        DroneEvent.on(EVENT_HISTORY_MODE_CLOSE, function() {
            HistoryDashboard.display(false);
        });
    }

    static displayCarousel(pictures) {
        if (pictures == null || pictures.length == 0) {
            // TODO
            console.warn("Dashboard/History: Tried to display a carousel without any picture.");
            return;
        }

        HistoryDashboard.DIVS.CONTAINER.innerHTML = HistoryDashboard.createCarousel(pictures);
        i18n.applyOn(HistoryDashboard.DIVS.CONTAINER);
    }

    static createCarousel(pictures) {
        let html = "";

        html += "                <div id=\"picture-history-carousel\" class=\"slideshow-container\">\n";
        html += "                    <div id=\"history-carousel\" class=\"carousel slide\" data-ride=\"carousel\" data-interval=\"0\" style=\"\">\n";

        html += "                        <ol class=\"carousel-indicators\">\n";
        for (let i = 0; i < pictures.length; i++) {
            let picture = pictures[i];

            html += "                            <li data-target=\"#history-carousel\" data-slide-to=\"" + i + "\" class=\"" + (i == 0 ? "active" : "")  + "\"></li>\n";
        }
        html += "                        </ol>\n";

        html += "                        <div class=\"carousel-inner\">\n";
        for (let i = 0; i < pictures.length; i++) {
            let picture = pictures[i];

            let remote = API_URL + picture.remote;

            let position = DroneMap.formatCoordinates(DroneMap.createPositionObjectFromJson(picture.position));
            let title = position[0] + "/" + position[1] + " - ~" + formatBytes(picture.length);

            html += "                            <div class=\"carousel-item" + (i == 0 ? " active" : "")  + "\">\n";
            html += "                                <img class=\"d-block w-100\" src=\"" + remote + "\" alt=\"slide #" + i + "\">\n";
            html += "                                <div class=\"carousel-caption d-none d-md-block\">\n";
            html += "                                    <button onclick=\"PictureViewer.show('" + remote + "');\" class=\"btn btn-success translatable\" style=\"width: 120px; display: inline;\" data-i18n=\"history.picture.show\">?</button>\n";
            html += "                                    <button class=\"btn btn-success translatable\" style=\"width: 120px; display: inline;\" data-i18n=\"history.picture.move-to\">?</button>\n";
            html += "                                    <h6 style=\"margin: 5px; background-color: rgba(0, 0, 0, 0.4); font-family: Consolas;\">" + title + "</h6>\n";
            html += "                                </div>\n";
            html += "                            </div>\n";
        }
        html += "                        </div>\n";

        html += "                        <a class=\"carousel-control-prev\" href=\"#history-carousel\" role=\"button\" data-slide=\"prev\">\n";
        html += "                            <span class=\"carousel-control-prev-icon\" aria-hidden=\"true\"></span>\n";
        html += "                            <span class=\"sr-only\">Previous</span>\n";
        html += "                        </a>\n";
        html += "                        <a class=\"carousel-control-next\" href=\"#history-carousel\" role=\"button\" data-slide=\"next\">\n";
        html += "                            <span class=\"carousel-control-next-icon\" aria-hidden=\"true\"></span>\n";
        html += "                            <span class=\"sr-only\">Next</span>\n";
        html += "                        </a>\n";
        html += "                    </div>\n";
        html += "                </div>\n";

        HistoryDashboard.CURRENT_CAROUSEL.pictures = pictures;

        return html;
    }

    static validateCarousel(validation) {
        HistoryDashboard.CURRENT_CAROUSEL.valid = validation;
    }

    static display(state) {
        HistoryDashboard.DIVS.CONTAINER.style.display = (state === true ? "" : "none");
    }

}