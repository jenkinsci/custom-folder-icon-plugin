/**
 * Select the icon for preview.
 * @param {string} icon - The icon name.
 */
function setIoniconIcon(icon) {
    if (icon == null || icon === "") {
        return;
    }

    let iconName = document.getElementById("ionicon-icon-name");
    iconName.setAttribute("value", icon);
    iconName.dispatchEvent(new Event("input"));

    let oldPreview = document.getElementById("ionicon-preview");
    let selectedIcon = document.getElementById("ionicon-icon-" + icon);

    let newPreview = selectedIcon.cloneNode(true);
    newPreview.id = "ionicon-preview";
    newPreview.classList.replace("icon-md", "icon-xlg");

    oldPreview.parentElement.replaceChild(newPreview, oldPreview);
}
