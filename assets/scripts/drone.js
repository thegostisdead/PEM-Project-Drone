class DroneSocket {

    static initialize() {
        DroneSocket.socket = null;

        DroneSocket.listeners = [];
        DroneSocket.subscriptions = [];
    }

    static connect() {
        let socket = DroneSocket.socket = new WebSocket("ws://" + WEB_SOCKET_HOST + ":" + WEB_SOCKET_PORT);

        socket.onopen = function() {
            console.log("WebSocket: Connected");

            DroneSocket.fire("onopen", null);
        };

        socket.onmessage = function(event) {
            let data = event.data;
            console.log("WebSocket: Received message:", data);

            let json = JSON.parse(data);

            /* Subscribed Callbacks */
            try {
                let identifier = json.identifier;
                let callbacks = DroneSocket.subscriptions[identifier];

                if (callbacks != undefined) {
                    for (let callback of callbacks) {
                        try {
                            callback(identifier, json.data);
                        } catch (exception) {
                            console.error(exception);
                        }
                    }
                }
            } catch (exception) {
                console.error(exception);
            }

            /* Fire registered listeners */
            DroneSocket.fire("onmessage", json);
        };

        socket.onclose = function(event) {
            console.log("WebSocket: Closed. Reconnect will be attempted in 1 second.", event.reason);

            setTimeout(function() {
                DroneSocket.connect();
            }, 1000);
        };

        socket.onerror = function(error) {
            console.error("WebSocket: Encountered error: ", error.message, "Closing socket.");
            socket.close();

            DroneSocket.fire("onerror", error);
        };
    }

    static addListener(event, listener) {
        let array = DroneSocket.listeners[event];

        if (array == undefined) {
            array = [];
            DroneSocket.listeners[event] = array;
        }

        array.push(listener);
        console.log("WebSocket: Registered listener on event: " + event);
    }

    static subscribe(identifiers, callback) {
        if (typeof(identifiers) == "string") {
            identifiers = [identifiers];
            console.warn("WebSocket: Trying to subscribe with a string parameter, please use an array next time.");
        }

        for (let identifier of identifiers) {
            let array = DroneSocket.subscriptions[identifier];

            if (array == undefined) {
                array = [];
                DroneSocket.subscriptions[identifier] = array;
            }

            array.push(callback);
            console.log("WebSocket: Registered callback on identifier: " + identifier);
        }
    }

    /**
     * Fire an event from added listeners.
     * 
     * @param {string} event Event's name.
     * @param {object} data Event's data.
     */
    static fire(event, data) {
        let array = DroneSocket.listeners[event];

        if (array != undefined) {
            for (let listener of array) {
                try {
                    listener(data);
                } catch (exception) {
                    console.error(exception);
                }
            }
        }
    }

}

class DroneApi {

    static initialize() {

    }

    static call(endpoint, callback, payload = null) {
        let request = new XMLHttpRequest();

        request.onreadystatechange = function(event) {
            if (this.readyState === XMLHttpRequest.DONE) {
                let json = JSON.parse(this.responseText);

                if (callback(json, this.status === 200) === true) {
                    setTimeout(function() {
                        DroneApi.call(endpoint, callback, payload);
                    }, API_CALL_FAILED_RETRY_WAIT);
                }
            }
        };

        request.open("GET", API_URL + "/" + endpoint, true);
        request.send(payload);
    }

}