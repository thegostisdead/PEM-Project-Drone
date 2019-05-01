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
	
	static useLogger() {
		Logger.init();
		
		for (let sub of ["log", "warn", "error"]) {
			console["old_" + sub] = console[sub];
			
			console[sub] = function(string) {
				Logger.print(string);
				
				var i = -1, l = arguments.length, args = [], fn = "console.old_" + sub + "(args)";
				while(++i<l){
					args.push('args['+i+']');
				};
				fn = new Function('args',fn.replace(/args/,args.join(',')));
				fn(arguments);
			}
		}
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

            if(++count > 40) {
                console.log("Debug: Cancelled interval, limit reached.");
                clearInterval(id);
            }
        }, 250);
    }

}