AJS.$(function () {
    // AJS.log("statuspage loaded");
});
let $statuspage = {

    // ----------- API -------------
    pages : function() {
        let url = this.baseUrl() + "pages?issueKey=" + JIRA.Issue.getIssueKey();
        const result = this.serviceCall(url);
        // AJS.log("----------- pages")
        // AJS.log(result);
        return result;
    },
    groups : function(pageId) {
        let url = this.baseUrl() + "groups?pageId=" + pageId + "&issueKey=" + JIRA.Issue.getIssueKey();
        const result = this.serviceCall(url);
        // AJS.log("----------- groups")
        // AJS.log(result);
        return result;
    },
    groupComponents : function(pageId, groupId) {
        let url = this.baseUrl() + "groupComponents?pageId=" + pageId + "&groupId=" + groupId + "&issueKey=" + JIRA.Issue.getIssueKey();
        const result = this.serviceCall(url);
        // AJS.log("----------- group components")
        // AJS.log(result);
        return result;
    },
    nonGroupComponents : function(pageId) {
        let url = this.baseUrl() + "nonGroupComponents?pageId=" + pageId + "&issueKey=" + JIRA.Issue.getIssueKey();
        const result = this.serviceCall(url);
        // AJS.log("----------- non-group components")
        // AJS.log(result);
        return result;
    },
    components : function(pageId) {
        let url = this.baseUrl() + "components?pageId=" + pageId + "&issueKey=" + JIRA.Issue.getIssueKey();
        const result = this.serviceCall(url);
        // AJS.log("----------- components")
        // AJS.log(result);
        return result;
    },
    incidents : function(pageId, activeOnly) {
        let url = this.baseUrl() + "incidents?pageId=" + pageId + "&issueKey=" + JIRA.Issue.getIssueKey() + "&activeOnly=" + activeOnly;
        const result = this.serviceCall(url);
        // AJS.log("----------- incidents")
        // AJS.log(result);
        return result;
    },
    impacts: function() {
        let url = this.baseUrl() + "impacts";
        const result = this.serviceCall(url);
        // AJS.log("----------- impacts: ")
        // AJS.log(result);
        return result;
    },
    componentStatuses: function() {
        let url = this.baseUrl() + "component/statuses";
        const result = this.serviceCall(url);
        // AJS.log("----------- component statuses: " + JSON.stringify(result))
        return result;
    },
    defaultMessage: function(status) {
        if (status == "investigating")
            return "We are continuing to investigate this issue.";
        else if (status == "identified")
            return "The issue has been identified and a fix is being implemented.";
        else if (status == "monitoring")
            return "A fix has been implemented and we are monitoring the results.";
        else if (status == "resolved")
            return "This incident has been resolved.";
        else if (status == "scheduled")
            return "We will be undergoing scheduled maintenance during this time.";
        else if (status == "in_progress")
            return "Scheduled maintenance is currently in progress. We will provide updates as necessary.";
        else if (status == "verifying")
            return "Verification is currently underway for the maintenance items.";
        else if (status == "completed")
            return "The scheduled maintenance has been completed.";
        else
            return "";
    },

    // ---------- TOOLS ------------
    baseUrl: function () {
        return AJS.contextPath() + "/rest/ws-slink-statuspage/1.0/api/";
    },
    serviceCall : async function(url_) {
        return await jQuery.get(url_);
    }
}
