var spin = new TimelineMax();
var loading = new TimelineMax({
	repeat : -1
});

spin.to($('.blade'), 0.6, {
    rotation : 360,
    repeat : -1,
    transformOrigin : '50% 50%',
    ease : Linear.easeNone
});

loading.to($('.loadingText'), 1, {
    opacity : 0,
    ease : Linear.easeNone
}).to($('.loadingText'), 1, {
    opacity : 1,
    ease : Linear.easeNone
});

$(window).on('load', function() {
	$(".se-pre-con").fadeOut("slow");
});