let $statusPageAdmin = {
    updateConfig: function (){
        let projects = $statusPageCommon.getSelectValuesString("projects");
        let roles = $statusPageCommon.getSelectValuesString("roles");
        let field_name = AJS.$("#custom-field-name")[0].value;

        // AJS.log("~~~ SAVING CONFIGURATION:");
        // AJS.log("       projects: " + projects);
        // AJS.log("       roles   : " + roles);
        // AJS.log("       field_id: " + field_name);
        // AJS.log("~~~~~~~~~~~~~~~~~~~~~~~~~~");

        AJS.$.ajax({
            url: AJS.contextPath() + "/rest/ws-slink-statuspage/1.0/admin",
            type: "PUT",
            contentType: "application/json",
            data: '{ "projects": "' + projects + '", "roles": "' + roles + '", "custom_field": "' + field_name + '"}',
            processData: false
        }).done(function () {
            JIRA.Messages.showSuccessMsg("configuration saved")
        }).error(function (error, message) {
            // AJS.log("---------------------------------------------------");
            // AJS.log(error);
            // AJS.log("---------------------------------------------------");
            // AJS.log(message);
            // AJS.log("---------------------------------------------------");
            JIRA.Messages.showErrorMsg("could not save configuration: <br><br>" + error.responseText)
        });
    }
}
AJS.$(function () {
    // console.log("[STATUSPAGE ADMIN JS LOADED]");
});
