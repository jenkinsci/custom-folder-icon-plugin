package jenkins.plugins.foldericon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BallColor;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.BuildStatusFolderIcon.DescriptorImpl;

/**
 * Custom Folder Icon Tests
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

	customIcon = ((BuildStatusFolderIcon) icon);
	assertTrue(StringUtils.startsWith(customIcon.getDescription(), project.getPronoun()));
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

	customIcon = ((BuildStatusFolderIcon) icon);
	assertTrue(StringUtils.startsWith(customIcon.getDescription(), project.getPronoun()));
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

	customIcon = ((BuildStatusFolderIcon) icon);

	try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
	    StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);
	    stapler.when(Stapler::getCurrentRequest).thenReturn(mockReq);
	    Mockito.when(mockReq.getContextPath()).thenReturn("/jenkins");

	    // default
	    String image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.NOTBUILT.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    String iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.NOTBUILT.getIconClassName()));

	    // Success
	    FreeStyleProject success = project.createProject(FreeStyleProject.class, "Success");
	    FreeStyleBuild successBuild = success.scheduleBuild2(0).get();
	    r.assertBuildStatus(Result.SUCCESS, successBuild);

	    image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.BLUE.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.BLUE.getIconClassName()));

	    // Unstable
	    FreeStyleProject unstable = project.createProject(FreeStyleProject.class, "Unstable");
	    unstable.getPublishersList().replaceBy(Collections.singleton(new ResultPublisher(Result.UNSTABLE)));
	    r.buildAndAssertStatus(Result.UNSTABLE, unstable);

	    image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.YELLOW.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.YELLOW.getIconClassName()));

	    // Failure
	    FreeStyleProject failure = project.createProject(FreeStyleProject.class, "Failure");
	    failure.getPublishersList().replaceBy(Collections.singleton(new ResultPublisher(Result.FAILURE)));
	    r.buildAndAssertStatus(Result.FAILURE, failure);

	    image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.RED.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.RED.getIconClassName()));

	    // Not build
	    FreeStyleProject notBuilt = project.createProject(FreeStyleProject.class, "Not Built");
	    notBuilt.getPublishersList().replaceBy(Collections.singleton(new ResultPublisher(Result.NOT_BUILT)));
	    r.buildAndAssertStatus(Result.NOT_BUILT, notBuilt);

	    image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.NOTBUILT.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.NOTBUILT.getIconClassName()));

	    // Aborted
	    FreeStyleProject aborted = project.createProject(FreeStyleProject.class, "Aborted");
	    aborted.getPublishersList().replaceBy(Collections.singleton(new ResultPublisher(Result.ABORTED)));
	    r.buildAndAssertStatus(Result.ABORTED, aborted);

	    image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.ABORTED.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.ABORTED.getIconClassName()));
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

	customIcon = ((BuildStatusFolderIcon) icon);

	try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
	    StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);
	    stapler.when(Stapler::getCurrentRequest).thenReturn(mockReq);
	    Mockito.when(mockReq.getContextPath()).thenReturn("/jenkins");

	    // Previous Success
	    FreeStyleProject success = project.createProject(FreeStyleProject.class, "Success");
	    FreeStyleBuild successBuild = success.scheduleBuild2(0).get();
	    r.assertBuildStatus(Result.SUCCESS, successBuild);

	    String image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.BLUE.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    String iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.BLUE.getIconClassName()));

	    // Running Build
	    success.getBuildersList().replaceBy(Collections.singleton(new DelayBuilder()));
	    QueueTaskFuture<FreeStyleBuild> runningBuild = success.scheduleBuild2(0);

	    runningBuild.getStartCondition().get();

	    image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.BLUE_ANIME.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.BLUE_ANIME.getIconClassName()));
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

	customIcon = ((BuildStatusFolderIcon) icon);

	try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
	    StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);
	    stapler.when(Stapler::getCurrentRequest).thenReturn(mockReq);
	    Mockito.when(mockReq.getContextPath()).thenReturn("/jenkins");

	    // Running Build
	    FreeStyleProject running = project.createProject(FreeStyleProject.class, "Running");
	    running.getBuildersList().replaceBy(Collections.singleton(new DelayBuilder()));
	    QueueTaskFuture<FreeStyleBuild> runningBuild = running.scheduleBuild2(0);

	    runningBuild.getStartCondition().get();

	    String image = customIcon.getImageOf("42");
	    assertTrue(StringUtils.endsWith(image, BallColor.NOTBUILT_ANIME.getImage()));
	    assertTrue(StringUtils.contains(image, "/jenkins"));
	    assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));

	    String iconClass = customIcon.getIconClassName();
	    assertTrue(StringUtils.endsWith(iconClass, BallColor.NOTBUILT_ANIME.getIconClassName()));
	}
    }

    static class DelayBuilder extends Builder {

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
		throws InterruptedException, IOException {
	    Thread.sleep(5000);
	    return true;
	}
    }

    static class ResultPublisher extends Publisher {
	private final Result result;

	@SuppressWarnings("deprecation")
	ResultPublisher(Result result) {
	    this.result = result;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
	    build.setResult(result);
	    return true;
	}

	@Override
	public Descriptor<Publisher> getDescriptor() {
	    return new Descriptor<Publisher>(ResultPublisher.class) {
	    };
	}
    }
}
