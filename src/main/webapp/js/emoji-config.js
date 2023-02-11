/**
 * Set the emoji src in the preview.
 *
 * @param {string} key - The emoji key.
 * @param {string} emoji - The unicode emoji.
 */
function setEmoji(key, emoji) {
    document.getElementById('emoji').setAttribute("value", key);

    let preview = document.getElementById('preview')
    preview.setValue(emoji);
    preview.dispatchEvent(new Event("change"));
}