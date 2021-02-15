jQuery(function () {
    $("#page-location").val($(location).attr('href'));
});
let $incidentCreateDialog = {

    componentStatuses: [],
    cachedComponents: {},
    params: {},
    loadDialog: function () {
        $pluginCommon.buttonBusy('incident-create-dialog-submit-button');
        this.loadImpacts();
    },
    loadImpacts: function() {
        $pluginCommon.buttonBusy('incident-create-dialog-submit-button');
        $statuspage.impacts().then(function(impacts) {
            let options_str = "";
            impacts.forEach( function(item) {
                options_str += '<option value="' + item + '">' + item + '</option>';
            });
            $('#' + $incidentCreateDialog.params['impactsElement'])[0].innerHTML = options_str;
            $incidentCreateDialog.loadHeader();
        }).catch(function(error) {
            AJS.log("[load impacts] service call error: ");
            AJS.log(error);
        });
    },
    loadHeader : function() {
        $pluginCommon.buttonBusy('incident-create-dialog-submit-button');
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
                    ' class="component-button component-header ' + item.id + ' header aui-icon aui-icon-small ' + $pluginCommon.getComponentStatusButtonClass(item.id) + '">' +
                    item.title + '</span>';
            });
            header_str += '<div style="float: left; width: 10px;"></div>'
            $('#' + $incidentCreateDialog.params['headerElement'])[0].innerHTML = header_str;
            $incidentCreateDialog.loadPages();
        }).catch(function(error) {
            AJS.log("[load header] service call error: ");
            AJS.log(error);
        });
    },
    loadPages : function() {
        $pluginCommon.buttonBusy('incident-create-dialog-submit-button');
        $statuspage.pages().then(function(pages) {
            let options_str = "";
            pages.forEach( function(page) {
                options_str += '<option value="' + page.id + '">' + page.name + '</option>';
            });
            $('#' + $incidentCreateDialog.params['pagesElement'])[0].innerHTML = options_str;
            $incidentCreateDialog.loadGroups();
        }).catch(function(error) {
            AJS.log("[load pages] service call error: ");
            AJS.log(error);
        });
    },
    loadGroups : function() {
        $pluginCommon.buttonBusy('incident-create-dialog-submit-button');
        $('#' + $incidentCreateDialog.params['componentsElement']).html("");
        $statuspage.groups($('#' + $incidentCreateDialog.params['pagesElement']).val()).then(function(groups) {
            $incidentCreateDialog.loadComponents(groups);
        }).catch(function(error) {
            AJS.log("[load groups] service call error: ");
            AJS.log(error);
        });
    },
    loadComponents: function(groups) {
        $pluginCommon.buttonBusy('incident-create-dialog-submit-button');
        $statuspage.components($('#' + $incidentCreateDialog.params['pagesElement']).val()).then(function(components) {
            let groupedComponents = {};
            groupedComponents["---no-group---"] = {};
            groupedComponents["---no-group---"]["title"] = "root";
            groupedComponents["---no-group---"]["components"] = [];
            // $statuspage.nonGroupComponents($('#' + $incidentCreateDialog.params['pagesElement']).val()).then(function(ngc) {
            // });
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
                                $pluginCommon.getComponentStatusButtonClass(status.id),
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
            $('#' + $incidentCreateDialog.params['componentsElement']).html(components_str);
            $pluginCommon.buttonIdle('incident-create-dialog-submit-button');
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
                // $("#" + sourceId).removeClass("selected");
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
        let components = $pluginCommon.getComponentsConfig();
        let componentsForUpdate = {};
        $pluginCommon.status_values.forEach(function(item) {
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
