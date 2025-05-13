/**
 * Cleanup unused icons.
 */
Behaviour.specify('[id^="custom-icon-cleanup"]', "CustomIconCleanup", 0, (element) => {
    element.onclick = () => {
        fetch(rootURL + "/descriptor/jenkins.plugins.foldericon.CustomFolderIconConfiguration/cleanup", {
            method: "post",
            headers: crumb.wrap({}),
        })
            .then((response) => {
                if (response.ok) {
                    hoverNotification("Successfully removed all unused icon images", element.parentNode.parentNode);
                } else {
                    hoverNotification(
                        "Failed to remove unused icon images: " + response.status + " - " + response.statusText,
                        element.parentNode.parentNode,
                    );
                }
            })
            .catch((error) => {
                console.error(error);
            });

        return false;
    };
});
