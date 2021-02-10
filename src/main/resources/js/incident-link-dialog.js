let $incidentLinkDialog = {
    loadPages : function(pagesElement, incidentsElement, wheelElement) {
        $statuspage.pages().then(function(pages) {
            let options_str = "";
            pages.forEach( function(page) {
                options_str += '<option value="' + page.id + '">' + page.name + '</option>';
            });
            $('#' + pagesElement)[0].innerHTML = options_str;
            $incidentLinkDialog.loadIncidents($('#' + pagesElement).val(), incidentsElement, wheelElement);
        }).catch(function(error) {
            AJS.log("service call error: ");
            AJS.log(error);
        });
    },
    loadIncidents : function(pageId, element, wheelElement) {
        $('#' + wheelElement).show();
        $statuspage.incidents(pageId, true).then(function(incidents) {
            let options_str = "";
            incidents.forEach( function(page) {
                options_str += '<option value="' + page.id + '">' + page.name + '</option>';
            });
            $('#' + element)[0].innerHTML = options_str;
            $('#' + wheelElement).hide();
            $pluginCommon.buttonIdle('incident-link-dialog-submit-button');
        }).catch(function(error) {
            AJS.log("service call error: ");
            AJS.log(error);
        });
    }
}

AJS.$(function () {
    AJS.log("incidentLinkDialog loaded");
    $("#page-location").val($(location).attr('href'));
});
