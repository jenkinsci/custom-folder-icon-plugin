/**
 * Set the emoji src in the preview.
 *
 * @param {string} key - The emoji key.
 * @param {string} emoji - The unicode emoji.
 */
function setEmoji(key, emoji) {
    if(key == null || key == "") {
        key = "sloth";
        emoji = "🦥";
    }

    let emoji_field = document.getElementById("emoji");
    emoji_field.setAttribute("value", key);
    emoji_field.dispatchEvent(new Event("change"));

    let preview = document.getElementById("preview")
    preview.setValue(emoji);
    preview.setAttribute("tooltip", key);
    preview.dispatchEvent(new Event("change"));
}