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

        FlightDashboard.subscribeToApi();
        FlightDashboard.subscribeToSocket();
    }

    static subscribeToApi() {
        DroneApi.listen(ENDPOINT_FLIGHTS, function(endpoint, json, success, restartWanted) {
            FlightDashboard.display(safeWalk(json, ["current", "active"], false));
        });
    }

    static subscribeToSocket() {
        DroneSocket.subscribe(["flight.starting"], function(identifier, json) {
            FlightDashboard.display(true);
        });
        DroneSocket.subscribe(["flight.finished"], function(identifier, json) {
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