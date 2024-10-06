/**
 * Select the icon for preview.
 * @param {string} icon - The icon name.
 */
function setFontAwesomeIcon(icon) {
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
}
