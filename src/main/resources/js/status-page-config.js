function sort_options(element) {
    var options = $('#' + element + ' option');
    var arr = options.map(function(_, o) { return { t: $(o).text(), v: o.value }; }).get();
    arr.sort(function(o1, o2) { return o1.t > o2.t ? 1 : o1.t < o2.t ? -1 : 0; });
    options.each(function(i, o) {
        o.value = arr[i].v;
        $(o).text(arr[i].t);
    });
}
function move_items(from, to, items) {
    for (let idx = 0; idx < items.length; idx++) {
        // AJS.log(items[idx].value + " -> " + items[idx].text + " : " + items[idx].selected);
        $('#' + to).append('<option value="' + items[idx].value + '">' + items[idx].text + '</option>');
        $('#' + from + ' option[value=' + items[idx].value + ']').remove();
        sort_options(to);
    }
}
function get_selected(items) {
    let result = [];
    for (let idx = 0; idx < items.length; idx++) {
        // AJS.log(idx + " : " + items[idx].value + " -> " + items[idx].text + " (" + items[idx].selected + ")");
        if (items[idx].selected) {
            result.push(items[idx]);
        }
    }
    return result;
}
function get_select_values_string(element) {
    let result = "";
    let arr = AJS.$("#" + element + " option");
    for(let i = 0; i < arr.length; i++) {
        result += arr[i].value;
        if (i < arr.length - 1)
            result += ",";
    }
    return result;
}

function config_add_mgmt_roles() {
    move_items('available-mgmt-roles', 'selected-mgmt-roles', get_selected($("#available-mgmt-roles option")));
}
function config_add_all_mgmt_roles() {
    move_items('available-mgmt-roles', 'selected-mgmt-roles', $("#available-mgmt-roles option"));
}
function config_remove_mgmt_roles() {
    move_items('selected-mgmt-roles', 'available-mgmt-roles', get_selected($("#selected-mgmt-roles option")));
}
function config_remove_all_mgmt_roles() {
    move_items('selected-mgmt-roles', 'available-mgmt-roles', $("#selected-mgmt-roles option"));
}

function config_add_view_roles() {
    move_items('available-view-roles', 'selected-view-roles', get_selected($("#available-view-roles option")));
}
function config_add_all_view_roles() {
    move_items('available-view-roles', 'selected-view-roles', $("#available-view-roles option"));
}
function config_remove_view_roles() {
    move_items('selected-view-roles', 'available-view-roles', get_selected($("#selected-view-roles option")));
}
function config_remove_all_view_roles() {
    move_items('selected-view-roles', 'available-view-roles', $("#selected-view-roles option"));
}

function config_update_config() {

    // let projects = get_select_values_string("selected-projects");
    let apikey     = AJS.$('input[name=apikey]').val();
    let project    = AJS.$('input[name=project]').val();
    let mgmt_roles = get_select_values_string("selected-mgmt-roles");
    let view_roles = get_select_values_string("selected-view-roles");

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
