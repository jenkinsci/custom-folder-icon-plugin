/**
 * Select the icon for preview.
 * @param {string} icon - The icon name.
 */
function setEmojiIcon(icon) {
    if (icon == null || icon === "") {
        return;
    }

    let iconName = document.getElementById("emoji-icon-name");
    iconName.setAttribute("value", icon);
    iconName.dispatchEvent(new Event("input"));

    let oldPreview = document.getElementById("emoji-preview");
    let selectedIcon = document.getElementById("emoji-icon-" + icon);

    let newPreview = selectedIcon.cloneNode(true);
    newPreview.id = "emoji-preview";
    newPreview.classList.replace("icon-md", "icon-xlg");

    oldPreview.parentElement.replaceChild(newPreview, oldPreview);
}
