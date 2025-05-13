package jenkins.plugins.foldericon;

import static jenkins.plugins.foldericon.utils.TestUtils.createCustomIconFile;
import static org.htmlunit.WebAssert.assertTextPresent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.FilePath;
import java.time.Duration;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Various UI Tests.
 */
@WithJenkins
class UITest {

    private JenkinsRule r;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        r = rule;
    }

    static Stream<String> options() {
        return Stream.of(
                Messages.BuildStatusFolderIcon_description(),
                Messages.CustomFolderIcon_description(),
                Messages.EmojiFolderIcon_description(),
                Messages.FontAwesomeFolderIcon_description(),
                Messages.IoniconFolderIcon_description(),
                Messages.OpenSourceFolderIcon_description(),
                Messages.UrlFolderIcon_description());
    }

    /**
     * Test the behavior of the folder icon option selection.
     *
     * @throws Throwable in case anything goes wrong
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("options")
    void selectFolderIconOption(String folderIconOption) throws Throwable {
        Folder project = r.jenkins.createProject(Folder.class, "folder");

        try (JenkinsRule.WebClient webClient = r.createWebClient()) {
            HtmlPage configure = webClient.getPage(project, "configure");
            HtmlForm form = configure.getFormByName("config");

            HtmlOption selection = (HtmlOption) configure.getElementsByTagName("option").stream()
                    .filter(option -> StringUtils.equals(option.getTextContent(), folderIconOption))
                    .findFirst()
                    .orElseThrow(() -> fail("Unable to select folder icon option " + folderIconOption));

            assertFalse(selection.isSelected());
            configure = selection.click();
            assertTrue(selection.isSelected());
            r.submit(form);

            configure = (HtmlPage) configure.refresh();
            selection = (HtmlOption) configure.getElementsByTagName("option").stream()
                    .filter(option -> StringUtils.equals(option.getTextContent(), folderIconOption))
                    .findFirst()
                    .orElseThrow(() -> fail("Unable to select folder icon option " + folderIconOption));

            assertTrue(selection.isSelected());
        }
    }

    /**
     * Test the behavior of the CustomFolderIconConfiguration.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void customFolderIconGlobalConfiguration() throws Throwable {
        FilePath file = createCustomIconFile(r);

        try (JenkinsRule.WebClient webClient = r.createWebClient()) {
            HtmlPage appearance = webClient.goTo("manage/appearance");
            assertTextPresent(appearance, "Custom Folder Icons");
            assertTextPresent(appearance, "Disk usage of icons:   " + FileUtils.byteCountToDisplaySize(file.length()));

            appearance.getElementsByTagName("button").stream()
                    .filter(button -> StringUtils.equals(button.getTextContent(), "Cleanup unused icons"))
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
            assertTextPresent(appearance, "Disk usage of icons:   " + FileUtils.byteCountToDisplaySize(0L));
        }
    }

    /**
     * Test the behavior of croppie.js.
     *
     * @throws Throwable in case anything goes wrong
     */
    @Test
    void customFolderIconCroppieLoaded() throws Throwable {
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
            selection.click();
            assertTrue(selection.isSelected());
            r.submit(form);

            configure = webClient.getPage(project, "configure");
            DomElement cropper = configure.getElementById("custom-icon-cropper");
            assertNotNull(cropper);

            String src = null;
            if (!cropper.getElementsByTagName("img").isEmpty()) {
                DomElement image = cropper.getElementsByTagName("img").get(0);
                src = image.getAttribute("src");
            }
            assertNotNull(src);

            assertEquals("/jenkins/plugin/custom-folder-icon/icons/default.svg", src);
        }
    }
}
