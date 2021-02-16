let $incidentUnlinkDialog = {
    config: {
        unlinkButtonId: "incident-unlink-dialog-submit-button",
        unlinkFormContentId: "unlink-form-content",
        unlinkFormWarningId: "unlink-form-warning"
    },
    checkAccess: function () {
        $pluginCommon
            .checkAccess(
                () => {
                    $("#" + $incidentUnlinkDialog.config.unlinkFormWarningId).hide()
                    $("#" + $incidentUnlinkDialog.config.unlinkFormContentId).show();
                    $("#" + $incidentUnlinkDialog.config.unlinkButtonId).show();
                },
                () => {
                    $("#" + $incidentUnlinkDialog.config.unlinkFormContentId).hide();
                    $("#" + $incidentUnlinkDialog.config.unlinkButtonId).hide();
                    $("#" + $incidentUnlinkDialog.config.unlinkFormWarningId).show()
                }
            );
    }
}
