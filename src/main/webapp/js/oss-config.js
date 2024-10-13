/**
 * Select the icon for preview.
 * @param {string} icon - The icon name.
 */
function setOpenSourceIcon(icon) {
    if (icon == null || icon === "") {
        return;
    }

    let iconName = document.getElementById("oss-icon-name");
    iconName.setAttribute("value", icon);
    iconName.dispatchEvent(new Event("input"));

    let oldPreview = document.getElementById("oss-preview");
    let selectedIcon = document.getElementById("oss-icon-" + icon);

    let newPreview = selectedIcon.cloneNode(true);
    newPreview.id = "oss-preview";
    newPreview.classList.replace("icon-md", "icon-xlg");

    oldPreview.parentElement.replaceChild(newPreview, oldPreview);
}
