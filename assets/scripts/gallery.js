class Gallery {
	
	static initialize() {
		Gallery.DIVS = {
			FLIGHTS : document.getElementById("gallery-flights"),
			PICTURES : document.getElementById("gallery-pictures")
		}
		
		Gallery.currentFlightLocalFile = null;
		Gallery.displayedFlightLocalFile = null;
		
        DroneSocket.addListener("onmessage", Gallery.onMessageSocketListener);
	}
	
	static onMessageSocketListener(json) {
		switch (json.identifier) {
			case "picture.download.finished": {
				if (Gallery.currentFlightLocalFile != null && Gallery.currentFlightLocalFile == Gallery.displayedFlightLocalFile) {
					Gallery.addPicture(json.data.file);
				}
				break;
			}
			
			case "flight.starting":
			case "flight.finished": {
				Gallery.fillGallery();
				break;
			}
		}
	}
	
	static fillGallery() {
		Gallery.DIVS.FLIGHTS.innerHTML = HTML_PART_LOADER;
		
		DroneApi.call(ENDPOINT_FLIGHTS, function(json, success) {
			console.log(json);
			
			if (!success) {
				log("Failed to fetch flights.");
				return;
			}
			
			let firstIsCurrent = json.current.active;
			let flights = [];
			
			if (firstIsCurrent) {
				let currentFlight = json.current.flight;
			
				flights.push(currentFlight);
				Gallery.currentFlightLocalFile = currentFlight.local_file;
			}
			flights.extend(json.all);
			console.log(flights);
			
			let html = "";
			for (let index = 0; index < flights.length; index++) {
				let flight = flights[index];
				console.log(index);
				
				let active = index == 0 && firstIsCurrent;
				let mutedClassPart = !active ? " class=\"text-muted\"" : "";
				let durationPart = flight.end == 0 ? "toujours en cours" : formatDate(new Date(flight.end - flight.start));
				
				html += "<a data-flight=\"" + flight.local_file + "\" onclick=\"Gallery.selectFlight(this);\" href=\"#\" class=\"gallery-flight-item list-group-item list-group-item-action\">";
				html += "	<div class=\"d-flex w-100 justify-content-between\">\n";
				html += "		<h5 class=\"mb-1\">" + flight.name + "</h5>\n";
				html += "		<small" + mutedClassPart + ">" + new Date(flight.start).toLocaleString() + "</small>\n";
				html += "	</div>\n";
				html += "	<p class=\"mb-1\">Durée de vol : " + durationPart + ".</p>";
				// html += " <small" + mutedClassPart + ">Nombre de photo prises:</small>\n";
				// html += " <span class=\"badge badge-primary badge-pill\">" + flight.pictures.length + "</span>\n";
				html += "</a>\n";
			}
			
			html += "<a data-flight=\"unknown\" onclick=\"Gallery.selectFlight(this);\" href=\"#\" class=\"gallery-flight-item list-group-item list-group-item-action\">";
			html += "	<div class=\"d-flex w-100 justify-content-between\">\n";
			html += "		<h5 class=\"mb-1\">Autres</h5>\n";
			html += "	</div>\n";
			html += "</a>\n";
			
			Gallery.DIVS.FLIGHTS.innerHTML = html;
			
			let elements = Gallery.DIVS.FLIGHTS.getElementsByClassName("gallery-flight-item");
			console.log(elements);
			if (elements.length > 0) {
				Gallery.selectFlight(elements[0]);
			}
		});
	}
	
	static unselectAllFlights() {
		Gallery.displayedFlightLocalFile = null;
	
		let elements = Gallery.DIVS.FLIGHTS.getElementsByClassName("gallery-flight-item");
		
		for (let element of elements) {
			element.classList.remove("active");
		}
	}
	
	static selectFlight(div) {
		console.log(div);
		
		Gallery.unselectAllFlights();
		Gallery.DIVS.PICTURES.innerHTML = HTML_PART_LOADER;
		
		DroneApi.call(ENDPOINT_PICTURE_LIST, function(json, success) {
			console.log(json);
			
			if (!success) {
				log("Failed to fetch pictures list.");
				return;
			}
			
			Gallery.unselectAllFlights(); /* Just to be sure */
			
			div.classList.add("active");
			
			let flightLocalFile = Gallery.displayedFlightLocalFile = div.dataset.flight;
			let pictures = [];
			
			if (json[flightLocalFile] != undefined) {
				pictures.extend(json[flightLocalFile]);
			}
			
			console.log(flightLocalFile);
			
			Gallery.DIVS.PICTURES.innerHTML = "";
			
			if (pictures.length > 0) {
				for (let index = 0; index < pictures.length; index++) {
					let picture = pictures[index];
					
					Gallery.addPicture(picture);
				}
			} else {
				Gallery.DIVS.PICTURES.innerHTML = "Aucune image.";
			}			
		});
	}
	
	static addPicture(picture) {
		let html = Gallery.DIVS.PICTURES.innerHTML;
		
		if (html == "Aucune image.") {
			html = "";
		}
		
		let elementHtml = "";
		elementHtml += "<div class=\"picture-container\">\n";
		elementHtml += "	<img class=\"picture\" src=\"" + API_URL + picture.remote + "\">\n";
		elementHtml += "	<div class=\"middle\">\n";
		elementHtml += "		<a data-latitude=\"" + picture.position.latitude + "\" data-longitude=\"" + picture.position.longitude + "\" onclick=\"Gallery.showOnMap(this);\" href=\"#\">\n";
		elementHtml += "			<div class=\"text\">GPS</div>\n";
		elementHtml += "		</a>\n";
		elementHtml += "	</div>\n";
		elementHtml += "</div>\n";
		
		html = elementHtml + html;
		
		Gallery.DIVS.PICTURES.innerHTML = html;
	}
	
	static showOnMap(element) {
		let latitude = element.dataset.latitude;
		let longitude = element.dataset.longitude;
		
		// TODO
		map.setCenter(new google.maps.LatLng(latitude, longitude));
		marker.setPosition(new google.maps.LatLng(latitude, longitude));
		
		Gallery.display(false);
	}
	
	static display(state) {
		let modal = document.getElementById("modal-gallery");
		let display = "none";
		
		if (state === true) {
			display = "block";
		}
		
		modal.style.display = display;
	}
	
}