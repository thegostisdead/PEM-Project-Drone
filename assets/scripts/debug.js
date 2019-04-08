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

}