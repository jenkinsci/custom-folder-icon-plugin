/**
 * Update the icon for preview.
 */
Behaviour.specify('[id="url-icon"]', "UrlPreview", 0, (element) => {
    element.onblur = () => {
        let url = element.value;

        if (url == null || url === "") {
            url = rootURL + "/plugin/custom-folder-icon/icons/default.svg";
        } else if (url.toLowerCase().startsWith("http") === false) {
            return;
        }

        let oldPreview = document.getElementById("url-preview");
        let newPreview = oldPreview.cloneNode(true);

        newPreview.setAttribute("tooltip", url);
        newPreview.setAttribute("title", url);
        newPreview.querySelector("img").src = url;

        oldPreview.parentNode.replaceChild(newPreview, oldPreview);

        return false;
    };
});
