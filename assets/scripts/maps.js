var mapper;

var position = [47.1920857, 2.3766726];
var map;
var marker;
var pictureMarker;

function initialize() {
    var latlng = new google.maps.LatLng(position[0], position[1]);

    map = new google.maps.Map(document.getElementById("map-canvas"), {
        zoom: 16,
        center: latlng,
        mapTypeId: google.maps.MapTypeId.HYBRID,
        keyboardShortcuts: false //,
            //	    scrollwheel : false,
            //	    gestureHandling : "none",
            //	    panControl : false,

        /* The enabled/disabled state of the Fullscreen control. */
        //	    fullscreenControl : false,

        /* The enabled/disabled state of the Zoom control. */
        //	    zoomControl : false
    });

    mapper = new Mapper(map);

    marker = new google.maps.Marker({
        position: latlng,
        map: map,
        icon: {
            url: "./assets/images/drone-marker.png",
            scaledSize: new google.maps.Size(50, 50),
            origin: new google.maps.Point(0, 0),
            anchor: new google.maps.Point(25, 50)
        }
    });

    pictureMarker = new google.maps.Marker({
        position: latlng,
        map: map,
        icon: {
            url: "https://www.pilotpen.fr/pub/media/catalog/product/cache/image/755x566/beff4985b56e3afdbeabfc89641a4582/4/9/4902505511097-4902505511097_zoom_01.jpg",
            scaledSize: new google.maps.Size(50, 50),
            origin: new google.maps.Point(0, 0),
            anchor: new google.maps.Point(25, 50)
        }
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

    DroneSocket.subscribe(["flight.point.new"], function(identifier, json) {
        let latLng = new google.maps.LatLng(json.position.latitude, json.position.longitude);

        map.setCenter(latLng);
        marker.setPosition(latLng);

        mapper.appendFlightPlanCoordinates(latLng);

        console.log("Flight: received new position (lat/lon): " + latLng.lat() + "/" + latLng.lng() + " (" + new Date(json.position.time) + ")");
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