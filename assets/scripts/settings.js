class SettingsItem {
	
	constructor(baseDivId, key, defaultValue, getHandler, setHandler) {
		this.baseDiv = document.getElementById(baseDivId);
		this.key = key;
		this.defaultValue = defaultValue;
		this.getHandler = getHandler;
		this.setHandler = setHandler;
	}
	
	set(value, isDefault, isChanged) {
		return this.setHandler(this.baseDiv, value, isDefault, isChanged);
	}
	
	value() {
		return this.getHandler(this.baseDiv);
	}
	
	visible(state) {
		this.baseDiv.style.visibility = state ? "visible" : "hidden";
	}
	
}

class SettingsManager {
	
	constructor() {
		this.settingsItems = [];
	}
	
	add(settingsItem) {
		this.settingsItems.push(settingsItem);
	}
	
	retrive(url) {
		for (let item of this.settingsItems) {
			item.visible(false);
		}
		
		this.doRetrive(url);
	}
	
	push(url) {
		this.doPush(url)
	}
	
	doRetrive(url) {
		let manager = this;
		let xml = new XMLHttpRequest();
		
		xml.onreadystatechange = function(event) {
		    if (this.readyState === XMLHttpRequest.DONE) {
		        if (this.status === 200) {
		        	let json = JSON.parse(this.responseText);
		        	console.log(json);
		        	
		        	manager.display(json, false);
		        } else {
		            log("Failed to fetch settings, response code: " + this.status + " (" + this.statusText + ")");
		            
		            setTimeout(function(){
		            	manager.doRetrive(url);
		            }, 1000);
		        }
		    }
		};

		xml.open("GET", url, true);
		xml.send(null);
	}
	
	doPush(url) {
		let manager = this;
		let xml = new XMLHttpRequest();
		
		xml.onreadystatechange = function(event) {
		    if (this.readyState === XMLHttpRequest.DONE) {
		        if (this.status === 200) {
		        	let json = JSON.parse(this.responseText);
		        	console.log(json);
		        	
		        	manager.display(json, true);
		        } else {
		            log("Failed to push settings, response code: " + this.status + " (" + this.statusText + ")");
		            
		            setTimeout(function(){
		            	manager.doPush(url);
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
	
	display(json, updated) {
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
			item.visible(true);
		}
	}
	
}

var settingsManager = new SettingsManager();

function pushChanges() {
	settingsManager.push(API_ENDPOINT_SETTINGS_PUSH);
}

settingsManager.add(new SettingsItem("google-api-key-container", "googlemap.api.key", "azertyuiopqsdfghjklmwxcvbn", function(div) {
	return div.getElementsByTagName("input")[0].value;
}, function(div, value, isDefault, isChanged) {
	if (!isChanged) {
		div.getElementsByTagName("input")[0].value = value;
	}
}));

settingsManager.retrive(API_ENDPOINT_SETTINGS);