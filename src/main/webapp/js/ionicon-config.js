/**
 * Set the icon value in the dropdown.
 *
 * @param {string} icon - The value.
 */
function setIcon(icon) {
    let dropdown = document.getElementById("ionicon");

    if(icon == null || icon == "") {
        icon = "jenkins";

        dropdown.setAttribute("value", icon);
        dropdown.setAttribute("selected", icon);
        dropdown.dispatchEvent(new Event("change"));
    } else {
        for (let i = 0; i < dropdown.options.length; i++) {
            if (dropdown.options[i].value == icon) {
                dropdown.options[i].selected = true;
                break;
            }
        }
    }
}