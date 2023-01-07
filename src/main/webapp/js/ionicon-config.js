/**
 * Set the icon value in the dropdown.
 *
 * @param {string} icon - The value.
 */
function setIcon(icon) {
    let dropdown = document.getElementById('ionicon')
    dropdown.setValue(icon);
    dropdown.dispatchEvent(new Event("change"));
}