// global instance
let croppie;

/**
 * Initialization of croppie and preview image.
 */
Behaviour.specify('[id^="custom-icon-cropper"]', "CustomIconCropper", 0, () => {
    let preview = document.getElementById("custom-icon-name").getAttribute("value");
    let url;
    if (preview == null || preview === "") {
        url = rootURL + "/plugin/custom-folder-icon/icons/default.svg";
    } else {
        url = rootURL + "/userContent/customFolderIcons/" + preview;
    }

    // init croppie
    croppie = new Croppie(document.getElementById("custom-icon-cropper"), {
        viewport: { width: 128, height: 128 },
        boundary: { width: 200, height: 200 },
        enforceBoundary: false,
        url: url,
    });

    // fix to scale the image correctly
    try {
        croppie.bind({
            zoom: 1,
        });
    } catch (e) {
        // NOP
    }

    return false;
});

/**
 * Set an icon for cropping / preview.
 */
Behaviour.specify('[id^="custom-icon-preview-"]', "CustomIconPreviewSelection", 0, (element) => {
    element.onclick = () => {
        let url = element.src;

        // load icon image
        croppie.bind({
            url: url,
            zoom: 1,
        });

        // reset the name in the upload input element
        document.getElementById("custom-icon-upload").value = "";

        // set the file name - in case you don't crop / upload the image again it will simply be re-used that way
        let paths = url.split("/");
        let icon = paths[paths.length - 1];

        let iconName = document.getElementById("custom-icon-name");
        iconName.setAttribute("value", icon);
        iconName.dispatchEvent(new Event("input"));

        return false;
    };
});

/**
 * Set a file for cropping / preview.
 */
Behaviour.specify('[id^="custom-icon-upload"]', "CustomIconPreview", 0, (element) => {
    element.onchange = () => {
        // read file input
        let reader = new FileReader();
        reader.onload = (event) => {
            croppie.bind({
                url: event.target.result,
                zoom: 1,
            });
        };
        let file = element.files[0];
        reader.readAsDataURL(file);

        return false;
    };
});

/**
 * Upload the cropped icon.
 */
Behaviour.specify('[id^="custom-icon-upload-apply"]', "CustomIconUpload", 0, (element) => {
    element.onclick = () => {
        // get the icon blob
        croppie
            .result("blob")
            .then((blob) => {
                let formData = new FormData();
                formData.append("file", blob);
                return formData;
            })
            .then((formData) => {
                // upload the icon
                let jobUrl = window.location.href.substring(0, window.location.href.lastIndexOf("/"));

                fetch(jobUrl + "/descriptorByName/jenkins.plugins.foldericon.CustomFolderIcon/uploadIcon", {
                    method: "post",
                    headers: crumb.wrap({}),
                    body: formData,
                })
                    .then((response) => {
                        response
                            .text()
                            .then((text) => {
                                let cropper = document.getElementById("custom-icon-cropper");
                                if (response.ok) {
                                    let iconName = document.getElementById("custom-icon-name");
                                    iconName.setAttribute("value", text);
                                    iconName.dispatchEvent(new Event("input"));
                                    hoverNotification("Image uploaded as " + text, cropper);
                                } else {
                                    let error = text.substring(text.lastIndexOf("<title>") + 7, text.lastIndexOf("</title>"));
                                    hoverNotification("Image uploaded failed: " + error, cropper);
                                }
                            })
                            .catch((error) => {
                                console.error(error);
                            });
                    })
                    .catch((error) => {
                        console.error(error);
                    });
            })
            .catch((error) => {
                console.error(error);
            });

        return false;
    };
});
