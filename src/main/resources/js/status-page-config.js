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
function config_update_config() {

    let apikey     = AJS.$('input[name=apikey]').val();
    let project    = AJS.$('input[name=project]').val();
    let mgmt_roles = get_select_values_string("managers");
    let view_roles = get_select_values_string("viewers");

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
        data: '{ "project": "' + project + '", "mgmt_roles": "' +  mgmt_roles + '", "view_roles": "' + view_roles + '", "apikey": "' + apikey + '" }',
        processData: false
    }).done(function () {
        JIRA.Messages.showSuccessMsg("configuration saved")
    }).error(function () {
        JIRA.Messages.showErrorMsg("could not save configuration")
    });
}
