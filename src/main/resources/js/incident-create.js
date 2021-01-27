// alert("test");
jQuery(function () {

    new AJS.FormPopup({
        id: "incident-create-dialog",
        trigger: "#incident-create",
        ajaxOptions: {
            url: this.href,
            data: {
                decorator: "dialog",
                inline: "true"
            }
        },
        widthClass: "large"
    });

});