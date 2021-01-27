jQuery(function () {
    // AJS.log("---> COMMON FUNCTION LOADED")
});

let $pluginCommon = {
    getComponentStatusButtonClass(status) {
        if (status == 'operational')
            return "aui-iconfont-check-circle";
        else if (status == 'degraded_performance')
            return "aui-iconfont-devtools-task-disabled";
        else if (status == 'partial_outage')
            return "aui-iconfont-failed-build";
        else if (status == 'major_outage')
            return "aui-iconfont-remove";
        else if (status == 'under_maintenance')
            return "aui-iconfont-info-circle";
        else
            return "";
    }
    ,getComponents: function(state) {
        let result = [];
        $(".component-name").not(".removed").each(function() {
            $(this).parent().find("." + state + ".selected").each(function (){
                result.push($(this).parent().attr("id"));
            });
        });
        return result;
    }
    ,getComponentsConfig: function() {
        let result = {};
        result["remove"]               = [];
        result["operational"]          = this.getComponents("operational");
        result["degraded_performance"] = this.getComponents("degraded_performance");
        result["partial_outage"]       = this.getComponents("partial_outage");
        result["major_outage"]         = this.getComponents("major_outage");
        result["under_maintenance"]    = this.getComponents("under_maintenance");
        $(".component-name.removed").each(function() {
            result["remove"].push($(this).parent().attr("id"));
        });
        return result;
    }
}