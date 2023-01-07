/**
 * Cleanup unused icons.
 *
 * @param {string} successMessage - The success message.
 * @param {string} errorMessage - The error message.
 */
function doCleanup(successMessage, errorMessage) {
    new Ajax.Request(rootURL + "/descriptor/jenkins.plugins.foldericon.CustomFolderIcon/cleanup", {
        onSuccess: function (rsp) {
            alert(successMessage);
        },
        onFailure: function (rsp) {
            alert(errorMessage + " " + rsp.message);
        }
    });
}