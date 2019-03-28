var logIntercepter = function(message) {
//	alert("Default Log Intercepter\n\nMessage: " + message);
};

function log(message) {
	console.log(message);
	
	if (logIntercepter != null) {
		logIntercepter(message);
	}
}