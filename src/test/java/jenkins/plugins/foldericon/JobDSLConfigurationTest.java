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
import hudson.FilePath;
import hudson.model.*;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to configure folders via Job DSL plugin.
 *
 * @author strangelookingnerd
 */
@WithJenkins
class JobDSLConfigurationTest {

    @Test
    void testBuildStatusFolderIcon(JenkinsRule r) throws Exception {
        BuildStatusFolderIcon customIcon = createFolder(r, "build-status.groovy", BuildStatusFolderIcon.class);
        assertEquals(Set.of("dev", "main"), customIcon.getJobs());
        assertEquals(BallColor.NOTBUILT.getIconClassName(), customIcon.getIconClassName());
    }

    @Test
    void testCustomIconFolderIcon(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = createFolder(r, "custom-icon.groovy", CustomFolderIcon.class);
        assertEquals("custom.png", customIcon.getFoldericon());
        assertEquals(Set.of("custom.png"), CustomFolderIcon.getAvailableIcons());
    }

    @Test
    void testEmojiFolderIcon(JenkinsRule r) throws Exception {
        EmojiFolderIcon customIcon = createFolder(r, "emoji-icon.groovy", EmojiFolderIcon.class);
        assertEquals("sloth", customIcon.getEmoji());
    }

    @Test
    void testFontAwesomeFolderIcon(JenkinsRule r) throws Exception {
        FontAwesomeFolderIcon customIcon = createFolder(r, "fontawesome-icon.groovy", FontAwesomeFolderIcon.class);
        assertEquals("brands/jenkins", customIcon.getFontAwesome());
    }

    @Test
    void testIoniconFolderIcon(JenkinsRule r) throws Exception {
        IoniconFolderIcon customIcon = createFolder(r, "ionicon-icon.groovy", IoniconFolderIcon.class);
        assertEquals("jenkins", customIcon.getIonicon());
    }

    @SuppressWarnings("unchecked")
    private static <T extends FolderIcon> T createFolder(JenkinsRule r, String scriptName, Class<T> clazz) throws Exception {
        // setup
        FreeStyleProject job = r.jenkins.createProject(FreeStyleProject.class, "Job DSL Configuration");

        if (clazz.equals(CustomFolderIcon.class)) {
            FreeStyleBuild build = job.scheduleBuild2(0).getStartCondition().get();
            r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(build));

            FilePath workspace = job.getSomeWorkspace();
            assertNotNull(workspace);
            workspace.child("custom.png").touch(System.currentTimeMillis());
        }

        URL url = JobDSLConfigurationTest.class.getClassLoader().getResource(scriptName);
        assertNotNull(url);
        String script = Files.readString(Path.of(url.toURI()), StandardCharsets.UTF_8);

        ExecuteDslScripts dslBuildStep = new ExecuteDslScripts();
        dslBuildStep.setUseScriptText(true);
        dslBuildStep.setScriptText(script);

        job.getBuildersList().replaceBy(Collections.singleton(dslBuildStep));

        FreeStyleBuild build = job.scheduleBuild2(0).getStartCondition().get();
        r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(build));

        // validate
        Item item = r.getInstance().getItem(StringUtils.substringBefore(scriptName, '.'));
        assertNotNull(item);

        assertInstanceOf(Folder.class, item);
        Folder folder = (Folder) item;

        FolderIcon icon = folder.getIcon();
        assertNotNull(icon);
        assertTrue(clazz.isInstance(icon));

        return (T) icon;
    }
}
