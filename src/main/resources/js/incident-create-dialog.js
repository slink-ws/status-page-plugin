let $incidentCreateDialog = {

    componentStatuses: [],
    cachedComponents: {},
    config: {
        pagesElement       : 'sp-create-page',
        impactsElement     : 'sp-create-impact',
        headerElement      : 'components-header',
        componentsElement  : 'tab-components-list',
        createButtonId     : 'incident-create-dialog-submit-button',
        createFormContentId: 'create-form-content',
        createFormWarningId: 'create-form-warning'
    },
    checkAccess: function () {
        // console.log("----> create dialog check access");
        $statusPageCommon
            .checkAccess(
                () => {
                    // console.log("-----> create dialog ok! loading create page!")
                    $("#" + $incidentCreateDialog.config.createFormWarningId).hide()
                    $("#" + $incidentCreateDialog.config.createFormContentId).show();
                    $("#" + $incidentCreateDialog.config.createButtonId).show();
                    $incidentCreateDialog.loadDialog();
                },
                () => {
                    // console.log("-----> create dialog err! show warning!")
                    $("#" + $incidentCreateDialog.config.createFormContentId).hide();
                    $("#" + $incidentCreateDialog.config.createButtonId).hide();
                    $("#" + $incidentCreateDialog.config.createFormWarningId).show()
                }
            );
    },
    loadDialog: function () {
        $statusPageCommon.buttonBusy($incidentCreateDialog.config.createButtonId, true);
        $incidentCreateDialog.loadImpacts();
    },
    loadImpacts: function() {
        $statusPageCommon.buttonBusy($incidentCreateDialog.config.createButtonId, true);
        $statuspage.impacts().then(function(impacts) {
            let options_str = "";
            impacts.forEach( function(item) {
                options_str += '<option value="' + item + '">' + item + '</option>';
            });
            $('#' + $incidentCreateDialog.config.impactsElement)[0].innerHTML = options_str;
            $incidentCreateDialog.loadHeader();
        }).catch(function(error) {
            AJS.log("[load impacts] service call error: ");
            AJS.log(error);
        });
    },
    loadHeader : function() {
        $statusPageCommon.buttonBusy($incidentCreateDialog.config.createButtonId, true);
        $statuspage.componentStatuses().then(function(statuses) {
            $incidentCreateDialog.componentStatuses = statuses;
            let header_str = '<div class="component-name-header">&nbsp;</div>';
            header_str  += '<span id="components-header-reset"' +
                ' onclick="headerButtonClick(this)" ' +
                ' title="Set all components to actual state" ' +
                ' class="component-button component-header reset header aui-icon aui-icon-small aui-iconfont-refresh">' +
                'Reset' + '</span>';
            $incidentCreateDialog.componentStatuses.forEach( function(item) {
                // console.log(item);
                header_str  += '<span id="components-header-' + item.id + '"' +
                    ' onclick="headerButtonClick(this)" ' +
                    ' title="Set all components to \'' + item.title + '\' state" ' +
                    ' class="component-button component-header ' + item.id + ' header aui-icon aui-icon-small ' + $statusPageCommon.getComponentStatusButtonClass(item.id) + '">' +
                    item.title + '</span>';
            });
            header_str += '<div style="float: left; width: 10px;"></div>'
            $('#' + $incidentCreateDialog.config.headerElement)[0].innerHTML = header_str;
            $incidentCreateDialog.loadPages();
        }).catch(function(error) {
            AJS.log("[load header] service call error: ");
            AJS.log(error);
        });
    },
    loadPages : function() {
        $statusPageCommon.buttonBusy($incidentCreateDialog.config.createButtonId, true);
        $statuspage.pages().then(function(pages) {
            let options_str = "";
            pages.forEach( function(page) {
                options_str += '<option value="' + page.id + '">' + page.name + '</option>';
            });
            $('#' + $incidentCreateDialog.config.pagesElement)[0].innerHTML = options_str;
            $incidentCreateDialog.loadGroups();
        }).catch(function(error) {
            AJS.log("[load pages] service call error: ");
            AJS.log(error);
        });
    },
    loadGroups : function() {
        $statusPageCommon.buttonBusy($incidentCreateDialog.config.createButtonId, true);
        $('#' + $incidentCreateDialog.config.componentsElement).html("");
        $statuspage.groups($('#' + $incidentCreateDialog.config.pagesElement).val()).then(function(groups) {
            $incidentCreateDialog.loadComponents(groups);
        }).catch(function(error) {
            AJS.log("[load groups] service call error: ");
            AJS.log(error);
        });
    },
    loadComponents: function(groups) {
        $statusPageCommon.buttonBusy($incidentCreateDialog.config.createButtonId, true);
        $statuspage.components($('#' + $incidentCreateDialog.config.pagesElement).val()).then(function(components) {
            let groupedComponents = {};
            groupedComponents["---no-group---"] = {};
            groupedComponents["---no-group---"]["title"] = "root";
            groupedComponents["---no-group---"]["components"] = [];
            groups.forEach(function(group) {
                groupedComponents[group.id] = {}
                groupedComponents[group.id]["title"] = group.name;
                groupedComponents[group.id]["components"] = [];
            });
            components.forEach(function(component) {
                if (!component.group) {
                    let groupId = component.group_id;
                    if (undefined != groupId) {
                        groupedComponents[groupId]["components"].push(component);
                    } else {
                        groupedComponents["---no-group---"]["components"].push(component);
                    }
                }
            });
            // console.log(JSON.stringify(groupedComponents, null, 2));
            let components_str = "";
            for (let key in groupedComponents) {
                let group = groupedComponents[key];
                if (group.components && group.components.length > 0) {
                    components_str += '<div class="component" id=""><div class="component-name">' + group.title + '</div></div>';
                    group.components.forEach(function(component) {
                        components_str += '<div class="component" id="' + component.id + '">' +
                            '  <div class="component-name" title="' + component.name + '">&nbsp;&nbsp;' + component.name +'</div>';
                        components_str += $incidentCreateDialog.buttonHTML(
                            'aui-iconfont-refresh',
                            false,
                            component.id,
                            'reset',
                            'Set to actual component state'
                        );
                        $incidentCreateDialog.cachedComponents[component.id] = component;
                        $incidentCreateDialog.componentStatuses.forEach(function(status) {
                            components_str += $incidentCreateDialog.buttonHTML(
                                $statusPageCommon.getComponentStatusButtonClass(status.id),
                                false,
                                component.id,
                                status.id,
                                status.title
                            );
                        });
                        components_str += '<div style="float: left; width: 10px;"></div>'
                        components_str += '</div>';
                    });
                }
            }
            $('#' + $incidentCreateDialog.config.componentsElement).html(components_str);
            $statusPageCommon.buttonIdle($incidentCreateDialog.config.createButtonId);
        }).catch(function(error) {
            AJS.log("[load components] service call error: ");
            AJS.log(error);
        });
    },
    changeAllComponentsState : function(source) {
        let sourceId = source.id;
        let parentId = source.parentElement.id;
        let statusId = source.id.replace(source.parentElement.id + "-", "")

        // console.log("source id: " + sourceId);
        // console.log("parent id: " + parentId);
        // console.log("status id: " + statusId);
        if (source.id.includes('reset')) {
            if ($("#" + sourceId).hasClass("selected")) {
                $("#" + parentId).children("span").removeClass("selected");
                $(".component .component-button").removeClass("selected");
            } else {
                $(".component .component-button").removeClass("selected");
                $("#" + parentId).children("span").removeClass("selected");
                $("#" + sourceId).addClass("selected");
                $(".component").each(function (item, element) {
                    let component = $incidentCreateDialog.cachedComponents[$(element).attr("id")];
                    if (null != component && undefined != component) {
                        $("#" + component.id + "-" + component.status).addClass("selected");
                    }
                })
            }
        } else {
            let selected = $("#" + sourceId).hasClass("selected");
            $("#" + parentId).children("span").removeClass("selected");
            if (selected) {
                $("#" + sourceId).addClass("selected");
                $(".component .component-button").removeClass("selected");
            } else {
                // console.log("-----> check all components with " + statusId);
                $(".component .component-button").removeClass("selected");
                $(".component .component-button." + statusId).addClass("selected");
            }
        }
        this.updateComponentsConfig();
    },
    changeComponentState : function(source) {
        let selected = $("#" + source.id).hasClass("selected");
        $("#" + source.parentElement.id).children("span").removeClass("selected");

        if (source.id.includes('reset')) {
            $("#" + source.parentElement.id).find("." + $incidentCreateDialog.cachedComponents[source.parentElement.id].status).addClass("selected");
        } else {
            if (!selected) {
                $("#" + source.id).addClass("selected");
            }
        }
        this.updateComponentsConfig();
    },
    updateComponentsConfig : function() {
        let components = $statusPageCommon.getComponentsConfig();
        let componentsForUpdate = {};
        $statusPageCommon.status_values.forEach(function(item) {
            componentsForUpdate[item] = components[item].map(a => a.id);
        })
        $("#components-config").val(JSON.stringify(componentsForUpdate));
        // console.log($("#components-config").val());
    },
    buttonHTML: function (buttonClass, selected, componentId, statusId, title) {
        let result =
            '    <span id="' + componentId + '-' + statusId + '" ' +
            '          onclick="statusButtonClick(this)" ' +
            '          title="' + title + '" ' +
            '          class="component-button ' + statusId + ' ' + ((selected) ? 'selected' : '') +
            '                 aui-icon aui-icon-small ' + buttonClass + '" ' +
            '          >' + title + '</span>';
        return result;
    }
}

AJS.$(function () {
    // AJS.log("incidentCreateDialog loaded");
    $("#page-location").val($(location).attr('href'));
});
