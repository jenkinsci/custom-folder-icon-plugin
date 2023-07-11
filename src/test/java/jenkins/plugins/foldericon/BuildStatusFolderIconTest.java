/*
 * The MIT License
 *
 * Copyright (c) 2023 strangelookingnerd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.foldericon;

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
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.Stapler;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Build Status Folder Icon Tests
 *
 * @author strangelookingnerd
 */
@WithJenkins
class BuildStatusFolderIconTest {

    /**
     * Test behavior on a regular {@link Folder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testFolder(JenkinsRule r) throws Exception {
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
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testOrganizationFolder(JenkinsRule r) throws Exception {
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
     */
    @Test
    void testDescriptor(@SuppressWarnings("unused") JenkinsRule r) {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
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
    void testFinishedBuildStatusIcon(JenkinsRule r) throws Exception {
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
            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(successBuild));

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
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testRunningBuildStatusIcon(JenkinsRule r) throws Exception {
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
            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(successBuild));

            TestUtils.validateIcon(icon, BallColor.BLUE.getImage(), BallColor.BLUE.getIconClassName());

            // Running Build
            DelayBuilder builder = new DelayBuilder();
            success.getBuildersList().replaceBy(Collections.singleton(builder));
            FreeStyleBuild runningBuild = success.scheduleBuild2(0).getStartCondition().get();

            TestUtils.validateIcon(icon, BallColor.BLUE_ANIME.getImage(), BallColor.BLUE_ANIME.getIconClassName());
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
    void testRunningNoPreviousBuildStatusIcon(JenkinsRule r) throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof BuildStatusFolderIcon);

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            TestUtils.mockStaplerRequest(stapler);

            // Running Build
            FreeStyleProject running = project.createProject(FreeStyleProject.class, "Running");
            DelayBuilder builder = new DelayBuilder();
            running.getBuildersList().replaceBy(Collections.singleton(builder));
            FreeStyleBuild runningBuild = running.scheduleBuild2(0).getStartCondition().get();

            TestUtils.validateIcon(icon, BallColor.NOTBUILT_ANIME.getImage(), BallColor.NOTBUILT_ANIME.getIconClassName());
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
    void testDisabledBuildStatusIcon(JenkinsRule r) throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof BuildStatusFolderIcon);

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            TestUtils.mockStaplerRequest(stapler);

            // Disabled Build
            FreeStyleProject disabled = project.createProject(FreeStyleProject.class, "Disabled");
            disabled.setDisabled(true);

            assertFalse(disabled.isBuildable());
            assertTrue(disabled.isDisabled());
            TestUtils.validateIcon(icon, BallColor.DISABLED.getImage(), BallColor.DISABLED.getIconClassName());
        }
    }

    /**
     * Test behavior of possible {@link Result}s on no builds.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testNoBuildStatusIcon(JenkinsRule r) throws Exception {
        BuildStatusFolderIcon customIcon = new BuildStatusFolderIcon();
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof BuildStatusFolderIcon);

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            TestUtils.mockStaplerRequest(stapler);

            // No Build
            project.createProject(FreeStyleProject.class, "No Build");

            TestUtils.validateIcon(icon, BallColor.NOTBUILT.getImage(), BallColor.NOTBUILT.getIconClassName());
        }
    }
}
