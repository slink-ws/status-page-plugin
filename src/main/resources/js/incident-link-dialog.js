jQuery(function () {
    console.log("incidentLinkDialog loaded");
});
let $incidentLinkDialog = {

    loadPages : function(pagesElement, incidentsElement) {
        console.log("incidentLinkDialog.loadPages");
        const pages = $statuspage.pages();
        console.log(pages);
        let options_str = "";
        pages.forEach( function(page) {
            options_str += '<option value="' + page.id + '">' + page.name + '</option>';
        });
        $('#' + pagesElement)[0].innerHTML = options_str;
        this.loadIncidents($('#' + pagesElement).val(), incidentsElement);
    },

    loadIncidents : function(pageId, element) {
        console.log("incidentLinkDialog.loadIncidents for " + pageId);
        const incidents = $statuspage.incidents(pageId, true);
        console.log(incidents);
        let options_str = "";
        incidents.forEach( function(page) {
            options_str += '<option value="' + page.id + '">' + page.name + '</option>';
        });
        $('#' + element)[0].innerHTML = options_str;
    },

    test : function() {
        console.log("$incidentLinkDialog: test");
    }
}
