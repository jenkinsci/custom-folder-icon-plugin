package jenkins.plugins.foldericon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import hudson.FilePath;
import hudson.model.BallColor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Result;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Test to configure folders via Job DSL plugin.
 */
@WithJenkins
class JobDSLConfigurationTest {

    private JenkinsRule r;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        r = rule;
    }

    /**
     * Test behavior for build-status.groovy.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void buildStatusFolderIcon() throws Exception {
        BuildStatusFolderIcon customIcon = createFolder(r, "build-status.groovy", BuildStatusFolderIcon.class);
        assertThat(customIcon.getJobs(), contains("main", "dev"));
        assertThat(customIcon.getIconClassName(), is("symbol-status-" + BallColor.NOTBUILT.getIconName()));
    }

    /**
     * Test behavior for build-status.groovy.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void customIconFolderIcon() throws Exception {
        CustomFolderIcon customIcon = createFolder(r, "custom-icon.groovy", CustomFolderIcon.class);
        assertThat(customIcon.getFoldericon(), is("custom.png"));
        assertThat(CustomFolderIcon.getAvailableIcons(), contains("custom.png"));
    }

    /**
     * Test behavior for emoji-icon.groovy.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void emojiFolderIcon() throws Exception {
        EmojiFolderIcon customIcon = createFolder(r, "emoji-icon.groovy", EmojiFolderIcon.class);
        assertThat(customIcon.getEmoji(), is("sloth"));
    }

    /**
     * Test behavior for fontawesome-icon.groovy.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void fontAwesomeFolderIcon() throws Exception {
        FontAwesomeFolderIcon customIcon = createFolder(r, "fontawesome-icon.groovy", FontAwesomeFolderIcon.class);
        assertThat(customIcon.getFontAwesome(), is("brands/jenkins"));
    }

    /**
     * Test behavior for ionicon-icon.groovy.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void ioniconFolderIcon() throws Exception {
        IoniconFolderIcon customIcon = createFolder(r, "ionicon-icon.groovy", IoniconFolderIcon.class);
        assertThat(customIcon.getIonicon(), is("jenkins"));
    }

    /**
     * Test behavior for oss-icon.groovy.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void openSourceFolderIcon() throws Exception {
        OpenSourceFolderIcon customIcon = createFolder(r, "opensource-icon.groovy", OpenSourceFolderIcon.class);
        assertThat(customIcon.getOssicon(), is("cdf-icon-color"));
    }

    /**
     * Test behavior for url-icon.groovy.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void urlFolderIcon() throws Exception {
        UrlFolderIcon customIcon = createFolder(r, "url-icon.groovy", UrlFolderIcon.class);
        assertThat(customIcon.getUrl(), is("https://get.jenkins.io/art/jenkins-logo/headshot.svg"));
    }

    @SuppressWarnings("unchecked")
    private static <T extends FolderIcon> T createFolder(JenkinsRule r, String scriptName, Class<T> clazz)
            throws Exception {
        // setup
        FreeStyleProject job = r.jenkins.createProject(FreeStyleProject.class, "Job DSL Configuration");

        if (clazz.equals(CustomFolderIcon.class)) {
            FreeStyleBuild build = job.scheduleBuild2(0).getStartCondition().get();
            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(build));

            FilePath workspace = job.getSomeWorkspace();
            assertThat(workspace, notNullValue());
            workspace.child("custom.png").copyFrom(new FilePath(new File("./src/main/webapp/icons/default.svg")));
        }

        URL url = JobDSLConfigurationTest.class.getClassLoader().getResource(scriptName);
        assertThat(url, notNullValue());
        String script = Files.readString(Path.of(url.toURI()), StandardCharsets.UTF_8);

        ExecuteDslScripts dslBuildStep = new ExecuteDslScripts();
        dslBuildStep.setUseScriptText(true);
        dslBuildStep.setScriptText(script);

        job.getBuildersList().replaceBy(Collections.singleton(dslBuildStep));

        FreeStyleBuild build = job.scheduleBuild2(0).getStartCondition().get();
        r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(build));

        // validate
        Item item = r.getInstance().getItem(scriptName.substring(0, scriptName.indexOf('.')));
        assertThat(item, notNullValue());

        assertThat(item, instanceOf(Folder.class));
        Folder folder = (Folder) item;

        FolderIcon icon = folder.getIcon();
        assertThat(icon, notNullValue());
        assertThat(clazz.isInstance(icon), is(true));

        return (T) icon;
    }
}
