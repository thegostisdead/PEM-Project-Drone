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

if (!Date.prototype.correctTimezoneOffset) {
    Date.prototype.correctTimezoneOffset = function() {
        let timestamp = this.getTime();

        timestamp += DATE_TIMEZONE_HOUR_OFFSET * 60 * 1000;

        return new Date(timestamp);
    }
}

if (!Date.prototype.toSimpleHourString) {
    Date.prototype.toSimpleHourString = function() {
        let hours = this.getHours();
        let minutes = this.getMinutes();
        let seconds = this.getSeconds();

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
}

if (!Date.prototype.toSimpleDayString) {
    Date.prototype.toSimpleDayString = function() {
        let year = this.getFullYear();
        let month = this.getMonth();
        let day = this.getDay();

        if (month < 10) {
            month = "0" + month;
        }
        if (day < 10) {
            day = "0" + day;
        }

        return "" + day + ":" + month + ":" + year + " at " + this.toSimpleHourString();
    }
}

if (!Date.prototype.round) {
    Date.prototype.rounded = function() {
        let timestamp = this.getTime();

        timestamp -= timestamp % (24 * 60 * 60 * 1000); /* Subtract amount of time since midnight */
        timestamp += new Date().getTimezoneOffset() * 60 * 1000; /* Add on the timezone offset */

        return new Date(timestamp);
    }
}

if (!Array.prototype.extend) {
    Array.prototype.extend = function(other) {
        other.forEach(function(item) {
            this.push(item);
        }, this);
    }
}

if (!String.prototype.format) {
    String.prototype.format = function() {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function(match, number) {
            return typeof args[number] != 'undefined' ? args[number] : match;
        });
    };
}

if (!String.prototype.toBoolean) {
    String.prototype.toBoolean = function() {
        switch (this.toLowerCase().trim()) {
            case "true":
            case "yes":
            case "1":
                return true;

            case "false":
            case "no":
            case "0":
            case null:
                return false;

            default:
                return Boolean(string);
        }
    }
};


class EventDelayer {

    constructor() {
        this.callbacks = [];
        this.fired = false;
    }

    delay(callback) {
        if (!this.fired) {
            this.callbacks.push(callback);
        } else {
            callback();
        }
    }

    fire() {
        for (let callback of this.callbacks) {
            callback();
        }
    }

}