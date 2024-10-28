/*
 * The MIT License
 *
 * Copyright (c) 2024 strangelookingnerd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// global instance
let croppie

/**
 * Initialization of croppie and preview image.
 */
window.addEventListener("DOMContentLoaded", () => {
    let preview = document.getElementById("custom-icon-name").getAttribute("value");
    let url;
    if (preview == null || preview === "") {
        url = rootURL + "/plugin/custom-folder-icon/icons/default.png";
    } else {
        url = rootURL + "/userContent/customFolderIcons/" + preview;
    }

    // init croppie
    croppie = new Croppie(document.getElementById("custom-icon-cropper"), {
        viewport: {width: 128, height: 128},
        boundary: {width: 200, height: 200},
        enforceBoundary: false,
        url: url
    });

    // fix to scale the image correctly
    try {
        croppie.bind({
            zoom: 1
        });
    } catch (e) {
        // NOP
    }

    return false;
});

/**
 * Set an icon for cropping / preview.
 */
Behaviour.specify("[id^=\"custom-icon-preview-\"]", "CustomIconPreviewSelection", 0, element => {
    element.onclick = () => {
        let url = element.src;

        // load icon image
        croppie.bind({
            url: url,
            zoom: 1
        });

        // reset the name in the upload input element
        document.getElementById("custom-icon-upload").value = "";

        // set the file name - in case you don't crop / upload the image again it will simply be re-used that way
        let paths = url.split("/");
        let icon = paths[paths.length - 1];

        let iconName = document.getElementById("custom-icon-name")
        iconName.setAttribute("value", icon);
        iconName.dispatchEvent(new Event("input"));

        return false;
    }
});


/**
 * Set a file for cropping / preview.
 */
Behaviour.specify("[id^=\"custom-icon-upload\"]", "CustomIconPreview", 0, element => {
    element.onchange = () => {
        // read file input
        let reader = new FileReader();
        reader.onload = event => {
            croppie.bind({
                url: event.target.result,
                zoom: 1
            });
        }
        let file = element.files[0];
        reader.readAsDataURL(file);

        return false;
    }
});

/**
 * Upload the cropped icon.
 */
Behaviour.specify("[id^=\"custom-icon-upload-apply\"]", "CustomIconUpload", 0, element => {
    element.onclick = () => {
        // get the icon blob
        croppie.result("blob").then(blob => {
            let formData = new FormData();
            formData.append("file", blob);
            return formData;
        }).then(formData => {
                // upload the icon
                let jobUrl = window.location.href.substring(0, window.location.href.lastIndexOf('/'));

                fetch(jobUrl + "/descriptorByName/jenkins.plugins.foldericon.CustomFolderIcon/uploadIcon", {
                    method: "post",
                    headers: crumb.wrap({}),
                    body: formData
                }).then(response => {
                    response.text().then(text => {
                        let cropper = document.getElementById("custom-icon-cropper")
                        if (response.ok) {
                            let iconName = document.getElementById("custom-icon-name")
                            iconName.setAttribute("value", text);
                            iconName.dispatchEvent(new Event("input"));
                            hoverNotification("Image uploaded as " + text, cropper);
                        } else {
                            let error = text.substring(text.lastIndexOf("<title>") + 7, text.lastIndexOf("</title>"))
                            hoverNotification("Image uploaded failed: " + error, cropper);
                        }
                    }).catch(error => {
                        console.error(error);
                    });
                }).catch(error => {
                    console.error(error);
                });
            }
        ).catch(error => {
            console.error(error);
        });

        return false;
    }
});
