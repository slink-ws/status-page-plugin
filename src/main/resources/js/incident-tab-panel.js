let $incidentTabPanel = {

    config: {
        // incident view
        pageNameBlockId              : "incident-page-block",
        titleBlockId                 : "incident-title-block",
        impactBlockId                : "incident-impact-block",
        statusBlockId                : "incident-status-block",
        messageBlockId               : "incident-message-block",
        commentCheckboxId            : "publish-comment-checkbox",
        affectedComponentsListBlockId: "tab-components-list",

        // incident glance view
        glancePageBlockId            : "incident-glance-page",
        glanceTitleBlockId           : "incident-glance-title",
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
        // issueKeyBlockId              : "issue-key",

        // buttons
        updateButtonId               : "tab-update-incident-button",
        resetButtonId                : "tab-reset-incident-button",
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
        let update = $incidentTabPanel.enableSaveButton;
        if (undefined != f && 'undefined' != f)
            update = f;
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
            $incidentTabPanel.setMessage($statuspage.defaultMessage(source.id), null);
            $incidentTabPanel.enableSaveButton();
        }
    } // changeIncidentImpact
   ,addAffectedComponentMain: function() {
        let componentId   = $("#" + $incidentTabPanel.config.newComponentTitleElementId).val();
        if (null != componentId && undefined != componentId && "" != componentId) {
            let componentName = $("#" + $incidentTabPanel.config.newComponentTitleElementId + " option:selected").text();
            $("#" + $incidentTabPanel.config.newComponentTitleElementId + " option[value='" + componentId + "']").remove();
            if ($("#" + $incidentTabPanel.config.newComponentTitleElementId + " option").length == 0) {
                $("#" + $incidentTabPanel.config.newComponentTitleElementId).auiSelect2("val", "");
            } else {
                $("#" + $incidentTabPanel.config.newComponentTitleElementId).trigger('change');
            }
            let component = {"id": componentId, "name": componentName, "status": ""};
            $incidentTabPanel.addAffectedComponent(component);
            $incidentTabPanel.enableSaveButton();
        }
    }
   ,addAffectedComponent: function(component) {
        let f = $statusPageCommon.getComponentStatusButtonClass;
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
        $("#" + $incidentTabPanel.config.affectedComponentsListBlockId).append(html);
    }

   ,updateIncident: function() {
        $incidentTabPanel.confirmDialog = AJS.dialog2("#confirm-dialog");
        $incidentTabPanel.confirmDialog.show();
   }
   ,doUpdateIncident: function() {
        $statusPageCommon.buttonBusy($incidentTabPanel.config.updateButtonId, false);

        let status     = $("#" + $incidentTabPanel.config.statusBlockId  + " .selected").attr("id");
        let impact     = $("#" + $incidentTabPanel.config.impactBlockId  + " .selected").attr("id");
        let message    = $("#" + $incidentTabPanel.config.messageBlockId + " textarea").val();

        let pageId     = $("#" + $incidentTabPanel.config.pageIdBlockId).val();
        let incidentId = $("#" + $incidentTabPanel.config.incidentIdBlockId).val();
        let projectKey = $("#" + $incidentTabPanel.config.projectKeyBlockId).val();
        let issueKey   = JIRA.Issue.getIssueKey() // $("#" + $incidentTabPanel.config.issueKeyBlockId).val();

        let components = $statusPageCommon.getComponentsConfig();
        let componentsForUpdate = {};
        $statusPageCommon.status_values.forEach(function(item, index) {
            componentsForUpdate[item] = components[item].map(a => a.id);
        })
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

        AJS.$.ajax({
            url: AJS.contextPath() + "/rest/ws-slink-statuspage/1.0/api/incident",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(config, null, 2),
            processData: false
        }).done(function () {
            JIRA.Messages.showSuccessMsg("statuspage updated");
            $statusPageCommon.buttonIdle($incidentTabPanel.config.updateButtonId);
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
            $incidentTabPanel.resetIncident();
        }).error(function (error, message) {
            AJS.log("[update incident] error");
            AJS.log(error);
            AJS.log(message);
            JIRA.Messages.showErrorMsg(error.responseText)
            $statusPageCommon.buttonIdle($incidentTabPanel.config.updateButtonId);
        });
    }
   ,reloadIncident: function() {
        $statusPageCommon.buttonBusy($incidentTabPanel.config.updateButtonId, true);
        $statusPageCommon.buttonBusy($incidentTabPanel.config.resetButtonId, true);
        $statusPageCommon.incidentsQuery(JIRA.Issue.getIssueKey())
        .then(function(incident) {
            $statusPageCommon.setImpact(incident.impact, $incidentTabPanel.config.impactBlockId, $incidentTabPanel.config.glanceImpactBlockId);
            $statusPageCommon.setStatus(incident.status, $incidentTabPanel.config.statusBlockId, $incidentTabPanel.config.glanceStatusBlockId);
            $statusPageCommon.setPageName(incident.page.name, $incidentTabPanel.config.pageNameBlockId, $incidentTabPanel.config.glancePageBlockId);
            $statusPageCommon.setIncidentTitle(incident.name, $incidentTabPanel.config.titleBlockId, $incidentTabPanel.config.glanceTitleBlockId);
            $statusPageCommon.componentsQuery(JIRA.Issue.getIssueKey(), incident)
                .then(function(components) {
                    let affectedComponentsIds = incident.components.map((component) => component.id);
                    let nonAffectedComponents = components
                        .filter((component) => !affectedComponentsIds.includes(component.id))
                        .filter((component) => !component.group)
                    ;
                    // AJS.log(JSON.stringify(nonAffectedComponents, null, 2))
                    // AJS.log("---------");
                    // AJS.log(JSON.stringify(incident.components, null, 2))
                    $incidentTabPanel.restoreAffectedComponents(incident.components);
                    $incidentTabPanel.restoreNonAffectedComponents(nonAffectedComponents);
                    $statusPageCommon.buttonIdle($incidentTabPanel.config.updateButtonId);
                    $statusPageCommon.buttonIdle($incidentTabPanel.config.resetButtonId);
                    JIRA.Messages.showSuccessMsg("incident reloaded");
                })
                .catch(function (error) {
                    JIRA.Messages.showErrorMsg("could not reload components");
                    AJS.log(JSON.stringify(error, null, 2));
                    $statusPageCommon.buttonIdle($incidentTabPanel.config.updateButtonId);
                    $statusPageCommon.buttonIdle($incidentTabPanel.config.resetButtonId);
                });
        })
        .catch(function(error) {
            JIRA.Messages.showErrorMsg("could not reload incident");
            AJS.log(JSON.stringify(error, null, 2));
            $statusPageCommon.buttonIdle($incidentTabPanel.config.updateButtonId);
            $statusPageCommon.buttonIdle($incidentTabPanel.config.resetButtonId);
        })
        ;
    }
   ,resetIncident: function() {
        this.reloadIncident();
        $incidentTabPanel.setMessage($statuspage.defaultMessage(status), "");
        $("#" + $incidentTabPanel.config.commentCheckboxId).prop('checked', false);
    }
   ,restoreAffectedComponents: function(components) {
        components.sort(function(a, b) {
            return a.name > b.name ? 1 : a.name < b.name ? -1 : 0;
        })
        $(".tab-panel #" + $incidentTabPanel.config.affectedComponentsListBlockId).html("");
        for (let i in components) {
            // console.log("--> restoring component: " + JSON.stringify(componentsList[i], null, 2));
            $incidentTabPanel.addAffectedComponent(components[i]);
        }
    }
   ,restoreNonAffectedComponents: function(components) {
        for (let i in components) {
            $("#" + $incidentTabPanel.config.newComponentTitleElementId).append('<option value="' + components[i].id + '">' + components[i].name + '</option>');
        }
        let options = $('#' + $incidentTabPanel.config.newComponentTitleElementId + ' option');
        let arr = options.map(function(_, o) { return { t: $(o).text(), v: o.value }; }).get();
        arr.sort(function(o1, o2) { return o1.t > o2.t ? 1 : o1.t < o2.t ? -1 : 0; });
        options.each(function(i, o) {
            o.value = arr[i].v;
            $(o).text(arr[i].t);
        });
        $("#" + $incidentTabPanel.config.newComponentTitleElementId).trigger('change');
    }
   ,setMessage: function(placeholder, value) {
        let messageInput = $("#" + $incidentTabPanel.config.messageBlockId).find("textarea");
        if (messageInput) {
            if (placeholder)
                messageInput.attr("placeholder", placeholder);
            if (null != value)
                messageInput.val(value)
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
} // $incidentTabPanel

AJS.$(function () {
    // console.log("[STATUSPAGE TAB-PANEL JS LOADED]");
});
