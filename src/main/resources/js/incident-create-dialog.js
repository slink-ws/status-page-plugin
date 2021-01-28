jQuery(function () {
    $("#page-location").val($(location).attr('href'));
});
let $incidentCreateDialog = {

    componentStatuses: [],
    cachedComponents: {},
    loadImpacts: function(impactsElement) {
        const impacts = $statuspage.impacts();
        let options_str = "";
        impacts.forEach( function(item) {
            options_str += '<option value="' + item + '">' + item + '</option>';
        });
        $('#' + impactsElement)[0].innerHTML = options_str;
    },
    loadPages : function(pagesElement) {
        const pages = $statuspage.pages();
        // AJS.log(pages);
        let options_str = "";
        pages.forEach( function(page) {
            options_str += '<option value="' + page.id + '">' + page.name + '</option>';
        });
        $('#' + pagesElement)[0].innerHTML = options_str;
        // this.loadComponents($('#' + pagesElement).val(), incidentsElement, wheelElement);
    },
    loadHeader : function(headerElement, componentsElement, pageElement) {
        this.componentStatuses = $statuspage.componentStatuses();
        let header_str = '<div class="component-name-header">&nbsp;</div>';


        header_str  += '<span id="components-header-reset"' +
            ' onclick="headerButtonClick(this)" ' +
            ' title="Set all components to actual state" ' +
            ' class="component-button component-header reset header aui-icon aui-icon-small aui-iconfont-refresh">' +
            'Reset' + '</span>';

        this.componentStatuses.forEach( function(item) {
            // console.log(item);
            header_str  += '<span id="components-header-' + item.id + '"' +
                           ' onclick="headerButtonClick(this)" ' +
                           ' title="Set all components to \'' + item.title + '\' state" ' +
                           ' class="component-button component-header ' + item.id + ' header aui-icon aui-icon-small ' + $pluginCommon.getComponentStatusButtonClass(item.id) + '">' +
                           item.title + '</span>';
        });
        header_str += '<div style="float: left; width: 10px;"></div>'
        // header_str += '<span id="components-header-remove" title="Remove all components" onClick="headerRemoveClick(this)"' +
        //               ' class="component-button header remove aui-icon aui-icon-small aui-iconfont-trash">Remove</span>';
        // header_str += '<span class="component-button header remove>&nbsp;</span>';
        $('#' + headerElement)[0].innerHTML = header_str;
        this.loadComponents(componentsElement, pageElement);
    },
    loadComponents : function(componentsElement, pageElement) {
        // console.log(pageElement + " -> " + $('#' + pageElement).val());
        $('#' + componentsElement).html("");

        $('#components-load-spinner').show();

        let groups     = $statuspage.groups($('#' + pageElement).val())
        let components = $statuspage.components($('#' + pageElement).val());

        // console.log(components);

        let groupedComponents = {};
        groupedComponents["---no-group---"] = {};
        groupedComponents["---no-group---"]["title"] = "root";
        groupedComponents["---no-group---"]["components"] = $statuspage.nonGroupComponents($('#' + pageElement).val());

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
                }
            }
        });
        // console.log(JSON.stringify(groupedComponents, null, 2));

        let components_str = "";
        for (let key in groupedComponents) {
            let group = groupedComponents[key];
            if (group.components.length > 0) {
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
                            false,//(component.status == status.id),
                            component.id,
                            status.id,
                            status.title
                        );
                           // '    <span id="' + component.id + '-' + status.id + '" ' +
                           // '          onclick="statusButtonClick(this)" ' +
                           // '          title="' + status.title + '" ' +
                           // '          class="component-button ' + status.id + ' ' + ((component.status == status.id) ? 'selected' : '') +
                           // '                 aui-icon aui-icon-small ' + $pluginCommon.getComponentStatusButtonClass(status.id) + '" ' +
                           // '          >' + status.title + '</span>';
                    });
                    // components_str += '<span id="' + component.id + '-remove" title="Remove Component" ' +
                    //                   '      onclick="removeButtonClick(this)" ' +
                    //                   '      class="component-button remove aui-icon aui-icon-small aui-iconfont-trash">' +
                    //                   '  REMOVE' +
                    //                   '</span>';
                    // components_str += '<span class="component-button remove>&nbsp;</span>';
                    components_str += '<div style="float: left; width: 10px;"></div>'
                    components_str += '</div>';
                });
            }
        }
        $('#components-load-spinner').hide();
        $('#' + componentsElement).html(components_str);
        // console.log($incidentCreateDialog.cachedComponents);
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
        // console.log($pluginCommon.getComponentsConfig());
        $("#components-config").val(JSON.stringify($pluginCommon.getComponentsConfig()));
        // console.log($("#components-config").val());
    },


    // loadIncidents : function(pageId, element, wheelElement) {
    //     // AJS.log("incidentLinkDialog.loadIncidents for " + pageId);
    //     $('#' + wheelElement).show();
    //     const incidents = $statuspage.incidents(pageId, true);
    //     // AJS.log(incidents);
    //     let options_str = "";
    //     incidents.forEach( function(page) {
    //         options_str += '<option value="' + page.id + '">' + page.name + '</option>';
    //     });
    //     $('#' + element)[0].innerHTML = options_str;
    //     $('#' + wheelElement).hide();
    // },
    buttonHTML: function (buttonClass, selected, componentId, statusId, title) {
        let result =
            '    <span id="' + componentId + '-' + statusId + '" ' +
            '          onclick="statusButtonClick(this)" ' +
            '          title="' + title + '" ' +
            '          class="component-button ' + statusId + ' ' + ((selected) ? 'selected' : '') +
            '                 aui-icon aui-icon-small ' + buttonClass + '" ' +
            '          >' + title + '</span>';
        return result;
    },

    test : function() {
        // AJS.log("$incidentLinkDialog: test");
    }

}

