

/* google maps  */
/* $(document).ready(function () {
    var mapOptions = {
        zoom: 12,
        center: new google.maps.LatLng(-34.397, 150.644),
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        streetViewControl: false, // on disable le street view
        fullscreenControl: false // on remove le zoom
    }
    var map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
}); */

/* radar */
$( document ).ready(function() {
    $(function () {
	    var wh = ($(window).outerWidth() + 200),
	        $radar = $('#radar-svg'),
	        $radGradient = $('#radar-gradient'),
	        $radBeam = $('#radar-beam'),
	        deg = -90,
	        rad = (wh / 2); // = 621/2
	
	    $radar.attr('width', wh).attr('height', wh);
	    $radGradient.attr('y1', rad).attr('y2', rad);
	    $radBeam.attr('points', '750,0 450,0 ' + rad + ',' + rad);
	    $radar.css({
	        transform: 'rotate(' + deg + 'deg)'
	    });
    });
});