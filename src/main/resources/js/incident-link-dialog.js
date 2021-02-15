let $incidentLinkDialog = {
    loadPages : function(pagesElement, incidentsElement) {
        $statuspage.pages().then(function(pages) {
            let options_str = "";
            pages.forEach( function(page) {
                options_str += '<option value="' + page.id + '">' + page.name + '</option>';
            });
            $('#' + pagesElement)[0].innerHTML = options_str;
            if ($("#sp-link-page" + " option").length == 0) {
                $("#sp-link-page").auiSelect2("val", "");
            } else {
                $("#sp-link-page").trigger('change');
            }
            $incidentLinkDialog.loadIncidents($('#' + pagesElement).val(), incidentsElement);
        }).catch(function(error) {
            AJS.log("service call error: ");
            AJS.log(error);
        });
    },
    loadIncidents : function(pageId, element) {
        $statuspage.incidents(pageId, true).then(function(incidents) {
            let options_str = "";
            incidents.forEach( function(page) {
                options_str += '<option value="' + page.id + '">' + page.name + '</option>';
            });
            $('#' + element)[0].innerHTML = options_str;
            if ($("#sp-link-incident" + " option").length == 0) {
                $("#sp-link-incident").auiSelect2("val", "");
            } else {
                $("#sp-link-incident").trigger('change');
            }

            $pluginCommon.buttonIdle('incident-link-dialog-submit-button');
        }).catch(function(error) {
            AJS.log("service call error: ");
            AJS.log(error);
        });
    }
}

AJS.$(function () {
    // AJS.log("incidentLinkDialog loaded");
    $("#page-location").val($(location).attr('href'));
});
