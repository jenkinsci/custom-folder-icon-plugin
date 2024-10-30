/**
 * Select the icon for preview.
 */
Behaviour.specify("[id^=\"select-fontawesome-\"]", "FontAwesomePreviewSelection", 0, element => {
    element.onclick = () => {
        let icon = element.id.replace("select-fontawesome-", "");

        if (icon == null || icon === "") {
            return;
        }

        let iconName = document.getElementById("fontawesome-icon-name");
        iconName.setAttribute("value", icon);
        iconName.dispatchEvent(new Event("input"));

        let oldPreview = document.getElementById("fontawesome-preview");
        let selectedIcon = document.getElementById("fontawesome-icon-" + icon);

        let newPreview = selectedIcon.cloneNode(true);
        newPreview.id = "fontawesome-preview";
        newPreview.classList.replace("icon-md", "icon-xlg");

        oldPreview.parentElement.replaceChild(newPreview, oldPreview);

        return false;
    }
});
