let $statusPagePluginCommon = {
     config: {
         restBaseUrl: "/rest/ws-slink-statuspage/1.0/api"
     }
    ,status_values: [
         "operational",
         "degraded_performance",
         "partial_outage",
         "major_outage",
         "under_maintenance"
     ]

    ,getComponentStatusButtonClass: function(status) {
        if (status == 'operational')
            return "aui-iconfont-check-circle";
        else if (status == 'degraded_performance')
            return "aui-iconfont-devtools-task-disabled";
        else if (status == 'partial_outage')
            return "aui-iconfont-failed-build";
        else if (status == 'major_outage')
            return "aui-iconfont-remove";
        else if (status == 'under_maintenance')
            return "aui-iconfont-info-circle";
        else
            return "";
    }
    ,getComponents: function(state) {
        let result = [];
        $(".component-name").not(".removed").each(function() {
            $(this).parent().find("." + state + ".selected").each(function (){
                result.push({"id": $(this).parent().attr("id"), "name": $(this).parent().find(".component-name").text(), "status": state});
            });
        });
        return result;
    }
    ,getRemovedComponents: function() {
        let result = [];
        $(".component-name.removed").each(function() {
            result.push({"id": $(this).parent().attr("id"), "name": $(this).parent().find(".component-name").text(), "status": "na"});
        });
        return result;
    }
    ,getComponentsConfig: function() {
        let result = {};
        $statusPagePluginCommon.status_values.forEach(function(item, index) {
            result[item] = $statusPagePluginCommon.getComponents(item);
        })
        result["remove"] = $statusPagePluginCommon.getRemovedComponents();
        return result;
    }
    ,buttonBusy: function(buttonId, doDisable) {
        let buttonObject = AJS.$('#' + buttonId)[0];
        try {
            if (doDisable)
                buttonObject.disabled = true;
            if (!buttonObject.isBusy())
                buttonObject.busy();
        } catch (error) {
            AJS.log("could not set busy state: " + error)
            AJS.log(buttonObject);
        }
    }
    ,buttonIdle: function(buttonId) {
        let buttonObject = AJS.$('#' + buttonId)[0];
        try {
            buttonObject.disabled = false;
            if (buttonObject.isBusy())
                buttonObject.idle();
        } catch (error) {
            AJS.log("could not set idle state: " + error)
            AJS.log(buttonObject);
        }
    }
    ,accessQuery: async function() {
        return await jQuery.get(AJS.contextPath() + $statusPagePluginCommon.config.restBaseUrl + "/access")
    }
    ,checkAccess: function(fOk, fNok) {
        $statusPagePluginCommon.accessQuery().then(function(result) {
            // console.log("----> access check ok: " + JSON.stringify(result));
            try {
                fOk();
            } catch (error) {
                AJS.log("----> check ok; fOk error: " + JSON.stringify(error));
            }
        }).catch(function(error) {
            // console.log("----> access check error: " + JSON.stringify(error));
            fNok();
        });
    }

    ,incidentsQuery: async function(issueKey) {
        return await jQuery.get(AJS.contextPath() + $statusPagePluginCommon.config.restBaseUrl + "/incident/" + issueKey);
    }
    ,componentsQuery: async function(issueKey, incident) {
        return await jQuery.get(AJS.contextPath() + $statusPagePluginCommon.config.restBaseUrl + "/components?issueKey=" + issueKey + "&pageId=" + incident.page.id);
    }

    ,setImpact: function(impact, tabPanelBlockId, glancePanelBlockId) {
        $(".tab-panel #" + tabPanelBlockId + " div").removeClass("selected");
        $(".tab-panel .incident-impact-" + impact).addClass("selected");
        $(".tab-panel .incident-impact-" + impact).addClass("selected");

        $("#" + glancePanelBlockId)[0].classList.forEach(function(c) {
            $("#" + glancePanelBlockId).removeClass(c)
        });
        $("#" + glancePanelBlockId).html(impact);
        $("#" + glancePanelBlockId).addClass("incident-impact")
        $("#" + glancePanelBlockId).addClass("incident-impact-" + impact)
        $("#" + glancePanelBlockId).addClass("selected")
    }
    ,setStatus: function(status, tabPanelBlockId, glancePanelBlockId) {
        $(".tab-panel #" + tabPanelBlockId + " div").removeClass("selected");
        $(".tab-panel .incident-status-" + status).addClass("selected");

        $("#" + glancePanelBlockId)[0].classList.forEach(function(c) {
            $("#" + glancePanelBlockId).removeClass(c)
        });
        $("#" + glancePanelBlockId).html(status);
        $("#" + glancePanelBlockId).addClass("incident-status")
        $("#" + glancePanelBlockId).addClass("incident-status-" + status)
    }
    ,setPageName: function(value, tabPanelBlockId, glancePanelBlockId) {
        $(".tab-panel #" + tabPanelBlockId + " a").html(value);
        $("#" + glancePanelBlockId + " a").html(value);
    }
    ,setIncidentTitle: function(value, tabPanelBlockId, glancePanelBlockId) {
        $(".tab-panel #" + tabPanelBlockId + " a").html(value);
        $("#" + glancePanelBlockId + " a").html(value);
    }
    ,getSelectValuesString: function (element) {
        let result = "";
        let arr = AJS.$("#" + element + " option:selected");
        for (let i = 0; i < arr.length; i++) {
            result += arr[i].value;
            if (i < arr.length - 1)
                result += ",";
        }
        return result;
    }
}

AJS.$(function () {
    // https://developer.atlassian.com/server/jira/platform/displaying-content-in-a-dialog-in-jira/

    // AJS.log("[STATUSPAGE PLUGIN JS LOADED]");

    JIRA.Dialogs.incidentLinkDialog = new JIRA.FormDialog({
        id: "incident-link-dialog",
        trigger: "#incident-link",
        ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
        width:450,
        onSuccessfulSubmit : function() {},
        onDialogFinished : function() {},
        autoClose : true
    });

    JIRA.Dialogs.incidentUnlinkDialog = new JIRA.FormDialog({
        id: "incident-unlink-dialog",
        trigger: "#incident-unlink",
        ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
        width:450,
        onSuccessfulSubmit : function() {},
        onDialogFinished : function() {},
        autoClose : true
    });

    JIRA.Dialogs.incidentCreateDialog = new JIRA.FormDialog({
        id: "incident-create-dialog",
        trigger: "#incident-create",
        ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
        width:800,
        onSuccessfulSubmit : function() {},
        onDialogFinished : function() {},
        autoClose : true
    });

});
