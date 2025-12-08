package jenkins.plugins.foldericon;

import static org.hamcrest.Matchers.containsString;

import com.cloudbees.hudson.plugins.folder.Folder;
import java.util.logging.Level;
import jenkins.plugins.foldericon.utils.MockCspBuilder;
import jenkins.security.csp.Directive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.LogRecorder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Url Folder Icon Tests
 */
@WithJenkins
class UrlFolderIconContributorTest {

    private static final String ICON_URL = "https://www.jenkins.io/images/logos/jenkins/jenkins.svg";

    private static final MockCspBuilder CSP_BUILDER = new MockCspBuilder();

    private JenkinsRule r;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        r = rule;
    }

    @AfterEach
    void tearDown() {
        CSP_BUILDER.reset();
    }

    /**
     * Test behavior of {@link UrlFolderIcon.UrlFolderIconContributor}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void apply() throws Exception {
        UrlFolderIcon customIcon = new UrlFolderIcon(ICON_URL);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        UrlFolderIcon.UrlFolderIconContributor contributor = new UrlFolderIcon.UrlFolderIconContributor();
        contributor.apply(CSP_BUILDER);

        CSP_BUILDER.assertDirective(Directive.IMG_SRC);
        CSP_BUILDER.assertValues(new String[] {ICON_URL});
    }

    /**
     * Test behavior of {@link UrlFolderIcon.UrlFolderIconContributor}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void applyNullUrl() throws Exception {
        UrlFolderIcon customIcon = new UrlFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        UrlFolderIcon.UrlFolderIconContributor contributor = new UrlFolderIcon.UrlFolderIconContributor();
        contributor.apply(CSP_BUILDER);

        CSP_BUILDER.assertDirective(null);
        CSP_BUILDER.assertValues(null);
    }

    /**
     * Test behavior of {@link UrlFolderIcon.UrlFolderIconContributor}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void applyBlankUrl() throws Exception {
        UrlFolderIcon customIcon = new UrlFolderIcon(" ");
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        UrlFolderIcon.UrlFolderIconContributor contributor = new UrlFolderIcon.UrlFolderIconContributor();
        contributor.apply(CSP_BUILDER);

        CSP_BUILDER.assertDirective(null);
        CSP_BUILDER.assertValues(null);
    }

    /**
     * Test behavior of {@link UrlFolderIcon.UrlFolderIconContributor}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void applyRelativeUrl() throws Exception {
        UrlFolderIcon customIcon = new UrlFolderIcon("relative");
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        UrlFolderIcon.UrlFolderIconContributor contributor = new UrlFolderIcon.UrlFolderIconContributor();
        contributor.apply(CSP_BUILDER);

        CSP_BUILDER.assertDirective(null);
        CSP_BUILDER.assertValues(null);
    }

    /**
     * Test behavior of {@link UrlFolderIcon.UrlFolderIconContributor}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void applyInvalidUrl() throws Exception {
        try (LogRecorder ignored = new LogRecorder().record(UrlFolderIcon.UrlFolderIconContributor.class, Level.FINE)) {
            UrlFolderIcon customIcon = new UrlFolderIcon("https://something.io/^");
            Folder project = r.jenkins.createProject(Folder.class, "folder");
            project.setIcon(customIcon);

            UrlFolderIcon.UrlFolderIconContributor contributor = new UrlFolderIcon.UrlFolderIconContributor();
            contributor.apply(CSP_BUILDER);

            CSP_BUILDER.assertDirective(null);
            CSP_BUILDER.assertValues(null);

            LogRecorder.recorded(containsString("Invalid URL: https://something.io/^"));
        }
    }

    /**
     * Test behavior of {@link UrlFolderIcon.UrlFolderIconContributor}.
     */
    @Test
    void applyNoFolderIcon() {
        UrlFolderIcon.UrlFolderIconContributor contributor = new UrlFolderIcon.UrlFolderIconContributor();
        contributor.apply(CSP_BUILDER);

        CSP_BUILDER.assertDirective(null);
        CSP_BUILDER.assertValues(null);
    }

    /**
     * Test behavior of {@link UrlFolderIcon.UrlFolderIconContributor}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void applyOtherFolderIcon() throws Exception {
        EmojiFolderIcon customIcon = new EmojiFolderIcon("sloth");
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        UrlFolderIcon.UrlFolderIconContributor contributor = new UrlFolderIcon.UrlFolderIconContributor();
        contributor.apply(CSP_BUILDER);

        CSP_BUILDER.assertDirective(null);
        CSP_BUILDER.assertValues(null);
    }
}
