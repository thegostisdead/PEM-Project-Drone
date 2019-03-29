var logIntercepter = function(message) {
	// alert("Default Log Intercepter\n\nMessage: " + message);
};

function log(message) {
	console.log(message);

	if (logIntercepter != null) {
		logIntercepter(message);
	}
}

function random(max, min, idToUpdate) {
	return Math.random() * (max - min) + min;
}

function randomText(length) {
	var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	var text = "";

	for (var i = 0; i < length; i++) {
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}

	return text;
}

Array.prototype.extend = function(other) {
	other.forEach(function(item) {
		this.push(item);
	}, this);
}