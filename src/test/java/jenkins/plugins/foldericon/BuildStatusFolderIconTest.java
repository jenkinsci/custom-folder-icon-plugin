package jenkins.plugins.foldericon;

import static jenkins.plugins.foldericon.utils.TestUtils.mockStaplerRequest;
import static jenkins.plugins.foldericon.utils.TestUtils.validateIcon;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import hudson.model.BallColor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.util.Collections;
import java.util.Set;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.BuildStatusFolderIcon.DescriptorImpl;
import jenkins.plugins.foldericon.utils.DelayBuilder;
import jenkins.plugins.foldericon.utils.ResultBuilder;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.Stapler;
import org.mockito.MockedStatic;

/**
 * Build Status Folder Icon Tests
 */
@WithJenkins
class BuildStatusFolderIconTest {

    private JenkinsRule r;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        r = rule;
    }

    /**
     * Test behavior of {@link BuildStatusFolderIcon#getAvailableJobs()}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void getAvailableJobs() throws Exception {
        Folder project = r.jenkins.createProject(Folder.class, "folder");

        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        project.setIcon(customIcon);

        assertTrue(customIcon.getAvailableJobs().isEmpty());

        project.createProject(FreeStyleProject.class, "Success");
        project.createProject(FreeStyleProject.class, "Aborted");

        assertEquals(2, customIcon.getAvailableJobs().size());
        assertEquals(Set.of("Aborted", "Success"), customIcon.getAvailableJobs());
    }

    /**
     * Test behavior when there is a configuration for jobs to consider.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void withConfiguredJobs() throws Exception {
        Folder project = r.jenkins.createProject(Folder.class, "folder");

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            mockStaplerRequest(stapler);

            // Setup
            FreeStyleProject success = project.createProject(FreeStyleProject.class, "Success");
            FreeStyleBuild successBuild = success.scheduleBuild2(0).get();
            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(successBuild));

            FreeStyleProject aborted = project.createProject(FreeStyleProject.class, "Aborted");
            aborted.getBuildersList().replaceBy(Collections.singleton(new ResultBuilder(Result.ABORTED)));
            r.buildAndAssertStatus(Result.ABORTED, aborted);

            Folder subfolder = project.createProject(Folder.class, "subfolder");
            FreeStyleProject nested = subfolder.createProject(FreeStyleProject.class, "Nested");
            nested.getBuildersList().replaceBy(Collections.singleton(new ResultBuilder(Result.NOT_BUILT)));
            r.buildAndAssertStatus(Result.NOT_BUILT, nested);

            // Validate

            BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(Set.of());
            project.setIcon(customIcon);
            FolderIcon icon = project.getIcon();
            validateIcon(icon, BallColor.ABORTED.getImage(), BallColor.ABORTED.getIconClassName());

            customIcon = new BuildStatusFolderIcon(Set.of("Success"));
            project.setIcon(customIcon);
            icon = project.getIcon();
            validateIcon(icon, BallColor.BLUE.getImage(), BallColor.BLUE.getIconClassName());

            customIcon = new BuildStatusFolderIcon(Set.of("Success", "Aborted"));
            project.setIcon(customIcon);
            icon = project.getIcon();
            validateIcon(icon, BallColor.ABORTED.getImage(), BallColor.ABORTED.getIconClassName());

            customIcon = new BuildStatusFolderIcon(Set.of("subfolder » Nested"));
            project.setIcon(customIcon);
            icon = project.getIcon();
            validateIcon(icon, BallColor.NOTBUILT.getImage(), BallColor.NOTBUILT.getIconClassName());

            customIcon = new BuildStatusFolderIcon(Set.of("doesnotexist"));
            project.setIcon(customIcon);
            icon = project.getIcon();
            validateIcon(icon, BallColor.ABORTED.getImage(), BallColor.ABORTED.getIconClassName());

            customIcon = new BuildStatusFolderIcon(Set.of("Success", "doesnotexist"));
            project.setIcon(customIcon);
            icon = project.getIcon();
            validateIcon(icon, BallColor.BLUE.getImage(), BallColor.BLUE.getIconClassName());
        }
    }

    /**
     * Test behavior on a regular {@link Folder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void folder() throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(BuildStatusFolderIcon.class, icon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void organizationFolder() throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(BuildStatusFolderIcon.class, icon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void descriptor() {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertEquals(Messages.BuildStatusFolderIcon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));
    }

    /**
     * Test behavior of possible {@link Result}s on finished builds.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void finishedBuildStatusIcon() throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(BuildStatusFolderIcon.class, icon);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            mockStaplerRequest(stapler);

            // default
            validateIcon(icon, BallColor.NOTBUILT.getImage(), BallColor.NOTBUILT.getIconClassName());

            // Success
            FreeStyleProject success = project.createProject(FreeStyleProject.class, "Success");
            FreeStyleBuild successBuild = success.scheduleBuild2(0).get();
            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(successBuild));

            validateIcon(icon, BallColor.BLUE.getImage(), BallColor.BLUE.getIconClassName());
            // Unstable
            FreeStyleProject unstable = project.createProject(FreeStyleProject.class, "Unstable");
            unstable.getBuildersList().replaceBy(Collections.singleton(new ResultBuilder(Result.UNSTABLE)));
            r.buildAndAssertStatus(Result.UNSTABLE, unstable);

            validateIcon(icon, BallColor.YELLOW.getImage(), BallColor.YELLOW.getIconClassName());

            // Failure
            FreeStyleProject failure = project.createProject(FreeStyleProject.class, "Failure");
            failure.getBuildersList().replaceBy(Collections.singleton(new ResultBuilder(Result.FAILURE)));
            r.buildAndAssertStatus(Result.FAILURE, failure);

            validateIcon(icon, BallColor.RED.getImage(), BallColor.RED.getIconClassName());

            // Not build
            FreeStyleProject notBuilt = project.createProject(FreeStyleProject.class, "Not Built");
            notBuilt.getBuildersList().replaceBy(Collections.singleton(new ResultBuilder(Result.NOT_BUILT)));
            r.buildAndAssertStatus(Result.NOT_BUILT, notBuilt);

            validateIcon(icon, BallColor.NOTBUILT.getImage(), BallColor.NOTBUILT.getIconClassName());

            // Aborted
            FreeStyleProject aborted = project.createProject(FreeStyleProject.class, "Aborted");
            aborted.getBuildersList().replaceBy(Collections.singleton(new ResultBuilder(Result.ABORTED)));
            r.buildAndAssertStatus(Result.ABORTED, aborted);

            validateIcon(icon, BallColor.ABORTED.getImage(), BallColor.ABORTED.getIconClassName());
        }
    }

    /**
     * Test behavior of possible {@link Result}s on running builds.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void runningBuildStatusIcon() throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(BuildStatusFolderIcon.class, icon);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            mockStaplerRequest(stapler);

            // Previous Success
            FreeStyleProject success = project.createProject(FreeStyleProject.class, "Success");
            FreeStyleBuild successBuild = success.scheduleBuild2(0).get();
            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(successBuild));

            validateIcon(icon, BallColor.BLUE.getImage(), BallColor.BLUE.getIconClassName());

            // Running Build
            DelayBuilder builder = new DelayBuilder();
            success.getBuildersList().replaceBy(Collections.singleton(builder));
            FreeStyleBuild runningBuild =
                    success.scheduleBuild2(0).getStartCondition().get();

            validateIcon(icon, BallColor.BLUE_ANIME.getImage(), BallColor.BLUE_ANIME.getIconClassName());
            builder.release();

            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(runningBuild));
        }
    }

    /**
     * Test behavior of possible {@link Result}s on running builds.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void runningNoPreviousBuildStatusIcon() throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(BuildStatusFolderIcon.class, icon);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            mockStaplerRequest(stapler);

            // Running Build
            FreeStyleProject running = project.createProject(FreeStyleProject.class, "Running");
            DelayBuilder builder = new DelayBuilder();
            running.getBuildersList().replaceBy(Collections.singleton(builder));
            FreeStyleBuild runningBuild =
                    running.scheduleBuild2(0).getStartCondition().get();

            validateIcon(icon, BallColor.NOTBUILT_ANIME.getImage(), BallColor.NOTBUILT_ANIME.getIconClassName());
            builder.release();

            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(runningBuild));
        }
    }

    /**
     * Test behavior of possible {@link Result}s on disabled builds.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void disabledBuildStatusIcon() throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(BuildStatusFolderIcon.class, icon);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            mockStaplerRequest(stapler);

            // Disabled Build
            FreeStyleProject disabled = project.createProject(FreeStyleProject.class, "Disabled");
            disabled.setDisabled(true);

            assertFalse(disabled.isBuildable());
            assertTrue(disabled.isDisabled());
            validateIcon(icon, BallColor.DISABLED.getImage(), BallColor.DISABLED.getIconClassName());
        }
    }

    /**
     * Test behavior of possible {@link Result}s on no builds.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void noBuildStatusIcon() throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(BuildStatusFolderIcon.class, icon);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            mockStaplerRequest(stapler);

            // No Build
            project.createProject(FreeStyleProject.class, "No Build");

            validateIcon(icon, BallColor.NOTBUILT.getImage(), BallColor.NOTBUILT.getIconClassName());
        }
    }
}
