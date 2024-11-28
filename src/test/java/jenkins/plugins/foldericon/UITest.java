package jenkins.plugins.foldericon;

import static jenkins.plugins.foldericon.utils.TestUtils.createCustomIconFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.FilePath;
import java.time.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.WebAssert;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Various UI Tests.
 */
@WithJenkins
class UITest {

    /**
     * Test behavior of the CustomFolderIconConfiguration.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void customFolderIconGlobalConfiguration(JenkinsRule r) throws Throwable {
        FilePath file = createCustomIconFile(r);

        try (JenkinsRule.WebClient webClient = r.createWebClient()) {
            HtmlPage appearance = webClient.goTo("manage/appearance");
            WebAssert.assertTextPresent(appearance, "Custom Folder Icons");
            WebAssert.assertTextPresent(
                    appearance, "Disk usage of icons:   " + FileUtils.byteCountToDisplaySize(file.length()));

            appearance.getElementsByTagName("input").stream()
                    .filter(input -> StringUtils.equals(input.getAttribute("value"), "Cleanup unused icons"))
                    .findFirst()
                    .orElseThrow(() -> fail("Unable to cleanup unused icons"))
                    .click();

            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                while (file.exists()) {
                    Thread.onSpinWait();
                }
            });
            assertFalse(file.exists());

            appearance = (HtmlPage) appearance.refresh();
            WebAssert.assertTextPresent(appearance, "Disk usage of icons:   " + FileUtils.byteCountToDisplaySize(0L));
        }
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void buildStatusFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.BuildStatusFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void customFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.CustomFolderIcon_description());
    }

    /**
     * Test behavior of croppie.js.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void customFolderIconCroppieLoaded(JenkinsRule r) throws Throwable {
        Folder project = r.jenkins.createProject(Folder.class, "folder");

        try (JenkinsRule.WebClient webClient = r.createWebClient()) {
            HtmlPage configure = webClient.getPage(project, "configure");
            HtmlForm form = configure.getFormByName("config");

            HtmlOption selection = (HtmlOption) configure.getElementsByTagName("option").stream()
                    .filter(option ->
                            StringUtils.equals(option.getTextContent(), Messages.CustomFolderIcon_description()))
                    .findFirst()
                    .orElseThrow(() ->
                            fail("Unable to select folder icon option " + Messages.CustomFolderIcon_description()));

            assertFalse(selection.isSelected());
            configure = selection.click();
            assertTrue(selection.isSelected());
            r.submit(form);

            configure = (HtmlPage) configure.refresh();
            DomElement cropper = configure.getElementById("custom-icon-cropper");
            assertNotNull(cropper);

            for (DomElement element : cropper.getElementsByTagName("img")) {
                System.out.println(element.getAttribute("src"));
            }

            DomElement image = cropper.getElementsByTagName("img").get(0);

            assertNotNull(image);
            assertEquals("/jenkins/plugin/custom-folder-icon/icons/default.svg", image.getAttribute("src"));
        }
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void emojiFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.EmojiFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void fontAwesomeFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.FontAwesomeFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void ioniconFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.IoniconFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void openSourceFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.OpenSourceFolderIcon_description());
    }

    private static void selectFolderIconOption(JenkinsRule r, String folderIcon) throws Throwable {
        Folder project = r.jenkins.createProject(Folder.class, "folder");

        try (JenkinsRule.WebClient webClient = r.createWebClient()) {
            HtmlPage configure = webClient.getPage(project, "configure");
            HtmlForm form = configure.getFormByName("config");

            HtmlOption selection = (HtmlOption) configure.getElementsByTagName("option").stream()
                    .filter(option -> StringUtils.equals(option.getTextContent(), folderIcon))
                    .findFirst()
                    .orElseThrow(() -> fail("Unable to select folder icon option " + folderIcon));

            assertFalse(selection.isSelected());
            configure = selection.click();
            assertTrue(selection.isSelected());
            r.submit(form);

            configure = (HtmlPage) configure.refresh();
            selection = (HtmlOption) configure.getElementsByTagName("option").stream()
                    .filter(option -> StringUtils.equals(option.getTextContent(), folderIcon))
                    .findFirst()
                    .orElseThrow(() -> fail("Unable to select folder icon option " + folderIcon));

            assertTrue(selection.isSelected());
        }
    }
}
