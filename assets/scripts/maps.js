var position = [ 47.1920857, 2.3766726 ];
var map;
function initialize() {
	var latlng = new google.maps.LatLng(position[0], position[1]);

	map = new google.maps.Map(document.getElementById("map-canvas"), {
	    zoom : 16,
	    center : latlng,
	    mapTypeId : google.maps.MapTypeId.HYBRID,
	    keyboardShortcuts: false
	});

	marker = new google.maps.Marker({
	    position : latlng,
	    map : map,
	    icon : "./assets/images/drone.png",
	    title : "Latitude:" + position[0] + " | Longitude:" + position[1]
	});

	google.maps.event.addListener(map, 'click', function(event) {
		map.setCenter(new google.maps.LatLng( event.latLng.lat(), event.latLng.lng()));
	});

	google.maps.event.addListener(map, 'keypress', function(event) {
		event =  event || window.event;
		
		console.log(event.keyCode);
	});

	google.maps.event.addListener(map, 'mousemove', function(event) {
		var result = [ event.latLng.lat(), event.latLng.lng() ];
		transition(result);
		displayCoordinates(event.latLng);
	});
}

// Load google map
google.maps.event.addDomListener(window, 'load', initialize);

function displayCoordinates(pnt) {
	var lat = pnt.lat();
	lat = lat.toFixed(4);
	var lng = pnt.lng();
	lng = lng.toFixed(4);
	console.log("Latitude: " + lat + "  Longitude: " + lng);
}

var numDeltas = 100;
var delay = 10; // milliseconds
var i = 0;
var deltaLat;
var deltaLng;

function transition(result) {
	i = 0;
	deltaLat = (result[0] - position[0]) / numDeltas;
	deltaLng = (result[1] - position[1]) / numDeltas;
	moveMarker();
}

function moveMarker() {
	position[0] += deltaLat;
	position[1] += deltaLng;
	var latlng = new google.maps.LatLng(position[0], position[1]);
	marker.setTitle("Latitude:" + position[0] + " | Longitude:" + position[1]);
	marker.setPosition(latlng);
	if (i != numDeltas) {
		i++;
		setTimeout(moveMarker, delay);
	}
}