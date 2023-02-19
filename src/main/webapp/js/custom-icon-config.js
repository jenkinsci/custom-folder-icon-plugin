let croppie

/**
 * Initialization of preview image.
 *
 */
function init() {
    let preview = document.getElementById("file-name").getAttribute("value");
    let url;
    if (preview == null || preview == "") {
        url = rootURL + "/plugin/custom-folder-icon/icons/default.png";
    } else {
        url = rootURL + "/userContent/customFolderIcons/" + preview;
    }

    // init croppie
    croppie = new Croppie(document.getElementById("file-cropper"), {
        viewport: {width: 128, height: 128},
        boundary: {width: 200, height: 200},
        enforceBoundary: false,
        url: url
    });
}

/**
 * Set the file for cropping.
 *
 * @param {Blob} file The file input.
 */
function setFile(file) {
    // read file input
    let reader = new FileReader();
    reader.onload = function (ev) {
        croppie.bind({
            url: ev.target.result
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
function doUploadIcon(successMessage, errorMessage) {
    let request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status == 200) {
                document.getElementById("file-name").setAttribute("value", this.responseText);
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