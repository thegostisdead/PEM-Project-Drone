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

function randomString(length) {
	var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	var text = "";

	for (var i = 0; i < length; i++) {
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}

	return text;
}

function formatDate(date) {
	let hours = date.getHours() - 1;
	let minutes = date.getMinutes();
	let seconds = date.getSeconds();

	if (hours < 10) {
		hours = "0" + hours;
	}
	if (minutes < 10) {
		minutes = "0" + minutes;
	}
	if (seconds < 10) {
		seconds = "0" + seconds;
	}

	return "" + hours + ":" + minutes + ":" + seconds;
}

Array.prototype.extend = function(other) {
	other.forEach(function(item) {
		this.push(item);
	}, this);
}