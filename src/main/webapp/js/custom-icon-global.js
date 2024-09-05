/**
 * Cleanup unused icons.
 * @param {string} successMessage - The success message.
 * @param {string} errorMessage - The error message.
 */
function doCustomIconCleanup(successMessage, errorMessage) {
    fetch(rootURL + "/descriptor/jenkins.plugins.foldericon.CustomFolderIconConfiguration/cleanup", {
        method: "post",
        headers: crumb.wrap({}),
    }).then(rsp => {
        let button = document.getElementById("custom-icon-cleanup")
        if (rsp.ok) {
            hoverNotification(successMessage, button.parentNode.parentNode);
        } else {
            hoverNotification(errorMessage + " " + rsp.status + " - " + rsp.statusText, button.parentNode.parentNode);
        }
    }).catch(error => {
        console.error(error);
    });
}
