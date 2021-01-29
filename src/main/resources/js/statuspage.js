jQuery(function () {
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

    // ---------- TOOLS ------------
    baseUrl: function () {
        return AJS.contextPath() + "/rest/ws-slink-statuspage/1.0/api/";
    },
    serviceCall : async function(url_) {
        return await jQuery.get(url_);
   }
}
