DroneSocket.initialize();
DroneSocket.connect();

Gallery.initialize();
Gallery.fillGallery();

/* Modals */
$(".modal-trigger").click(function(event) {
	event.preventDefault();
	
	let dataModal = $(this).attr("data-modal");
	
	$("#" + dataModal).css({
		"display" : "block"
	});
	
	// $("body").css({"overflow-y": "hidden"}); //Prevent double scrollbar.
});

$(".close-modal, .modal-sandbox").click(function() {
	$(".modal").css({
		"display" : "none"
	});
	
	// $("body").css({"overflow-y": "auto"}); //Prevent double scrollbar.
});