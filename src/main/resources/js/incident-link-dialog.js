jQuery(function () {
    // AJS.log("incidentLinkDialog loaded");
    $("#page-location").val($(location).attr('href'));
});
let $incidentLinkDialog = {

    loadPages : function(pagesElement, incidentsElement, wheelElement) {
        // AJS.log("incidentLinkDialog.loadPages");
        const pages = $statuspage.pages();
        // AJS.log(pages);
        let options_str = "";
        pages.forEach( function(page) {
            options_str += '<option value="' + page.id + '">' + page.name + '</option>';
        });
        $('#' + pagesElement)[0].innerHTML = options_str;
        this.loadIncidents($('#' + pagesElement).val(), incidentsElement, wheelElement);
    },

    loadIncidents : function(pageId, element, wheelElement) {
        // AJS.log("incidentLinkDialog.loadIncidents for " + pageId);
        $('#' + wheelElement).show();
        const incidents = $statuspage.incidents(pageId, true);
        // AJS.log(incidents);
        let options_str = "";
        incidents.forEach( function(page) {
            options_str += '<option value="' + page.id + '">' + page.name + '</option>';
        });
        $('#' + element)[0].innerHTML = options_str;
        $('#' + wheelElement).hide();
    },

    test : function() {
        // AJS.log("$incidentLinkDialog: test");
    }

}

