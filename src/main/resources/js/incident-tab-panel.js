let $incidentTabPanel = {
    changeComponentState: function (source, f) {
        let parentElement = source.parentElement;
        let componentId = parentElement.id;
        let statusId    = source.id.replace(componentId + "-", "");
        // AJS.log("componentId: " + componentId + ", status: " + statusId);
        let update = this.enableSaveButton;
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
        let f = this.enableSaveButton;
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
        let f = this.changeComponentState;
        let u = this.enableSaveButton;
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
            this.enableSaveButton();
        }
    } // changeIncidentImpact
    ,changeIncidentStatus: function(source) {
        if (!$(".tab-panel div.incident-status-" + source.id).hasClass("selected")) {
            $(".tab-panel div.incident-status").removeClass("selected");
            $(".tab-panel div.incident-status-" + source.id).addClass("selected");
            this.enableSaveButton();
        }
    } // changeIncidentImpact
   ,addAffectedComponent: function(title, status, container) {
        let componentId   = $("#" + title).val();
        if (null != componentId && undefined != componentId && "" != componentId) {
            let componentName = $("#" + title + " option:selected").text();
            let statusId      = $("#" + status).val();
            let statusName    = $("#" + status+ " option:selected").text();
            $("#" + title + " option[value='" + componentId + "']").remove();

            let f = $pluginCommon.getComponentStatusButtonClass;
            let html  = '<div class="component" id="' + componentId + '">\n';
            html += '<div class="component-name">' + componentName + '</div>\n';
            $("#" + status + " > option").each(function() {
                html += '<span id="' + componentId + '-' + this.value + '" onClick="statusButtonClick(this)" title="' + this.text + '" ';
                html += 'class="component-button aui-icon aui-icon-small ' + this.value + ' ';
                html += f(this.value) + ' ' + ((this.value == statusId) ? "selected" : "") + '">\n';
                html += this.text + "\n</span>\n";
                // AJS.log("option: " + this.text + ' ' + this.value);
            });
            html += '<span id="' + componentId + '-remove" title="Remove Component" onClick="removeButtonClick(this)" ';
            html += 'class="component-button remove aui-icon aui-icon-small aui-iconfont-trash">\nREMOVE\n</span>\n';
            html += '</div>';
            // AJS.log(html);
            // AJS.log("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            // AJS.log($("#" + container).html());
            $("#" + container).append(html);
            this.enableSaveButton();
        }
    }
   ,updateIncident: function(statusBlockId, impactBlockId, messageBlockId, pageIdBlock, incidentIdBlock, projectKeyBlock, issueKeyBlock) {
        $pluginCommon.buttonBusy('tab-update-incident-button', false);

        let status     = $("#" + statusBlockId + " .selected").attr("id");
        let impact     = $("#" + impactBlockId + " .selected").attr("id");
        let message    = $("#" + messageBlockId + " textarea").val();

        let pageId     = $("#" + pageIdBlock).val();
        let incidentId = $("#" + incidentIdBlock).val();
        let projectKey = $("#" + projectKeyBlock).val();
        let issueKey   = $("#" + issueKeyBlock).val();

        let components = $pluginCommon.getComponentsConfig();
        delete components.remove;

        let config = {}
        config["project"]    = projectKey;
        config["issue"]      = issueKey;
        config["page"]       = pageId;
        config["incident"]   = incidentId;
        config["status"]     = status;
        config["impact"]     = impact;
        config["message"]    = message;
        config["components"] = components;

        // AJS.log(JSON.stringify(config, null, 2));

        AJS.$.ajax({
            url: AJS.contextPath() + "/rest/ws-slink-statuspage/1.0/api/incident",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(config, null, 2),
            processData: false
        }).done(function () {
            JIRA.Messages.showSuccessMsg("statuspage updated");
            $pluginCommon.buttonIdle('tab-update-incident-button');
            setTimeout(() => {
                window.location = $(location).attr('href');
            }, 1000);
        }).error(function (error, message) {
            AJS.log("--- error -----------------------------------------");
            AJS.log(error);
            AJS.log("--- message ---------------------------------------");
            AJS.log(message);
            AJS.log("---------------------------------------------------");
            JIRA.Messages.showErrorMsg("could not update statuspage: <br><br> " + error.status + "<br>" + error.responseText)
            $pluginCommon.buttonIdle('tab-update-incident-button');
        });
    }
   ,enableSaveButton: function() {
        try {
            if (!$("#tab-update-incident-button").hasClass("aui-button-primary"))
                $("#tab-update-incident-button").addClass("aui-button-primary");
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
