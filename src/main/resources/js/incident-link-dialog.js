let $incidentLinkDialog = {
    config: {
        pagesElementId: "sp-link-page",
        incidentsElementId: "sp-link-incident",
        linkButtonId: "incident-link-dialog-submit-button",
        linkFormContentId: "link-form-content",
        linkFormWarningId: "link-form-warning"
    },
    checkAccess: function () {
        $pluginCommon
            .checkAccess(
                () => {
                    $("#" + $incidentLinkDialog.config.linkFormWarningId).hide()
                    $("#" + $incidentLinkDialog.config.linkFormContentId).show();
                    $("#" + $incidentLinkDialog.config.linkButtonId).show();
                    $incidentLinkDialog.loadPages();
                },
                () => {
                    $("#" + $incidentLinkDialog.config.linkFormContentId).hide();
                    $("#" + $incidentLinkDialog.config.linkButtonId).hide();
                    $("#" + $incidentLinkDialog.config.linkFormWarningId).show()
                }
            );
    },
    loadPages : function() {
        $statuspage.pages().then(function(pages) {
            let options_str = "";
            pages.forEach( function(page) {
                options_str += '<option value="' + page.id + '">' + page.name + '</option>';
            });
            $('#' + $incidentLinkDialog.config.pagesElementId)[0].innerHTML = options_str;
            if ($("#sp-link-page" + " option").length == 0) {
                $("#sp-link-page").auiSelect2("val", "");
            } else {
                $("#sp-link-page").trigger('change');
            }
            $incidentLinkDialog.loadIncidents($('#' + $incidentLinkDialog.config.pagesElementId).val());
        }).catch(function(error) {
            AJS.log("[load pages] service call error: ");
            AJS.log(error);
        });
    },
    loadIncidents : function(pageId) {
        $statuspage.incidents(pageId, true).then(function(incidents) {
            let options_str = "";
            incidents.forEach( function(page) {
                options_str += '<option value="' + page.id + '">' + page.name + '</option>';
            });
            $('#' + $incidentLinkDialog.config.incidentsElementId)[0].innerHTML = options_str;
            if ($("#sp-link-incident" + " option").length == 0) {
                $("#sp-link-incident").auiSelect2("val", "");
            } else {
                $("#sp-link-incident").trigger('change');
            }

            $pluginCommon.buttonIdle($incidentLinkDialog.config.linkButtonId);
        }).catch(function(error) {
            AJS.log("[load incidents] service call error: ");
            AJS.log(error);
        });
    }
}

AJS.$(function () {
    // AJS.log("incidentLinkDialog loaded");
    $("#page-location").val($(location).attr('href'));
});
