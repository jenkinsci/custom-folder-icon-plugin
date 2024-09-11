/*
 * The MIT License
 *
 * Copyright (c) 2024 strangelookingnerd
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

import static jenkins.plugins.foldericon.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.CustomFolderIcon.DescriptorImpl;
import jenkins.plugins.foldericon.utils.MockMultiPartRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

/**
 * Custom Folder Icon Tests
 *
 * @author strangelookingnerd
 */
@WithJenkins
class CustomFolderIconTest {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIconTest.class.getName());

    private static final String DUMMY_PNG = "dummy.png";

    private static final String FILE_NAME_PATTERN =
            "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\\.png$";

    /**
     * Test behavior on a regular {@link Folder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testFolder(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        assertEquals(DUMMY_PNG, customIcon.getFoldericon());
        assertEquals(Messages.Folder_description(), customIcon.getDescription());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(CustomFolderIcon.class, icon);

        assertEquals(DUMMY_PNG, customIcon.getFoldericon());
        assertEquals(project.getPronoun(), icon.getDescription());
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testOrganizationFolder(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        assertEquals(DUMMY_PNG, customIcon.getFoldericon());
        assertEquals(Messages.Folder_description(), customIcon.getDescription());

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(CustomFolderIcon.class, icon);

        assertEquals(DUMMY_PNG, customIcon.getFoldericon());
        assertEquals(project.getPronoun(), icon.getDescription());
    }

    /**
     * Test the default path of the image.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDefaultImagePath(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(CustomFolderIcon.class, icon);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            mockStaplerRequest(stapler);
            validateIcon(icon, "default.png", null);
        }
    }

    /**
     * Test the context path of the image.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testImagePath(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(CustomFolderIcon.class, icon);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            mockStaplerRequest(stapler);
            validateIcon(icon, DUMMY_PNG, null);
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void testDescriptor(@SuppressWarnings("unused") JenkinsRule r) {
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertEquals(Messages.CustomFolderIcon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoUploadIcon(JenkinsRule r) throws Exception {
        File upload = new File("./src/main/webapp/icons/default.png");

        byte[] buffer = createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);
        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

        validateResponse(response, 0, FILE_NAME_PATTERN, null);

        Field code = response.getClass().getDeclaredField("val$text");
        code.setAccessible(true);
        String filename = (String) code.get(response);

        FilePath parent = r.jenkins
                .getRootPath()
                .child(CustomFolderIconConfiguration.USER_CONTENT_PATH)
                .child(CustomFolderIconConfiguration.PLUGIN_PATH);
        FilePath file = parent.child(filename);
        assertTrue(file.exists());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} with an item.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoUploadWithItem(JenkinsRule r) throws Exception {
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        File upload = new File("./src/main/webapp/icons/default.png");

        byte[] buffer = createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);
        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, project);

        validateResponse(response, 0, FILE_NAME_PATTERN, null);

        Field code = response.getClass().getDeclaredField("val$text");
        code.setAccessible(true);
        String filename = (String) code.get(response);

        FilePath parent = r.jenkins
                .getRootPath()
                .child(CustomFolderIconConfiguration.USER_CONTENT_PATH)
                .child(CustomFolderIconConfiguration.PLUGIN_PATH);
        FilePath file = parent.child(filename);
        assertTrue(file.exists());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} when there is no file in the request.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoUploadIconNoFile(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(null);
        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

        validateResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, Messages.Upload_invalidFile());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} with a broken request.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoUploadBrokenRequest(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest mockReq = mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doUploadIcon(mockReq, null);

            validateResponse(
                    response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, Messages.Upload_invalidFile());
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} with a large file.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoUploadLargeFile(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        File upload = File.createTempFile("large", ".png");
        upload.deleteOnExit();
        try (RandomAccessFile raf = new RandomAccessFile(upload, "rw")) {
            raf.setLength(1024L * 1024L * 2L);
        }

        byte[] buffer = createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);
        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

        validateResponse(
                response,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                null,
                Messages.Upload_exceedsFileSize(mockRequest.getContentLength(), 1024L * 1024L));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} with an empty file.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoUploadEmptyFile(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        File upload = File.createTempFile("empty", ".png");
        upload.deleteOnExit();

        byte[] buffer = createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer) {
            @Override
            public int getContentLength() {
                return 0;
            }
        };

        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

        validateResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, Messages.Upload_invalidFile());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} when there are exceptions thrown.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoUploadIconThrowingExceptions(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        List<String> exceptions = Arrays.asList("IOException", "InterruptedException", "ServletException");
        String exceptionMessage = "Oh no :(";

        for (String exception : exceptions) {
            MockMultiPartRequest mockRequest = new MockMultiPartRequest(null) {
                @Override
                public FileItem getFileItem(String name) throws ServletException, IOException {
                    switch (exception) {
                        case "IOException":
                            throw new IOException(exceptionMessage);
                        case "InterruptedException":
                            throw new InterruptedIOException(exceptionMessage);
                        case "ServletException":
                            throw new ServletException(exceptionMessage);
                        default:
                            return fail("Unexpected exception '" + exception + "' - Test is broken!");
                    }
                }
            };

            DescriptorImpl descriptor = new DescriptorImpl();
            HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

            validateResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, exceptionMessage);
        }
    }

    /**
     * Test behavior of {@link CustomFolderIcon#getAvailableIcons()}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testGetAvailableIcons(JenkinsRule r) throws Exception {
        Set<String> icons = CustomFolderIcon.getAvailableIcons();

        assertNotNull(icons);
        assertTrue(icons.isEmpty());

        FilePath file1 = createCustomIconFile(r);
        FilePath file2 = createCustomIconFile(r);
        FilePath file3 = createCustomIconFile(r);

        icons = CustomFolderIcon.getAvailableIcons();

        assertNotNull(icons);
        assertEquals(3, icons.size());

        Set<String> expected = Stream.of(file1, file2, file3)
                .sorted(Comparator.comparingLong((FilePath file) -> {
                            try {
                                return file.lastModified();
                            } catch (IOException | InterruptedException ex) {
                                return 0;
                            }
                        })
                        .reversed())
                .map(FilePath::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, icons);
    }

    /**
     * Test behavior of {@link CustomFolderIcon#getAvailableIcons()} when an exception is thrown in the main logic.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testGetAvailableIconsThrowingExceptions(JenkinsRule r) throws Exception {
        Set<String> icons = CustomFolderIcon.getAvailableIcons();

        assertNotNull(icons);
        assertTrue(icons.isEmpty());

        createCustomIconFile(r);
        createCustomIconFile(r);
        createCustomIconFile(r);

        try (@SuppressWarnings("unused")
                MockedConstruction<FilePath> mocked = mockConstructionWithAnswer(FilePath.class, invocation -> {
                    String call = invocation.toString();
                    if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                        throw new IOException("Mocked Exception!");
                    }
                    return fail("Unexpected invocation '" + call + "' - Test is broken!");
                })) {
            icons = CustomFolderIcon.getAvailableIcons();

            assertNotNull(icons);
            assertTrue(icons.isEmpty());
        }
    }

    /**
     * Test behavior of {@link CustomFolderIcon#getAvailableIcons()} when an exception is thrown in the comparator logic.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testGetAvailableIconsComparatorThrowingExceptions(JenkinsRule r) throws Exception {
        Set<String> icons = CustomFolderIcon.getAvailableIcons();

        assertNotNull(icons);
        assertTrue(icons.isEmpty());

        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
        FilePath iconDir = userContent.child(CustomFolderIconConfiguration.PLUGIN_PATH);

        FilePath file1 = createCustomIconFile(r);
        FilePath file2 = createCustomIconFile(r);
        FilePath file3 = createCustomIconFile(r);

        final int[] counter = {0};

        try (@SuppressWarnings("unused")
                MockedConstruction<FilePath> mocked = mockConstructionWithAnswer(FilePath.class, invocation -> {
                    String call = invocation.toString();

                    if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                        return userContent;
                    } else if (StringUtils.equals(call, "filePath.exists();")) {
                        return true;
                    } else if (StringUtils.equals(call, "filePath.list();")) {
                        return iconDir.list();
                    } else if (StringUtils.equals(call, "filePath.lastModified();")) {
                        if (counter[0] == 0) {
                            counter[0] = 1;
                            return file3.lastModified();
                        } else if (counter[0] == 1) {
                            counter[0] = 2;
                            return file2.lastModified();
                        } else if (counter[0] == 2) {
                            counter[0] = 0;
                            throw new IOException("Mocked Exception!");
                        }
                    } else if (StringUtils.equals(call, "filePath.getName();")) {
                        if (counter[0] == 0) {
                            counter[0] = 1;
                            return file3.getName();
                        } else if (counter[0] == 1) {
                            counter[0] = 2;
                            return file2.getName();
                        } else if (counter[0] == 2) {
                            counter[0] = 0;
                            return file1.getName();
                        }
                    }
                    return fail("Unexpected invocation '" + call + "' - Test is broken!");
                })) {
            icons = CustomFolderIcon.getAvailableIcons();

            assertNotNull(icons);
            assertEquals(3, icons.size());
        }

        Set<String> expected = Stream.of(file1, file2, file3)
                .sorted(Comparator.comparingLong((FilePath file) -> {
                            try {
                                return file.lastModified();
                            } catch (IOException | InterruptedException ex) {
                                return 0;
                            }
                        })
                        .reversed())
                .map(FilePath::getName)
                .collect(Collectors.toSet());

        assertEquals(expected, icons);
    }

    /**
     * Test behavior of {@link CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testCleanupListener(JenkinsRule r) throws Exception {
        FilePath file = createCustomIconFile(r);
        CustomFolderIcon customIcon = new CustomFolderIcon(file.getName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        assertTrue(file.exists());
        project.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testCleanupListenerOtherProjects(JenkinsRule r) throws Exception {
        FilePath file = createCustomIconFile(r);
        CustomFolderIcon customIcon = new CustomFolderIcon(file.getName());

        Folder project1 = r.jenkins.createProject(Folder.class, "folder-1");
        project1.setIcon(customIcon);

        Folder project2 = r.jenkins.createProject(Folder.class, "folder-2");
        project2.setIcon(new CustomFolderIcon(DUMMY_PNG));

        Folder project3 = r.jenkins.createProject(Folder.class, "folder-3");
        FreeStyleProject project4 = r.jenkins.createProject(FreeStyleProject.class, "job-1");

        assertTrue(file.exists());

        project1.delete();
        project2.delete();
        project3.delete();
        project4.delete();

        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testCleanupListenerNoFile(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
        FilePath iconDir = userContent.child(CustomFolderIconConfiguration.PLUGIN_PATH);
        iconDir.mkdirs();

        FilePath file = iconDir.child(System.currentTimeMillis() + ".png");
        CustomFolderIcon customIcon = new CustomFolderIcon(file.getName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        assertFalse(file.exists());
        project.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testCleanupListenerDefaultIcon(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
        FilePath iconDir = userContent.child(CustomFolderIconConfiguration.PLUGIN_PATH);
        iconDir.mkdirs();

        CustomFolderIcon customIcon = new CustomFolderIcon(null);

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        project.delete();

        assertTrue(iconDir.exists());
    }

    /**
     * Test behavior of {@link CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)} when the icon is used elsewhere.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testCleanupListenerIconUsed(JenkinsRule r) throws Exception {
        FilePath file = createCustomIconFile(r);
        CustomFolderIcon customIcon = new CustomFolderIcon(file.getName());

        Folder project1 = r.jenkins.createProject(Folder.class, "folder1");
        project1.setIcon(customIcon);

        Folder project2 = r.jenkins.createProject(Folder.class, "folder2");
        project2.setIcon(customIcon);

        assertTrue(file.exists());
        project1.delete();
        assertTrue(file.exists());
        project2.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)} when the file is not deleted.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testCleanupListenerFileNotDeleted(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);

        FilePath file = createCustomIconFile(r);
        CustomFolderIcon customIcon = new CustomFolderIcon(file.getName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        try (@SuppressWarnings("unused")
                MockedConstruction<FilePath> mocked = mockConstructionWithAnswer(FilePath.class, invocation -> {
                    String call = invocation.toString();
                    if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                        return userContent;
                    } else if (StringUtils.equals(call, "filePath.child(\"" + file.getName() + "\");")) {
                        FilePath mock = mock(FilePath.class);
                        when(mock.delete()).thenReturn(false);
                        return mock;
                    }
                    return fail("Unexpected invocation '" + call + "' - Test is broken!");
                })) {
            project.delete();
        }

        assertTrue(file.exists());
        file.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)} when the file is not deleted due to an exception.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testCleanupListenerFileNotDeletedWithException(JenkinsRule r) throws Exception {
        FilePath file = createCustomIconFile(r);
        File remoteFile = new File(file.getRemote());

        CustomFolderIcon customIcon = new CustomFolderIcon(file.getName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

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

        project.delete();
        blocker.interrupt();

        file.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)} when the file is not deleted due to an exception.
     *
     * @throws Exception in case anything goes wrong
     * @implNote Sometimes {@link #testCleanupListenerFileNotDeletedWithException(JenkinsRule)} does not work.
     */
    @Test
    void testCleanupListenerFileNotDeletedWithMockedException(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);

        FilePath file = createCustomIconFile(r);
        CustomFolderIcon customIcon = new CustomFolderIcon(file.getName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        try (@SuppressWarnings("unused")
                MockedConstruction<FilePath> mocked = mockConstructionWithAnswer(FilePath.class, invocation -> {
                    String call = invocation.toString();
                    if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                        return userContent;
                    } else if (StringUtils.equals(call, "filePath.child(\"" + file.getName() + "\");")) {
                        throw new IOException("Mocked Exception!");
                    }
                    return fail("Unexpected invocation '" + call + "' - Test is broken!");
                })) {
            project.delete();
        }

        assertTrue(file.exists());
        file.delete();
        assertFalse(file.exists());
    }
}
