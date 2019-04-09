window.addEventListener("load", function(event) {
    i18n.initialize();

    DroneSocket.initialize();
    DroneSocket.connect();

    DroneApi.initialize();

    Statistics.initialize();

    Gallery.initialize();
    Gallery.fillGallery();

    DroneMap.initialize();

    /* Modals */
    $(".modal-trigger").click(function(event) {
        event.preventDefault();

        let dataModal = $(this).attr("data-modal");

        $("#" + dataModal).css({
            "display": "block"
        });

        // $("body").css({"overflow-y": "hidden"}); //Prevent double scrollbar.
    });

    $(".close-modal, .modal-sandbox").click(function() {
        $(".modal").css({
            "display": "none"
        });

        // $("body").css({"overflow-y": "auto"}); //Prevent double scrollbar.
    });

    DroneSocket.addListener("onopen", function() {
        document.getElementById("modal-disconnected").style.display = "none";
    });
    DroneSocket.addListener("onerror", function(error) {
        document.getElementById("modal-disconnected").style.display = "block";
    });

    DroneDebug.tryToRemoveGoogleMapDialog();
});