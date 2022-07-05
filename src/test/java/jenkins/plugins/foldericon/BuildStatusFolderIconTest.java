package jenkins.plugins.foldericon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.Stapler;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;

import hudson.model.BallColor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.BuildStatusFolderIcon.DescriptorImpl;
import jenkins.plugins.foldericon.utils.DelayBuilder;
import jenkins.plugins.foldericon.utils.ResultPublisher;
import jenkins.plugins.foldericon.utils.TestUtils;

/**
 * Build Status Folder Icon Tests
 * 
 * @author strangelookingnerd
 *
 */
public class BuildStatusFolderIconTest {

    /**
     * The rule.
     */
    @Rule
    public JenkinsRule r = new JenkinsRule();

    /**
     * Test behavior on a regular {@link Folder}.
     * 
     * @throws Exception
     */
    @Test
    public void testFolder() throws Exception {
	BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
	assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));

	Folder project = r.jenkins.createProject(Folder.class, "folder");
	project.setIcon(customIcon);
	FolderIcon icon = project.getIcon();

	assertTrue(icon instanceof BuildStatusFolderIcon);
	assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     * 
     * @throws Exception
     */
    @Test
    public void testOrganzationFolder() throws Exception {
	BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
	assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));

	OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
	project.setIcon(customIcon);
	FolderIcon icon = project.getIcon();

	assertTrue(icon instanceof BuildStatusFolderIcon);
	assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     * 
     * @throws Exception
     */
    @Test
    public void testDescriptor() throws Exception {
	BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
	DescriptorImpl descriptor = customIcon.getDescriptor();
	assertEquals(Messages.BuildStatusFolderIcon_description(), descriptor.getDisplayName());
	assertTrue(descriptor.isApplicable(null));
    }

    /**
     * Test behavior of possible {@link Result}s on finished builds.
     * 
     * @throws Exception
     */
    @Test
    public void testFinishedBuildStatusIcon() throws Exception {
	BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
	Folder project = r.jenkins.createProject(Folder.class, "folder");
	project.setIcon(customIcon);
	FolderIcon icon = project.getIcon();

	assertTrue(icon instanceof BuildStatusFolderIcon);

	try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
	    TestUtils.mockStaplerRequest(stapler);

	    // default
	    TestUtils.validateIcon(icon, BallColor.NOTBUILT.getImage(), BallColor.NOTBUILT.getIconClassName());

	    // Success
	    FreeStyleProject success = project.createProject(FreeStyleProject.class, "Success");
	    FreeStyleBuild successBuild = success.scheduleBuild2(0).get();
	    r.assertBuildStatus(Result.SUCCESS, successBuild);

	    TestUtils.validateIcon(icon, BallColor.BLUE.getImage(), BallColor.BLUE.getIconClassName());
	    // Unstable
	    FreeStyleProject unstable = project.createProject(FreeStyleProject.class, "Unstable");
	    unstable.getPublishersList().replaceBy(Collections.singleton(new ResultPublisher(Result.UNSTABLE)));
	    r.buildAndAssertStatus(Result.UNSTABLE, unstable);

	    TestUtils.validateIcon(icon, BallColor.YELLOW.getImage(), BallColor.YELLOW.getIconClassName());

	    // Failure
	    FreeStyleProject failure = project.createProject(FreeStyleProject.class, "Failure");
	    failure.getPublishersList().replaceBy(Collections.singleton(new ResultPublisher(Result.FAILURE)));
	    r.buildAndAssertStatus(Result.FAILURE, failure);

	    TestUtils.validateIcon(icon, BallColor.RED.getImage(), BallColor.RED.getIconClassName());

	    // Not build
	    FreeStyleProject notBuilt = project.createProject(FreeStyleProject.class, "Not Built");
	    notBuilt.getPublishersList().replaceBy(Collections.singleton(new ResultPublisher(Result.NOT_BUILT)));
	    r.buildAndAssertStatus(Result.NOT_BUILT, notBuilt);

	    TestUtils.validateIcon(icon, BallColor.NOTBUILT.getImage(), BallColor.NOTBUILT.getIconClassName());

	    // Aborted
	    FreeStyleProject aborted = project.createProject(FreeStyleProject.class, "Aborted");
	    aborted.getPublishersList().replaceBy(Collections.singleton(new ResultPublisher(Result.ABORTED)));
	    r.buildAndAssertStatus(Result.ABORTED, aborted);

	    TestUtils.validateIcon(icon, BallColor.ABORTED.getImage(), BallColor.ABORTED.getIconClassName());
	}

    }

    /**
     * Test behavior of possible {@link Result}s on running builds.
     * 
     * @throws Exception
     */
    @Test
    public void testRunningBuildStatusIcon() throws Exception {
	BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
	Folder project = r.jenkins.createProject(Folder.class, "folder");
	project.setIcon(customIcon);
	FolderIcon icon = project.getIcon();

	assertTrue(icon instanceof BuildStatusFolderIcon);

	try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
	    TestUtils.mockStaplerRequest(stapler);

	    // Previous Success
	    FreeStyleProject success = project.createProject(FreeStyleProject.class, "Success");
	    FreeStyleBuild successBuild = success.scheduleBuild2(0).get();
	    r.assertBuildStatus(Result.SUCCESS, successBuild);

	    TestUtils.validateIcon(icon, BallColor.BLUE.getImage(), BallColor.BLUE.getIconClassName());

	    // Running Build
	    success.getBuildersList().replaceBy(Collections.singleton(new DelayBuilder()));
	    success.scheduleBuild2(0).getStartCondition().get();

	    TestUtils.validateIcon(icon, BallColor.BLUE_ANIME.getImage(), BallColor.BLUE_ANIME.getIconClassName());
	}
    }

    /**
     * Test behavior of possible {@link Result}s on running builds.
     * 
     * @throws Exception
     */
    @Test
    public void testRunningNoPreviousBuildStatusIcon() throws Exception {
	BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
	Folder project = r.jenkins.createProject(Folder.class, "folder");
	project.setIcon(customIcon);
	FolderIcon icon = project.getIcon();

	assertTrue(icon instanceof BuildStatusFolderIcon);

	try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
	    TestUtils.mockStaplerRequest(stapler);

	    // Running Build
	    FreeStyleProject running = project.createProject(FreeStyleProject.class, "Running");
	    running.getBuildersList().replaceBy(Collections.singleton(new DelayBuilder()));
	    running.scheduleBuild2(0).getStartCondition().get();

	    TestUtils.validateIcon(icon, BallColor.NOTBUILT_ANIME.getImage(), BallColor.NOTBUILT_ANIME.getIconClassName());
	}
    }
}
