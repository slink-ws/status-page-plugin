let $statusPageConfig = {
    updateConfig: function() {
        let apikey = AJS.$('input[name=apikey]').val();
        let project = AJS.$('input[name=project]').val();
        let mgmt_roles = $statusPagePluginCommon.getSelectValuesString("managers");
        let view_roles = $statusPagePluginCommon.getSelectValuesString("viewers");

        // AJS.log("~~~ SAVING CONFIGURATION:");
        // AJS.log("       project   : " + project);
        // AJS.log("       api key   : " + apikey);
        // AJS.log("       m.roles   : " + mgmt_roles);
        // AJS.log("       v.roles   : " + view_roles);
        // AJS.log("~~~~~~~~~~~~~~~~~~~~~~~~~~")
        AJS.$.ajax({
            url: AJS.contextPath() + "/rest/ws-slink-statuspage/1.0/config",
            type: "PUT",
            contentType: "application/json",
            data: '{ "project": "' + project + '", "mgmt_roles": "' + mgmt_roles + '", "view_roles": "' + view_roles + '", "apikey": "' + apikey + '" }',
            processData: false
        }).done(function () {
            JIRA.Messages.showSuccessMsg("configuration saved")
        }).error(function () {
            JIRA.Messages.showErrorMsg("could not save configuration")
        });
    }
}

AJS.$(function () {
    // console.log("[STATUSPAGE CONFIG JS LOADED]");
});
