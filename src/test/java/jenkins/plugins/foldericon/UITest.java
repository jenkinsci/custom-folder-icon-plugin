package jenkins.plugins.foldericon;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.FilePath;
import java.time.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Various UI Tests
 */
@WithJenkins
class UITest {

    /**
     * Test behavior of the CustomFolderIconConfiguration.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void testCustomFolderIconGlobalConfiguration(JenkinsRule r) throws Throwable {
        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
        FilePath iconDir = userContent.child(CustomFolderIconConfiguration.PLUGIN_PATH);
        iconDir.mkdirs();

        long timestamp = System.currentTimeMillis();
        String filename = timestamp + ".png";
        FilePath file = iconDir.child(filename);
        file.touch(timestamp);

        try (JenkinsRule.WebClient webClient = r.createWebClient()) {
            HtmlPage appearance = webClient.goTo("manage/appearance");
            assertTrue(StringUtils.contains(appearance.getVisibleText(), "Custom Folder Icons"));
            assertTrue(StringUtils.contains(
                    appearance.getVisibleText(),
                    "Disk usage of icons:   " + FileUtils.byteCountToDisplaySize(file.length())));

            appearance.getElementsByTagName("input").stream()
                    .filter(input -> StringUtils.equals(input.getAttribute("value"), "Cleanup unused icons"))
                    .findFirst()
                    .orElseThrow(() -> fail("Unable to cleanup unused icons"))
                    .click();

            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                while (file.exists()) {
                    Thread.sleep(100);
                }
            });
            assertFalse(file.exists());

            appearance.refresh();
            assertTrue(StringUtils.contains(
                    appearance.getVisibleText(), "Disk usage of icons:   " + FileUtils.byteCountToDisplaySize(0)));
        }
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void testBuildStatusFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.BuildStatusFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void testCustomFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.CustomFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void testEmojiFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.EmojiFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void testFontAwesomeFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.FontAwesomeFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void testIoniconFolderIconOption(JenkinsRule r) throws Throwable {
        selectFolderIconOption(r, Messages.IoniconFolderIcon_description());
    }

    /**
     * Test behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void testOpenSourceFolderIconOption(JenkinsRule r) throws Throwable {
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

            selection.click();
            r.submit(form);

            configure.refresh();
            assertTrue(selection.isSelected());
        }
    }
}
