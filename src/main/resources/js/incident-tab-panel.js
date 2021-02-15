let $incidentTabPanel = {

    config: {
        // incident view
        impactBlockId                : "incident-impact-block",
        statusBlockId                : "incident-status-block",
        messageBlockId               : "incident-message-block",
        commentCheckboxId            : "publish-comment-checkbox",
        affectedComponentsListBlockId: "tab-components-list",

        // incident glance view
        glanceImpactBlockId          : "incident-glance-impact",
        glanceStatusBlockId          : "incident-glance-status",

        // add new component
        newComponentBlockId          : "incident-tab-panel-add-component",
        newComponentTitleElementId   : "new-component-title",
        newComponentStatusElementId  : "new-component-status",

        // hidden fields
        pageIdBlockId                : "page-id",
        incidentIdBlockId            : "incident-id",
        projectKeyBlockId            : "project-key",
        issueKeyBlockId              : "issue-key",

        // cached original values
        originalStatusBlockId        : "status-original",
        originalImpactBlockId        : "impact-original",
        originalComponentsBlockId    : "components-original",
        cachedComponentsBlockId      : "components-add-cache",

        // buttons
        updateButtonId               : "tab-update-incident-button",
    }
   ,confirmDialog: {}
   ,submitConfirmDialog: function() {
        if($incidentTabPanel.confirmDialog) {
            $incidentTabPanel.confirmDialog.hide();
        }
        $incidentTabPanel.doUpdateIncident();
   }
   ,cancelConfirmDialog: function() {
        if($incidentTabPanel.confirmDialog) {
            $incidentTabPanel.confirmDialog.hide();
        }
   }
   ,changeComponentState: function (source, f) {
        let parentElement = source.parentElement;
        let componentId = parentElement.id;
        let statusId    = source.id.replace(componentId + "-", "");
        // AJS.log("componentId: " + componentId + ", status: " + statusId);
        let update = $incidentTabPanel.enableSaveButton;
        // AJS.log("f:");
        // AJS.log(f);
        if (undefined != f && 'undefined' != f)
            update = f;
        // AJS.log("update:");
        // AJS.log(update);
        if (!($("#" + componentId + "-remove").hasClass("selected"))) {
            $("#" + componentId).children("span").removeClass("selected");
            $("#" + source.id).addClass("selected");
            update();
        }
    } // changeComponentState
   ,removeComponent: function(source) {
        let parentElement = source.parentElement;
        let componentId = parentElement.id;
        let statusId    = source.id.replace(componentId + "-", "");
        // AJS.log("componentId: " + componentId + ", status: " + statusId);
        let f = $incidentTabPanel.enableSaveButton;
        if (!$("#" + source.id).hasClass("selected")) {
            $("#" + source.id).addClass("selected");
            $("#" + parentElement.id).find(".component-name").addClass("removed");
            $("#" + parentElement.id).find(".component-button").addClass("removed");
            f();
        } else {
            $("#" + source.id).removeClass("selected");
            $("#" + parentElement.id).find(".component-name").removeClass("removed");
            $("#" + parentElement.id).find(".component-button").removeClass("removed");
        }
    } // removeComponent
   ,changeAllComponentsState: function(source) {
        let parentElement = source.parentElement;
        let componentId = parentElement.id;
        let statusId    = source.id.replace(componentId + "-", "");
        // AJS.log("componentId: " + componentId + ", status: " + statusId);
        let f = $incidentTabPanel.changeComponentState;
        let u = $incidentTabPanel.enableSaveButton;
        $(".component-name").not(".removed").each( function(idx, value) {
            let parentId = $(this).parent().attr("id");
            f($("#" + parentId + "-" + statusId)[0], u);
        });
    } // changeAllComponentsState
   ,removeAllComponents: function(source) {
        if (!$("#" + source.id).hasClass("slfvnosjhb")) {
            $("#" + source.id).addClass("slfvnosjhb");
            $(".component-name").addClass("removed");
            $(".component-button.remove").not(".header").addClass("selected");
        } else {
            $("#" + source.id).removeClass("slfvnosjhb");
            $(".component-name").removeClass("removed");
            $(".component-button.remove").not(".header").removeClass("selected");
        }
    } // removeAllComponents
   ,changeIncidentImpact: function(source) {
        if(!$(".tab-panel div.incident-impact-" + source.id).hasClass("selected")) {
            $(".tab-panel div.incident-impact").removeClass("selected");
            $(".tab-panel div.incident-impact-" + source.id).addClass("selected");
            $incidentTabPanel.enableSaveButton();
        }
    } // changeIncidentImpact
   ,changeIncidentStatus: function(source) {
        if (!$(".tab-panel div.incident-status-" + source.id).hasClass("selected")) {
            $(".tab-panel div.incident-status").removeClass("selected");
            $(".tab-panel div.incident-status-" + source.id).addClass("selected");
            // $("#serMemtb").attr("placeholder", "Type a Location").val("").focus().blur();
            // console.log("---> " + source.id + " : " + $statuspage.defaultMessage(source.id));
            let messageInput = $("#" + $incidentTabPanel.config.messageBlockId).find("textarea");
            if (messageInput)
                messageInput.attr("placeholder", $statuspage.defaultMessage(source.id))
            $incidentTabPanel.enableSaveButton();
        }
    } // changeIncidentImpact
   ,addAffectedComponentMain: function() {
        let componentId   = $("#" + $incidentTabPanel.config.newComponentTitleElementId).val();
        if (null != componentId && undefined != componentId && "" != componentId) {
            let componentName = $("#" + $incidentTabPanel.config.newComponentTitleElementId + " option:selected").text();
            let statusId      = $("#" + $incidentTabPanel.config.newComponentStatusElementId).val();
            $("#" + $incidentTabPanel.config.newComponentTitleElementId + " option[value='" + componentId + "']").remove();
            let component = {"id": componentId, "name": componentName, "status": statusId};
            $incidentTabPanel.addCachedComponent(component);
            $incidentTabPanel.addAffectedComponent(component);
            $incidentTabPanel.enableSaveButton();
        }
    }
   ,addAffectedComponent: function(component) {
        // console.log("adding affected component: " + JSON.stringify(component, null, 2));
        let f = $pluginCommon.getComponentStatusButtonClass;
        let html  = '<div class="component" id="' + component.id + '">\n';
        html += '<div class="component-name">' + component.name + '</div>\n';
        $("#" + $incidentTabPanel.config.newComponentStatusElementId + " > option").each(function() {
            html += '<span id="' + component.id + '-' + this.value + '" onClick="statusButtonClick(this)" title="' + this.text + '" ';
            html += 'class="component-button aui-icon aui-icon-small ' + this.value + ' ';
            html += f(this.value) + ' ' + ((this.value == component.status) ? "selected" : "") + '">\n';
            html += this.text + "\n</span>\n";
            // AJS.log("option: " + this.text + ' ' + this.value);
        });
        html += '<span id="' + component.id + '-remove" title="Remove Component" onClick="removeButtonClick(this)" ';
        html += 'class="component-button remove aui-icon aui-icon-small aui-iconfont-trash">\nREMOVE\n</span>\n';
        html += '</div>';
        // AJS.log(html);
        // AJS.log("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        // AJS.log($("#" + container).html());
        $("#" + $incidentTabPanel.config.affectedComponentsListBlockId).append(html);
    }
   ,updateIncident: function() {
        $incidentTabPanel.confirmDialog = AJS.dialog2("#confirm-dialog");
        $incidentTabPanel.confirmDialog.show();
   }
   ,doUpdateIncident: function() {

        // var demoDialog = AJS.dialog2("#demo-dialog");
        // demoDialog.on("show", function() {
        //     console.log("demo-dialog was shown");
        // });


        $pluginCommon.buttonBusy($incidentTabPanel.config.updateButtonId, false);

        let status     = $("#" + $incidentTabPanel.config.statusBlockId  + " .selected").attr("id");
        let impact     = $("#" + $incidentTabPanel.config.impactBlockId  + " .selected").attr("id");
        let message    = $("#" + $incidentTabPanel.config.messageBlockId + " textarea").val();

        let pageId     = $("#" + $incidentTabPanel.config.pageIdBlockId).val();
        let incidentId = $("#" + $incidentTabPanel.config.incidentIdBlockId).val();
        let projectKey = $("#" + $incidentTabPanel.config.projectKeyBlockId).val();
        let issueKey   = $("#" + $incidentTabPanel.config.issueKeyBlockId).val();

        let components = $pluginCommon.getComponentsConfig();
        // console.log("----------> components config:");
        // console.log(JSON.stringify(components, null, 2));
        // delete components.remove;

        let componentsForUpdate = {};
        let componentsToStore = [];
        $pluginCommon.status_values.forEach(function(item, index) {
            componentsForUpdate[item] = components[item].map(a => a.id);
            componentsToStore.push.apply(componentsToStore, components[item]);
        })

        components["remove"].forEach(function(item, index) {
            // console.log("caching removed component: " + JSON.stringify(item, null, 2));
            $incidentTabPanel.addCachedComponent(item);
        });

        $incidentTabPanel.removeCachedComponents(componentsToStore.map(c => c.id));

        let config = {}
        config["project"]       = projectKey;
        config["issue"]         = issueKey;
        config["page"]          = pageId;
        config["incident"]      = incidentId;
        config["status"]        = status;
        config["impact"]        = impact;
        config["message"]       = message;
        config["components"]    = componentsForUpdate;

        config["publishComment"] = $("#" + $incidentTabPanel.config.commentCheckboxId)[0].checked;

        // console.log("------> config");
        // console.log(JSON.stringify(config, null, 2));
        // console.log("------> components for update");
        // console.log(JSON.stringify(componentsForUpdate, null, 2));
        // console.log("------> components to cache");
        // console.log(JSON.stringify(componentsToStore, null, 2));

        let f1 = $incidentTabPanel.restoreAffectedComponents;
        let f2 = $incidentTabPanel.restoreCachedComponents;
        AJS.$.ajax({
            url: AJS.contextPath() + "/rest/ws-slink-statuspage/1.0/api/incident",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(config, null, 2),
            processData: false
        }).done(function () {
            JIRA.Messages.showSuccessMsg("statuspage updated");
            $pluginCommon.buttonIdle($incidentTabPanel.config.updateButtonId);
            let object = $('#' + $incidentTabPanel.config.glanceImpactBlockId);
            object[0].className = "selected incident-impact incident-impact-" + impact;
            object[0].innerText = impact;

            object = $('#' + $incidentTabPanel.config.glanceStatusBlockId);
            object[0].className = "incident-glance-common incident-status-" + status;
            object[0].innerText = status;

            $("#" + $incidentTabPanel.config.originalStatusBlockId).val(status);
            $("#" + $incidentTabPanel.config.originalImpactBlockId).val(impact);
            $("#" + $incidentTabPanel.config.originalComponentsBlockId).val(JSON.stringify(componentsToStore));
            // console.log("---> saved components list ('original'):");
            // console.log($("#" + $incidentTabPanel.config.originalComponentsBlockId).val());
            f1();
            f2();

            if (status == "resolved") {
                $("#" + $incidentTabPanel.config.updateButtonId).hide();
                $("#" + $incidentTabPanel.config.newComponentBlockId).hide();
                $('#' + $incidentTabPanel.config.impactBlockId).find('div').each(function() {
                    if ($(this).hasClass('incident-impact-' + impact))
                        $(this).addClass("selected")
                    else
                        $(this).hide();
                })
                $('#' + $incidentTabPanel.config.statusBlockId).find('div').each(function() {
                    if (!($(this).hasClass('incident-status-' + status)))
                        $(this).hide()
                })
            }
            // setTimeout(() => {
            //     window.location = $(location).attr('href');
            // }, 1000);
        }).error(function (error, message) {
            AJS.log("--- error -----------------------------------------");
            AJS.log(error);
            AJS.log("--- message ---------------------------------------");
            AJS.log(message);
            AJS.log("---------------------------------------------------");
            JIRA.Messages.showErrorMsg("could not update statuspage: <br><br> " + error.status + "<br>" + error.responseText)
            $pluginCommon.buttonIdle($incidentTabPanel.config.updateButtonId);
        });
    }
   ,resetIncident: function() {
        let status     = $("#" + $incidentTabPanel.config.originalStatusBlockId).val();
        let impact     = $("#" + $incidentTabPanel.config.originalImpactBlockId).val();

        $(".tab-panel #" + $incidentTabPanel.config.statusBlockId + " div").removeClass("selected");
        $(".tab-panel .incident-status-" + status).addClass("selected");

        $(".tab-panel #" + $incidentTabPanel.config.impactBlockId + " div").removeClass("selected");
        $(".tab-panel .incident-impact-" + impact).addClass("selected");

        this.restoreAffectedComponents();
        this.restoreCachedComponents();
    }
   ,restoreAffectedComponents: function() {
        let componentsList = JSON.parse($("#" + $incidentTabPanel.config.originalComponentsBlockId).val());
        componentsList.sort(function(a, b) {
            return a.name > b.name ? 1 : a.name < b.name ? -1 : 0;
        })
        // console.log("---> restored components list ('original'):");
        // console.log($("#" + $incidentTabPanel.config.originalComponentsBlockId).val());
        $(".tab-panel #" + $incidentTabPanel.config.affectedComponentsListBlockId).html("");
        for (let i in componentsList) {
            // console.log("--> restoring component: " + JSON.stringify(componentsList[i], null, 2));
            $incidentTabPanel.addAffectedComponent(componentsList[i]);
        }
    }
   ,restoreCachedComponents: function() {
        if ($("#" + $incidentTabPanel.config.cachedComponentsBlockId).val())
            try {
                let cachedComponents = JSON.parse($("#" + $incidentTabPanel.config.cachedComponentsBlockId).val());
                // console.log("---> reset cached components");
                // console.log(cachedComponents);
                Object.keys(cachedComponents).forEach(function(key, index) {
                    // console.log(cachedComponents[key]);
                    $("#" + $incidentTabPanel.config.newComponentTitleElementId).append('<option value="' + cachedComponents[key].id + '">' + cachedComponents[key].name + '</option>');
                })
                let options = $('#' + $incidentTabPanel.config.newComponentTitleElementId + ' option');
                let arr = options.map(function(_, o) { return { t: $(o).text(), v: o.value }; }).get();
                arr.sort(function(o1, o2) { return o1.t > o2.t ? 1 : o1.t < o2.t ? -1 : 0; });
                options.each(function(i, o) {
                    o.value = arr[i].v;
                    $(o).text(arr[i].t);
                });
                $("#" + $incidentTabPanel.config.cachedComponentsBlockId).val("");
            } catch (error) {
                AJS.log("---> error: " + error);
            }
    }
   ,addCachedComponent: function(component) {
        let cache = {};
        let str = $("#" + $incidentTabPanel.config.cachedComponentsBlockId).val();
        if (str) {
            cache = JSON.parse(str);
        }
        cache[component.id] = component;
        // console.log("----> add cached component: " + JSON.stringify(cache, null, 2));
        $("#" + $incidentTabPanel.config.cachedComponentsBlockId).val(JSON.stringify(cache));
        // console.log($("#" + $incidentTabPanel.config.cachedComponentsBlockId).val());
    }
   ,removeCachedComponents: function(componentIdsList) {
        // console.log("----> remove cached components: " + JSON.stringify(componentIdsList));
        let str = $("#" + $incidentTabPanel.config.cachedComponentsBlockId).val();
        if (str) {
            let cache = JSON.parse(str);
            componentIdsList.forEach(function(item, index) {
                delete cache[item];
            })
            // console.log(cache)
            $("#" + $incidentTabPanel.config.cachedComponentsBlockId).val(JSON.stringify(cache));
        }
    }
   ,enableSaveButton: function() {
        try {
            if (!$("#" + $incidentTabPanel.config.updateButtonId).hasClass("aui-button-primary"))
                $("#" + $incidentTabPanel.config.updateButtonId).addClass("aui-button-primary");
        } catch (e) {
            AJS.log("error: " + e)
        }
    }
   ,test: function() {
        AJS.log("incident tab panel test");
    }
} // $incidentTabPanel
// jQuery(function () {
//     AJS.log("incident-tab-panel loaded");
// });
