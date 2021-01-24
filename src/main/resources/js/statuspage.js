jQuery(function () {
    AJS.log("statuspage loaded");
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

    // ---------- TOOLS ------------
    baseUrl: function () {
        return AJS.contextPath() + "/rest/ws-slink-statuspage/1.0/api/";
    },
    // https://stackoverflow.com/questions/133310/how-can-i-get-jquery-to-perform-a-synchronous-rather-than-asynchronous-ajax-re/39058130#39058130
    // TODO: rework serviceCall function according to link above
    serviceCall : function(url_) {
        let strReturn = "";
        AJS.$.ajax({
            url: url_,
            success: function(html) {
                strReturn = html;
            },
            async:false
        });
        return strReturn;
    },
    updateTestDiv : function () {
        // document.getElementById('test-div').innerHTML = "H E L L O  F R O M  A J A X !";
        // $('#test-div')[0].innerHTML = "H E L L O  F R O M  A J A X !";
        // AJS.log("updateTestDiv");
    }

}
