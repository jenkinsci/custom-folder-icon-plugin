/*
 * The MIT License
 *
 * Copyright (c) 2023 strangelookingnerd
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

let croppie

/**
 * Initialization of preview image.
 *
 */
function initCustomIcon() {
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
}

/**
 * Set an icon for cropping / preview.
 *
 * @param {string} url The icon url.
 */
function setCustomIcon(url) {
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
}


/**
 * Set a file for cropping / preview.
 *
 * @param {Blob} file The file input.
 */
function setCustomIconFile(file) {
    // read file input
    let reader = new FileReader();
    reader.onload = function (ev) {
        croppie.bind({
            url: ev.target.result,
            zoom: 1
        });
    }
    reader.readAsDataURL(file);
}

/**
 * Upload the cropped icon.
 *
 * @param {string} successMessage - The success message.
 * @param {string} errorMessage - The error message.
 */
function doUploadCustomIcon(successMessage, errorMessage) {
    let request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                let iconName = document.getElementById("custom-icon-name")
                iconName.setAttribute("value", this.responseText);
                iconName.dispatchEvent(new Event("input"));
                alert(successMessage + " " + this.responseText);
            } else {
                let error = this.responseText.substring(this.responseText.lastIndexOf("<title>") + 7,
                    this.responseText.lastIndexOf("</title>"));
                alert(errorMessage + " " + error);
            }
        }
    };

    request.open("POST", rootURL + "/descriptor/jenkins.plugins.foldericon.CustomFolderIcon/uploadIcon");

    // get a crumb
    new Ajax.Request(rootURL + "/crumbIssuer/api/json", {
        method: "GET",
        onSuccess: function (req) {
            let jsonResponse = JSON.parse(req.transport.response);
            let header = jsonResponse.crumbRequestField;
            let value = jsonResponse.crumb;
            request.setRequestHeader(header, value);
        },
        onComplete: function () {
            // upload the file
            let formData = new FormData();
            croppie.result("blob").then(function (blob) {
                formData.append("file", blob);
                request.send(formData);
            });
        }
    });
}
