var map;

class DroneMap {

    static initialize() {
        DroneMap.map = map = new google.maps.Map(document.getElementById("map-canvas"), {
            zoom: 16,
            center: new google.maps.LatLng(MAP_START_POSITION[0], MAP_START_POSITION[1]),
            mapTypeId: google.maps.MapTypeId.HYBRID//,
            //keyboardShortcuts: false,
            //scrollwheel: false,
            //gestureHandling: "none",
            //panControl: false,
    
            /* The enabled/disabled state of the Fullscreen control. */
            //fullscreenControl: false,
    
            /* The enabled/disabled state of the Zoom control. */
            //zoomControl: false
        });

        DroneMap.MARKERS = {
            "DRONE": new google.maps.Marker({
                //position: latlng,
                map: DroneMap.map,
                icon: {
                    url: "./assets/images/drone-marker.png",
                    scaledSize: new google.maps.Size(50, 50),
                    origin: new google.maps.Point(0, 0),
                    anchor: new google.maps.Point(25, 50)
                }
            }),
            "PICTURE": new google.maps.Marker({
                //position: latlng,
                map: DroneMap.map,
                icon: {
                    url: "https://www.pilotpen.fr/pub/media/catalog/product/cache/image/755x566/beff4985b56e3afdbeabfc89641a4582/4/9/4902505511097-4902505511097_zoom_01.jpg",
                    scaledSize: new google.maps.Size(50, 50),
                    origin: new google.maps.Point(0, 0),
                    anchor: new google.maps.Point(25, 50)
                }
            })
        };

        DroneMap.initializeFlighPlan();
        DroneMap.registerListeners();
        DroneMap.subscribeToSocket();
    }

    static initializeFlighPlan() {
        DroneMap.flightPath = new google.maps.Polyline({
            path: [],
            geodesic: true,
            strokeColor: '#FF0000',
            strokeOpacity: 1.0,
            strokeWeight: 2
        });

        DroneMap.flightPath.setMap(DroneMap.map);
    }

    static registerListeners() {
        google.maps.event.addListener(DroneMap.map, 'click', function(event) {
            DroneMap.map.setCenter(event.latLng);
            DroneMap.MARKERS.DRONE.setPosition(event.latLng);

            DroneMap.appendFlightPlanCoordinates(event.latLng);
        });

        google.maps.event.addListener(DroneMap.map, 'keypress', function(event) {
            event = event || window.event;

            console.log(event.keyCode);
        });
    }

    static subscribeToSocket() {
        DroneSocket.subscribe(["flight.point.new"], function(identifier, json) {
            let latLng = new google.maps.LatLng(json.position.latitude, json.position.longitude);

            DroneMap.map.setCenter(latLng);
            DroneMap.MARKERS.DRONE.setPosition(latLng);

            DroneMap.appendFlightPlanCoordinates(latLng);

            console.log("Flight: received new position (lat/lon): " + latLng.lat() + "/" + latLng.lng() + " (" + new Date(json.position.time) + ")");
        });
    }

    static appendFlightPlanCoordinates(latLng) {
        DroneMap.flightPath.getPath().push(latLng);

        console.log("Added point to flight plan (" + latLng.lat() + " , " + latLng.lng() + ")");
    }

    static attachGoogleMap(map) {
        DroneMap.map = map;
    }

    static displayCoordinates(position) {
        var lat = position.lat().toFixed(4);
        var lng = position.lng().toFixed(4);

        console.log("Latitude: " + lat + " // Longitude: " + lng);
    }

}