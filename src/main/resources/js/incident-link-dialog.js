jQuery(function () {
    // console.log("incidentLinkDialog loaded");
});
let $incidentLinkDialog = {

    loadPages : function(pagesElement, incidentsElement, wheelElement) {
        // console.log("incidentLinkDialog.loadPages");
        const pages = $statuspage.pages();
        // console.log(pages);
        let options_str = "";
        pages.forEach( function(page) {
            options_str += '<option value="' + page.id + '">' + page.name + '</option>';
        });
        $('#' + pagesElement)[0].innerHTML = options_str;
        this.loadIncidents($('#' + pagesElement).val(), incidentsElement, wheelElement);
    },

    loadIncidents : function(pageId, element, wheelElement) {
        // console.log("incidentLinkDialog.loadIncidents for " + pageId);
        $('#' + wheelElement).show();
        const incidents = $statuspage.incidents(pageId, true);
        // console.log(incidents);
        let options_str = "";
        incidents.forEach( function(page) {
            options_str += '<option value="' + page.id + '">' + page.name + '</option>';
        });
        $('#' + element)[0].innerHTML = options_str;
        $('#' + wheelElement).hide();
    },

    test : function() {
        // console.log("$incidentLinkDialog: test");
    }
}
