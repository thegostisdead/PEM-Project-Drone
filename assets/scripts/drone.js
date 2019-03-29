class DroneSocket {
	
	static initialize() {
		DroneSocket.socket = null;

		DroneSocket.listeners = [];
	}
	
	static connect() {
	    let socket = DroneSocket.socket = new WebSocket("ws://" + WEB_SOCKET_HOST + ":" + WEB_SOCKET_PORT);

	    socket.onopen = function() {
		    console.log("Connected");

		    DroneSocket.fire("onopen", null);
	    };

	    socket.onmessage = function(event) {
	    	let data = event.data;
		    console.log("Message:", data);

		    DroneSocket.fire("onmessage", JSON.parse(data));
	    };

	    socket.onclose = function(event) {
		    console.log("Socket is closed. Reconnect will be attempted in 1 second.", event.reason);

		    setTimeout(function() {
			    DroneSocket.connect();
		    }, 1000);
	    };

	    socket.onerror = function(error) {
		    console.error("Socket encountered error: ", error.message, "Closing socket");
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
	}
	
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
				
				callback(json, this.status === 200);
			}
		};

		request.open("GET", API_URL + "/" + endpoint, true);
		request.send(payload);
	}
	
}