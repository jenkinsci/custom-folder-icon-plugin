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

    let emojiName = document.getElementById("emoji-name");
    emojiName.setAttribute("value", key);
    emojiName.dispatchEvent(new Event("change"));

    let preview = document.getElementById("preview");
    preview.setAttribute("value", emoji);
    preview.setAttribute("title", key);
    preview.setAttribute("tooltip", key);
}