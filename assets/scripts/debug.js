class DroneDebug {

    static startFlight(responseElementId) {
        let element = document.getElementById(responseElementId);

        let name = prompt("flight name ?", randomString(50));

        if (name == null) {
            console.log("Debug: Start flight cancelled.")
            return;
        }

        DroneApi.call("flight/start/" + name, function(json, success) {
            element.innerHTML = json.result;
        });
    }

    static stopFlight(responseElementId) {
        let element = document.getElementById(responseElementId);

        DroneApi.call("flight/stop", function(json, success) {
            element.innerHTML = json.result;
        });
    }

    static toggleRandomOrientationValues(responseElementId) {
        let element = document.getElementById(responseElementId);

        let intervalId = DroneDebug.randomOrientationValueIntervalId;

        if (intervalId == null) {
            intervalId = FlightDashboard.debug();

            element.innerHTML = "started, interval id: " + intervalId;
        } else {
            clearInterval(intervalId);
            intervalId = null;

            element.innerHTML = "stopped";
        }

        DroneDebug.randomOrientationValueIntervalId = intervalId;
    }

    static useLogger() {
        Logger.init();

        let loggers = {
            "log": {
                "display": "  LOG",
                "color": "#acb6ad"
            },
            "info": {
                "display": " INFO",
                "color": "#15e01b"
            },
            "warn": {
                "display": " WARN",
                "color": "#ffc107"
            },
            "error": {
                "display": "ERROR",
                "color": "#ff0000"
            },
            "trace": {
                "display": "TRACE",
                "color": "#d800ff"
            }
        };

        for (const sub in loggers) {
            let settings = loggers[sub];
            console["old_" + sub] = console[sub];

            console[sub] = function() {
                console["old_" + sub].apply(null, arguments);

                if (sub == "trace") {
                    /* Getting trace */
                    let object = {};
                    Error.captureStackTrace(object, this);
                    let stacktrace = object.stack.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");

                    Array.prototype.push.call(arguments, "\n" + stacktrace);
                }

                let message = Array.prototype.join.call(arguments, " ");
                let lines = message.split(/\r\n|\r|\n/);

                for (let i = 0; i < lines.length; i++) {
                    let line = lines[i].replace(/ /g, "&nbsp;");

                    Logger.print(("<font style=\"display: inline-block;text-align: end;padding-right: 5px;width: 50px;background: " + settings.color + ";\">" + (i == 0 ? settings.display : "&nbsp;") + "</font> ") + line);
                }
            }
        }

        window.onerror = function(msg, url, line, col, error) {
            if (error != null) {
                console.error(error.stack);
            }
        };

        console.info("           o x o x o x o . . .\r\n         o      _____            _______________ ___=====__T___\r\n       .][__n_n_|DD[  ====_____  |    |.\\\/.|   | |   |_|     |_\r\n      >(________|__|_[_________]_|____|_\/\\_|___|_|___________|_|\r\n      _\/oo OOOOO oo`  ooo   ooo   o^o       o^o   o^o     o^o\r\n-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");
    }

    static tryToRemoveGoogleMapDialog() {
        let element = document.getElementById("map-canvas");
        let nodes = element.childNodes;

        let count = 0;
        let id = setInterval(function() {
            if (nodes.length > 1) {
                try {
                    element.removeChild(nodes[1]);
                    console.log("Debug: Removed Google Map error dialog.");
                } catch (error) {
                    console.error("Debug: Failed to remove Google Map error dialog.", error);
                }

                clearInterval(id);
            }

            if (++count > 40) {
                console.log("Debug: Cancelled interval, limit reached.");
                clearInterval(id);
            }
        }, 250);
    }

}