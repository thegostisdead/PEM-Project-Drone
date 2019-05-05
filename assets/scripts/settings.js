class SettingsItem {

    constructor(baseDivId, key, defaultValue, getHandler, setHandler, receiveValueCallback) {
        this.baseDiv = document.getElementById(baseDivId);
        this.key = key;
        this.defaultValue = defaultValue;
        this.getHandler = getHandler;
		this.setHandler = setHandler;
		this.receiveValueCallback = receiveValueCallback;
    }

    set(value, isDefault, isChanged) {
        return this.setHandler(this.baseDiv, value, isDefault, isChanged);
    }

    value() {
        return this.getHandler(this.baseDiv);
	}
	
	onReceive(value) {
		return this.receiveValueCallback(value);
	}

    visible(state) {
        this.baseDiv.style.visibility = state ? "visible" : "hidden";
    }

}

class SettingsManager {

    static initialize() {
		SettingsManager.cachedSettings = null;
        SettingsManager.settingsItems = [];
		
		SettingsManager.retrive(API_ENDPOINT_SETTINGS);
    }

    static add(settingsItem) {
		SettingsManager.settingsItems.push(settingsItem);

		SettingsManager.notify();
    }

    static retrive(url, callback) {
        for (let item of this.settingsItems) {
            item.visible(false);
        }

        this.doRetrive(url, callback);
    }

    static push(url) {
        this.doPush(url)
    }

    static doRetrive(url, callback = null) {
        let xml = new XMLHttpRequest();

        xml.onreadystatechange = function(event) {
            if (this.readyState === XMLHttpRequest.DONE) {
                if (this.status === 200) {
                    let json = JSON.parse(this.responseText);
					// console.log(json);

					SettingsManager.cachedSettings = json;
					SettingsManager.notify();

					if (callback != null) {
						callback();
					}
                } else {
                    log("Failed to fetch settings, response code: " + this.status + " (" + this.statusText + ")");

                    setTimeout(function() {
                        SettingsManager.doRetrive(url);
                    }, 1000);
                }
            }
        };

        xml.open("GET", url, true);
        xml.send(null);
    }

    static doPush(url) {
        let xml = new XMLHttpRequest();

        xml.onreadystatechange = function(event) {
            if (this.readyState === XMLHttpRequest.DONE) {
                if (this.status === 200) {
                    let json = JSON.parse(this.responseText);
                    // console.log(json);

                    SettingsManager.display(json.updated_items, true);
                } else {
                    log("Failed to push settings, response code: " + this.status + " (" + this.statusText + ")");

                    setTimeout(function() {
                        SettingsManager.doPush(url);
                    }, 1000);
                }
            }
        };

        let jsonItems = [];
        for (let item of this.settingsItems) {
            jsonItems.push({
                key: item.key,
                value: item.value()
            });
        }

        xml.open("POST", url);
        xml.setRequestHeader("Content-Type", "application/json");
        xml.send(JSON.stringify(jsonItems));
	}

	static notify(updated = false) {
		let json = SettingsManager.cachedSettings;

		if (json != null) {
			SettingsManager.display(json, updated);
		}
	}

    static display(json, updated) {
        for (let item of this.settingsItems) {
            let value = item.defaultValue;
            let isDefault = true;

            for (let jsonItem of json) {
                if (jsonItem.key === item.key) {
                    value = jsonItem.value;
                    isDefault = false;
                    break;
                }
            }

			item.set(value, isDefault, updated);
			item.onReceive(value);
            item.visible(true);
        }
	}
	
	static pushChanges() {
		SettingsManager.push(API_ENDPOINT_SETTINGS_PUSH);
	}

}