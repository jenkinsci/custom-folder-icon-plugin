/**
 * Set the icon value in the dropdown.
 *
 * @param {string} icon - The value.
 */
function setIcon(icon) {
    let dropdown = document.getElementById("ionicon-name");

    if(icon == null || icon == "") {
        icon = "jenkins";
        dropdown.setAttribute("value", icon);
        dropdown.setAttribute("selected", icon);
    } else {
        for (let option of dropdown.options) {
            if (option.value == icon) {
                option.selected = true;
                break;
            }
        }
    }
    dropdown.dispatchEvent(new Event("change"));
}