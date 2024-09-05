/**
 * Select the icon for preview.
 */
Behaviour.specify("[id^=\"select-emoji-\"]", "EmojiPreviewSelection", 0, element => {
    element.onclick = () => {
        let icon = element.id.replace("select-emoji-", "");

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

        return false;
    }
});
