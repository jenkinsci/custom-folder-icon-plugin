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
import hudson.FilePath;
import jenkins.appearance.AppearanceCategory;
import jenkins.branch.OrganizationFolder;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.plugins.foldericon.utils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Custom Folder Icon Configuration Tests
 *
 * @author strangelookingnerd
 */
@WithJenkins
class CustomFolderIconConfigurationTest {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIconConfigurationTest.class.getName());

    private static final String DUMMY_PNG = "dummy.png";

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getCategory()}.
     */
    @Test
    void testGetCategory(@SuppressWarnings("unused") JenkinsRule r) {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        assertEquals(descriptor.getCategory(), GlobalConfigurationCategory.get(AppearanceCategory.class));
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testGetDiskUsage(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        FilePath parent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH).child(CustomFolderIconConfiguration.PLUGIN_PATH);
        parent.mkdirs();

        FilePath file = parent.child(System.currentTimeMillis() + ".png");
        file.touch(System.currentTimeMillis());
        assertTrue(file.exists());

        String usage = descriptor.getDiskUsage();
        assertEquals(FileUtils.byteCountToDisplaySize(file.length()), usage);
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testGetDiskUsageNoIcons(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        FilePath parent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH).child(CustomFolderIconConfiguration.PLUGIN_PATH);
        parent.mkdirs();

        String usage = descriptor.getDiskUsage();
        assertEquals(FileUtils.byteCountToDisplaySize(0L), usage);
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}}.
     */
    @Test
    void testGetDiskUsageNoRoot(@SuppressWarnings("unused") JenkinsRule r) {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        String usage = descriptor.getDiskUsage();
        assertEquals(FileUtils.byteCountToDisplaySize(0L), usage);
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}}.
     */
    @Test
    void testGetDiskUsageWithException(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
        FilePath iconDir = userContent.child(CustomFolderIconConfiguration.PLUGIN_PATH);
        iconDir.mkdirs();

        String filename = System.currentTimeMillis() + ".png";
        FilePath file = iconDir.child(filename);
        file.touch(System.currentTimeMillis());

        try (@SuppressWarnings("unused") MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
            String call = invocation.toString();
            if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                return userContent;
            } else if (StringUtils.equals(call, "filePath.exists();")) {
                return true;
            } else if (StringUtils.equals(call, "filePath.list();")) {
                return List.of(file);
            } else if (StringUtils.equals(call, "filePath.child(\"" + filename + "\");")) {
                throw new IOException("Mocked Exception!");
            }
            return fail("Unexpected invocation '" + call + "' - Test is broken!");
        })) {
            String usage = descriptor.getDiskUsage();
            assertEquals(FileUtils.byteCountToDisplaySize(0L), usage);
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoCleanupNoItems(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath parent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH).child(CustomFolderIconConfiguration.PLUGIN_PATH);
            parent.mkdirs();

            FilePath file = parent.child(System.currentTimeMillis() + ".png");
            file.touch(System.currentTimeMillis());
            assertTrue(file.exists());
            HttpResponse response = descriptor.doCleanup(mockReq);

            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertFalse(file.exists());
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoCleanupMissingIcon(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        FilePath parent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH).child(CustomFolderIconConfiguration.PLUGIN_PATH);
        parent.mkdirs();
        assertTrue(parent.exists());

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doCleanup(mockReq);

            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoCleanupOnlyUsedIcons(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);

        Folder project1 = r.jenkins.createProject(Folder.class, "folder");
        OrganizationFolder project2 = r.jenkins.createProject(OrganizationFolder.class, "org");

        project1.setIcon(customIcon);
        project2.setIcon(customIcon);

        FilePath parent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH).child(CustomFolderIconConfiguration.PLUGIN_PATH);
        parent.mkdirs();

        FilePath dummy = parent.child(DUMMY_PNG);
        dummy.touch(System.currentTimeMillis());
        assertTrue(dummy.exists());

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doCleanup(mockReq);

            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertTrue(dummy.exists());
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoCleanupUsedAndUnusedIcons(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);

        Folder project1 = r.jenkins.createProject(Folder.class, "folder");
        OrganizationFolder project2 = r.jenkins.createProject(OrganizationFolder.class, "org");

        project1.setIcon(customIcon);
        project2.setIcon(customIcon);

        FilePath parent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH).child(CustomFolderIconConfiguration.PLUGIN_PATH);
        parent.mkdirs();

        FilePath dummy = parent.child(DUMMY_PNG);
        dummy.touch(System.currentTimeMillis());
        assertTrue(dummy.exists());

        FilePath unused = parent.child("unused.png");
        unused.touch(System.currentTimeMillis());
        assertTrue(unused.exists());

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doCleanup(mockReq);

            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertTrue(dummy.exists());
            assertFalse(unused.exists());
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest)} when root does not exist.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoCleanupNoRoot(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath parent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH).child(CustomFolderIconConfiguration.PLUGIN_PATH);
            assertTrue(parent.delete());

            HttpResponse response = descriptor.doCleanup(mockReq);

            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertFalse(parent.exists());
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest)} if a file can not be deleted.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoCleanupFileNotDeleted(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
            FilePath iconDir = userContent.child(CustomFolderIconConfiguration.PLUGIN_PATH);
            iconDir.mkdirs();

            String filename = System.currentTimeMillis() + ".png";
            FilePath file = iconDir.child(filename);
            file.touch(System.currentTimeMillis());

            try (@SuppressWarnings("unused") MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
                String call = invocation.toString();
                if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                    return userContent;
                } else if (StringUtils.equals(call, "filePath.exists();")) {
                    return true;
                } else if (StringUtils.equals(call, "filePath.list();")) {
                    return List.of(file);
                } else if (StringUtils.equals(call, "filePath.child(\"" + filename + "\");")) {
                    FilePath mock = Mockito.mock(FilePath.class);
                    Mockito.when(mock.delete()).thenReturn(false);
                    return mock;
                }
                return fail("Unexpected invocation '" + call + "' - Test is broken!");
            })) {
                HttpResponse response = descriptor.doCleanup(mockReq);
                TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            }

            assertTrue(file.exists());
            assertTrue(file.delete());
            assertFalse(file.exists());
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest)} if a file can not be deleted due to an exception.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoCleanupFileNotDeletedWithException(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath parent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH).child(CustomFolderIconConfiguration.PLUGIN_PATH);
            parent.mkdirs();

            FilePath file = parent.child(System.currentTimeMillis() + ".png");
            file.touch(System.currentTimeMillis());
            File remoteFile = new File(file.getRemote());

            // jenkins is pretty brutal when deleting files...
            Thread blocker = new Thread() {
                @Override
                public void run() {
                    while (!this.isInterrupted()) {
                        if (!remoteFile.setReadOnly()) {
                            LOGGER.warning("Unable to set file to read-only!");
                        }
                    }
                }
            };

            blocker.start();
            assertTrue(file.exists());

            HttpResponse response = descriptor.doCleanup(mockReq);
            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);

            blocker.interrupt();
            response = descriptor.doCleanup(mockReq);

            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertFalse(file.exists());
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest)} if a file can not be deleted due to an exception.
     *
     * @throws Exception in case anything goes wrong
     * @implNote Sometimes {@link CustomFolderIconConfigurationTest#testDoCleanupFileNotDeletedWithException(JenkinsRule)} does not work.
     */
    @Test
    void testDoCleanupFileNotDeletedWithMockedException(JenkinsRule r) throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
            FilePath iconDir = userContent.child(CustomFolderIconConfiguration.PLUGIN_PATH);
            iconDir.mkdirs();

            String filename = System.currentTimeMillis() + ".png";
            FilePath file = iconDir.child(filename);
            file.touch(System.currentTimeMillis());

            try (@SuppressWarnings("unused") MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
                String call = invocation.toString();
                if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                    return userContent;
                } else if (StringUtils.equals(call, "filePath.exists();")) {
                    return true;
                } else if (StringUtils.equals(call, "filePath.list();")) {
                    return List.of(file);
                } else if (StringUtils.equals(call, "filePath.child(\"" + filename + "\");")) {
                    throw new IOException("Mocked Exception!");
                }
                return fail("Unexpected invocation '" + call + "' - Test is broken!");
            })) {
                HttpResponse response = descriptor.doCleanup(mockReq);
                TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            }

            assertTrue(file.exists());
            assertTrue(file.delete());
            assertFalse(file.exists());
        }
    }
}
