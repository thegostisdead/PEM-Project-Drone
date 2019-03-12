var mapper;

var position = [ 47.1920857, 2.3766726 ];
var map;

function initialize() {
	var latlng = new google.maps.LatLng(position[0], position[1]);

	map = new google.maps.Map(document.getElementById("map-canvas"), {
	    zoom : 16,
	    center : latlng,
	    mapTypeId : google.maps.MapTypeId.HYBRID,
	    keyboardShortcuts : false,
	    scrollwheel : false,
	    gestureHandling : "none",
	    panControl : false,

	    /* The enabled/disabled state of the Fullscreen control. */
	    fullscreenControl : false,

	    /* The enabled/disabled state of the Zoom control. */
	    zoomControl : false
	});
	
	mapper = new Mapper(map);

	let marker = new google.maps.Marker({
	    position : latlng,
	    map : map,
	    icon : "./assets/images/drone.png"
	});

	google.maps.event.addListener(map, 'click', function(event) {
		map.setCenter(event.latLng);
		marker.setPosition(event.latLng);
		
		mapper.appendFlightPlanCoordinates(event.latLng);
	});

	google.maps.event.addListener(map, 'keypress', function(event) {
		event = event || window.event;

		console.log(event.keyCode);
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

class Mapper {
	
	constructor(map) {
		this.map = map;
		
		this.initializeFlighPlan();
	}
	
	initializeFlighPlan() {
		this.flightPath = new google.maps.Polyline({
	          path: this.flightPlanCoordinates,
	          geodesic: true,
	          strokeColor: '#FF0000',
	          strokeOpacity: 1.0,
	          strokeWeight: 2
        });

	    this.flightPath.setMap(this.map);
	}
	
	appendFlightPlanCoordinates(latLng) {
		this.flightPath.getPath().push(latLng);
		
		console.log("Added point to flight plan (" + latLng.lat() + " , " + latLng.lng() + ")");
	}
	
	
}