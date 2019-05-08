// First static example
var first_attitude = $.flightIndicator('#first_attitude', 'attitude', {
    size: 350,
    roll: 8,
    pitch: 3,
    showBox: true
});
// Dynamic examples
var attitude = $.flightIndicator('#attitude', 'attitude', {
    roll: 50,
    pitch: -20,
    size: 200,
    showBox: true
});
var heading = $.flightIndicator('#heading', 'heading', {
    heading: 150,
    showBox: true
});
var variometer = $.flightIndicator('#variometer', 'variometer', {
    vario: -5,
    showBox: true
});
var airspeed = $.flightIndicator('#airspeed', 'airspeed', {
    showBox: false
});
var altimeter = $.flightIndicator('#altimeter', 'altimeter');
var turn_coordinator = $.flightIndicator('#turn_coordinator', 'turn_coordinator', {
    turn: 0
});
// Update at 20Hz
var increment = 0;
setInterval(function() {
    // Airspeed update
    airspeed.setAirSpeed(80 + 80 * Math.sin(increment / 10));
    // Attitude update
    attitude.setRoll(30 * Math.sin(increment / 10));
    attitude.setPitch(50 * Math.sin(increment / 20));
    // Altimeter update
    altimeter.setAltitude(10 * increment);
    altimeter.setPressure(1000 + 3 * Math.sin(increment / 50));
    increment++;
    // TC update - note that the TC appears opposite the angle of the attitude indicator, as it mirrors the actual wing up/down position
    turn_coordinator.setTurn((30 * Math.sin(increment / 10)) * -1);
    // Heading update
    heading.setHeading(increment);
    // Vario update
    variometer.setVario(2 * Math.sin(increment / 10));
}, 50);