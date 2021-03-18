function get_select_values_string(element) {
    let result = "";
    let arr = AJS.$("#" + element + " option:selected");
    for(let i = 0; i < arr.length; i++) {
        result += arr[i].value;
        if (i < arr.length - 1)
            result += ",";
    }
    return result;
}
function admin_update_config() {

    let projects   = get_select_values_string("projects");
    let roles      = get_select_values_string("roles");
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
        data: '{ "projects": "' + projects + '", "roles": "' +  roles + '", "custom_field": "' + field_name + '"}',
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
