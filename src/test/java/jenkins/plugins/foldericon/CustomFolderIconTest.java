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
import hudson.model.Item;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.CustomFolderIcon.DescriptorImpl;
import jenkins.plugins.foldericon.utils.MockMultiPartRequest;
import jenkins.plugins.foldericon.utils.TestUtils;
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
import org.mockito.Mockito;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Custom Folder Icon Tests
 *
 * @author strangelookingnerd
 */
@WithJenkins
class CustomFolderIconTest {

    private static final String DUMMY_PNG = "dummy.png";

    private static final String FILE_NAME_PATTERN = "" +
            "^[0-9a-fA-F]{8}" +
            "\\b-[0-9a-fA-F]{4}" +
            "\\b-[0-9a-fA-F]{4}" +
            "\\b-[0-9a-fA-F]{4}" +
            "\\b-[0-9a-fA-F]{12}" +
            "\\.png$";

    /**
     * Test behavior on a regular {@link Folder}.
     *
     * @throws Exception
     */
    @Test
    void testFolder(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        assertEquals(DUMMY_PNG, customIcon.getFoldericon());
        assertEquals(Messages.Folder_description(), customIcon.getDescription());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        assertEquals(DUMMY_PNG, customIcon.getFoldericon());
        assertEquals(project.getPronoun(), icon.getDescription());
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception
     */
    @Test
    void testOrganizationFolder(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        assertEquals(DUMMY_PNG, customIcon.getFoldericon());
        assertEquals(Messages.Folder_description(), customIcon.getDescription());

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        assertEquals(DUMMY_PNG, customIcon.getFoldericon());
        assertEquals(project.getPronoun(), icon.getDescription());
    }

    /**
     * Test the default path of the image.
     *
     * @throws Exception
     */
    @Test
    void testDefaultImagePath(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            TestUtils.mockStaplerRequest(stapler);
            TestUtils.validateIcon(icon, "default.png", null);
        }
    }

    /**
     * Test the context path of the image.
     *
     * @throws Exception
     */
    @Test
    void testImagePath(JenkinsRule r) throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            TestUtils.mockStaplerRequest(stapler);
            TestUtils.validateIcon(icon, DUMMY_PNG, null);
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void testDescriptor(JenkinsRule r) {
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertEquals(Messages.CustomFolderIcon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)}.
     *
     * @throws Exception
     */
    @Test
    void testDoUploadIcon(JenkinsRule r) throws Exception {
        File upload = new File("./src/main/webapp/icons/default.png");

        byte[] buffer = TestUtils.createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);
        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

        TestUtils.validateResponse(response, 0, FILE_NAME_PATTERN, null);

        Field code = response.getClass().getDeclaredField("val$text");
        code.setAccessible(true);
        String filename = (String) code.get(response);

        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
        FilePath file = parent.child(filename);
        assertTrue(file.exists());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} with an item.
     *
     * @throws Exception
     */
    @Test
    void testDoUploadWithItem(JenkinsRule r) throws Exception {
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        File upload = new File("./src/main/webapp/icons/default.png");

        byte[] buffer = TestUtils.createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);
        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, project);

        TestUtils.validateResponse(response, 0, FILE_NAME_PATTERN, null);

        Field code = response.getClass().getDeclaredField("val$text");
        code.setAccessible(true);
        String filename = (String) code.get(response);

        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
        FilePath file = parent.child(filename);
        assertTrue(file.exists());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} when there is no file in the request.
     *
     * @throws Exception
     */
    @Test
    void testDoUploadIconNoFile(JenkinsRule r) throws Exception {
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(null);
        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

        TestUtils.validateResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, Messages.Upload_invalidFile());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} with a broken request.
     *
     * @throws Exception
     */
    @Test
    void testDoUploadBrokenRequest(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doUploadIcon(mockReq, null);

            TestUtils.validateResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, Messages.Upload_invalidFile());
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} with a large file.
     *
     * @throws Exception
     */
    @Test
    void testDoUploadLargeFile(JenkinsRule r) throws Exception {
        File upload = File.createTempFile("large", ".png");
        upload.deleteOnExit();
        try (RandomAccessFile raf = new RandomAccessFile(upload, "rw")) {
            raf.setLength(1024L * 1024L * 2L);
        }

        byte[] buffer = TestUtils.createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);
        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

        TestUtils.validateResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, Messages.Upload_exceedsFileSize(mockRequest.getContentLength(), 1024L * 1024L));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} with an empty file.
     *
     * @throws Exception
     */
    @Test
    void testDoUploadEmptyFile(JenkinsRule r) throws Exception {
        File upload = File.createTempFile("empty", ".png");
        upload.deleteOnExit();

        byte[] buffer = TestUtils.createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer) {
            @Override
            public int getContentLength() {
                return 0;
            }
        };

        DescriptorImpl descriptor = new DescriptorImpl();
        HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

        TestUtils.validateResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, Messages.Upload_invalidFile());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)} when there are exceptions thrown.
     *
     * @throws Exception
     */
    @Test
    void testDoUploadIconThrowingExceptions(JenkinsRule r) throws Exception {
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
                            return null;
                    }
                }
            };

            DescriptorImpl descriptor = new DescriptorImpl();
            HttpResponse response = descriptor.doUploadIcon(mockRequest, null);

            TestUtils.validateResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, exceptionMessage);
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)}.
     *
     * @throws Exception
     */
    @Test
    void testDoCleanupNoItems(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
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
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)}.
     *
     * @throws Exception
     */
    @Test
    void testDoCleanupMissingIcon(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
        parent.mkdirs();
        assertTrue(parent.exists());

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doCleanup(mockReq);

            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)}.
     *
     * @throws Exception
     */
    @Test
    void testDoCleanupOnlyUsedIcons(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);

        Folder project1 = r.jenkins.createProject(Folder.class, "folder");
        OrganizationFolder project2 = r.jenkins.createProject(OrganizationFolder.class, "org");

        project1.setIcon(customIcon);
        project2.setIcon(customIcon);

        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
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
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)}.
     *
     * @throws Exception
     */
    @Test
    void testDoCleanupUsedAndUnusedIcons(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);

        Folder project1 = r.jenkins.createProject(Folder.class, "folder");
        OrganizationFolder project2 = r.jenkins.createProject(OrganizationFolder.class, "org");

        project1.setIcon(customIcon);
        project2.setIcon(customIcon);

        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
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
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)} when root does not exist.
     *
     * @throws Exception
     */
    @Test
    void testDoCleanupNoRoot(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
            assertTrue(parent.delete());

            HttpResponse response = descriptor.doCleanup(mockReq);

            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertFalse(parent.exists());
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)} if a file can not be deleted.
     *
     * @throws Exception
     */
    @Test
    void testDoCleanupFileNotDeleted(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath userContent = r.jenkins.getRootPath().child("userContent");
            FilePath iconDir = userContent.child("customFolderIcons");
            iconDir.mkdirs();
            String filename = System.currentTimeMillis() + ".png";
            FilePath file = iconDir.child(filename);
            file.touch(System.currentTimeMillis());

            try (MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
                String call = invocation.toString();
                if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                    return userContent;
                } else if (StringUtils.equals(call, "filePath.exists();")) {
                    return true;
                } else if (StringUtils.equals(call, "filePath.list();")) {
                    return List.of(file);
                } else if (invocation.toString().equals("filePath.child(\"" + filename + "\");")) {
                    FilePath mock = Mockito.mock(FilePath.class);
                    Mockito.when(mock.delete()).thenReturn(false);
                    return mock;
                }
                throw new IllegalStateException("Unexpected invocation '" + invocation + "' - Test is broken :(");
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
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)} if a file can not be deleted due to an exception.
     *
     * @throws Exception
     */
    @Test
    void testDoCleanupFileNotDeletedWithException(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
            parent.mkdirs();
            FilePath file = parent.child(System.currentTimeMillis() + ".png");
            file.touch(System.currentTimeMillis());
            File remoteFile = new File(file.getRemote());

            // jenkins is pretty brutal when deleting files...
            Thread blocker = new Thread() {
                @Override
                public void run() {
                    while (!this.isInterrupted()) {
                        remoteFile.setReadOnly();
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
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)} if a file can not be deleted due to an exception.
     *
     * @throws Exception
     * @implNote Sometimes {@link CustomFolderIconTest#testDoCleanupFileNotDeletedWithException(JenkinsRule)} does not work.
     */
    @Test
    void testDoCleanupFileNotDeletedWithMockedException(JenkinsRule r) throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = TestUtils.mockStaplerRequest(stapler);

            FilePath userContent = r.jenkins.getRootPath().child("userContent");
            FilePath iconDir = userContent.child("customFolderIcons");
            iconDir.mkdirs();
            String filename = System.currentTimeMillis() + ".png";
            FilePath file = iconDir.child(filename);
            file.touch(System.currentTimeMillis());

            try (MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
                String call = invocation.toString();
                if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                    return userContent;
                } else if (StringUtils.equals(call, "filePath.exists();")) {
                    return true;
                } else if (StringUtils.equals(call, "filePath.list();")) {
                    return List.of(file);
                } else if (invocation.toString().equals("filePath.child(\"" + filename + "\");")) {
                    throw new IOException("Mocked Exception!");
                }
                throw new IllegalStateException("Unexpected invocation '" + invocation + "' - Test is broken :(");
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
     * Test behavior of {@link CustomFolderIcon#getAvailableIcons()}.
     *
     * @throws Exception
     */
    @Test
    void testGetAvailableIcons(JenkinsRule r) throws Exception {
        List<String> icons = CustomFolderIcon.getAvailableIcons();

        assertNotNull(icons);
        assertTrue(icons.isEmpty());

        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();

        String filename1 = System.nanoTime() + ".png";
        FilePath file1 = iconDir.child(filename1);
        file1.touch(System.nanoTime());

        String filename2 = System.nanoTime() + ".png";
        FilePath file2 = iconDir.child(filename2);
        file2.touch(System.nanoTime());

        icons = CustomFolderIcon.getAvailableIcons();

        assertNotNull(icons);
        assertEquals(2, icons.size());

        List<String> expected = Arrays.asList(file1.getName(), file2.getName());
        expected.sort(Comparator.reverseOrder());
        assertEquals(expected, icons);
    }

    /**
     * Test behavior of {@link CustomFolderIcon#getAvailableIcons()} when an exception is thrown in the main logic.
     *
     * @throws Exception
     */
    @Test
    void testGetAvailableIconsThrowingExceptions(JenkinsRule r) throws Exception {
        List<String> icons = CustomFolderIcon.getAvailableIcons();

        assertNotNull(icons);
        assertTrue(icons.isEmpty());

        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();

        String filename1 = System.nanoTime() + ".png";
        FilePath file1 = iconDir.child(filename1);
        file1.touch(System.nanoTime());

        String filename2 = System.nanoTime() + ".png";
        FilePath file2 = iconDir.child(filename2);
        file2.touch(System.nanoTime());

        try (MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
            String call = invocation.toString();
            if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                throw new IOException("Mocked Exception!");
            }
            throw new IllegalStateException("Unexpected invocation '" + invocation + "' - Test is broken :(");
        })) {
            icons = CustomFolderIcon.getAvailableIcons();

            assertNotNull(icons);
            assertTrue(icons.isEmpty());
        }
    }

    /**
     * Test behavior of {@link CustomFolderIcon#getAvailableIcons()} when an exception is thrown in the comparator logic.
     *
     * @throws Exception
     */
    @Test
    void testGetAvailableIconsComparatorThrowingExceptions(JenkinsRule r) throws Exception {
        List<String> icons = CustomFolderIcon.getAvailableIcons();

        assertNotNull(icons);
        assertTrue(icons.isEmpty());

        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();

        String filename1 = System.nanoTime() + ".png";
        FilePath file1 = iconDir.child(filename1);
        file1.touch(System.nanoTime());

        String filename2 = System.nanoTime() + ".png";
        FilePath file2 = iconDir.child(filename2);
        file2.touch(System.nanoTime());

        final boolean[] first = {true};

        try (MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
            String call = invocation.toString();

            if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                return userContent;
            } else if (StringUtils.equals(call, "filePath.exists();")) {
                return true;
            } else if (StringUtils.equals(call, "filePath.list();")) {
                return iconDir.list();
            } else if (StringUtils.equals(call, "filePath.getName();")) {
                if (first[0]) {
                    first[0] = false;
                    return filename2;
                } else {
                    return filename1;
                }
            } else if (invocation.toString().equals("file2.lastModified()")) {
                throw new IOException("Mocked Exception!");
            }
            throw new IllegalStateException("Unexpected invocation '" + invocation + "' - Test is broken :(");
        })) {
            icons = CustomFolderIcon.getAvailableIcons();

            assertNotNull(icons);
            assertEquals(2, icons.size());

            List<String> expected = Arrays.asList(file1.getName(), file2.getName());
            expected.sort(Comparator.reverseOrder());
            assertEquals(expected, icons);
        }
    }

    /**
     * Test behavior of {@link jenkins.plugins.foldericon.CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)}.
     *
     * @throws Exception
     */
    @Test
    void testCleanupListener(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();
        String filename = System.currentTimeMillis() + ".png";
        FilePath file = iconDir.child(filename);
        file.touch(System.currentTimeMillis());

        CustomFolderIcon customIcon = new CustomFolderIcon(filename);

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        assertTrue(file.exists());
        project.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link jenkins.plugins.foldericon.CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)}.
     *
     * @throws Exception
     */
    @Test
    void testCleanupListenerNoFile(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();
        String filename = System.currentTimeMillis() + ".png";
        FilePath file = iconDir.child(filename);

        CustomFolderIcon customIcon = new CustomFolderIcon(filename);

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        assertFalse(file.exists());
        project.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link jenkins.plugins.foldericon.CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)} when the icon is used elsewhere.
     *
     * @throws Exception
     */
    @Test
    void testCleanupListenerIconUsed(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();
        String filename = System.currentTimeMillis() + ".png";
        FilePath file = iconDir.child(filename);
        file.touch(System.currentTimeMillis());

        CustomFolderIcon customIcon = new CustomFolderIcon(filename);

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
     * Test behavior of {@link jenkins.plugins.foldericon.CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)} when the file is not deleted.
     *
     * @throws Exception
     */
    @Test
    void testCleanupListenerFileNotDeleted(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();
        String filename = System.currentTimeMillis() + ".png";
        FilePath file = iconDir.child(filename);
        file.touch(System.currentTimeMillis());

        CustomFolderIcon customIcon = new CustomFolderIcon(filename);

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        try (MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
            String call = invocation.toString();
            if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                return userContent;
            } else if (StringUtils.equals(call, "filePath.exists();")) {
                return true;
            } else if (StringUtils.equals(call, "filePath.list();")) {
                return List.of(file);
            } else if (invocation.toString().equals("filePath.child(\"" + filename + "\");")) {
                FilePath mock = Mockito.mock(FilePath.class);
                Mockito.when(mock.delete()).thenReturn(false);
                return mock;
            }
            throw new IllegalStateException("Unexpected invocation '" + invocation + "' - Test is broken :(");
        })) {
            project.delete();
        }

        assertTrue(file.exists());
        project.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link jenkins.plugins.foldericon.CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)} when the file is not deleted due to an exception.
     *
     * @throws Exception
     */
    @Test
    void testCleanupListenerFileNotDeletedWithException(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();
        String filename = System.currentTimeMillis() + ".png";
        FilePath file = iconDir.child(filename);
        file.touch(System.currentTimeMillis());
        File remoteFile = new File(file.getRemote());

        CustomFolderIcon customIcon = new CustomFolderIcon(filename);

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        // jenkins is pretty brutal when deleting files...
        Thread blocker = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    remoteFile.setReadOnly();
                }
            }
        };

        blocker.start();

        assertTrue(file.exists());
        project.delete();
        assertTrue(file.exists());

        blocker.interrupt();

        project.delete();
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link jenkins.plugins.foldericon.CustomFolderIcon.CustomFolderIconCleanup#onDeleted(Item)} when the file is not deleted due to an exception.
     *
     * @throws Exception
     * @implNote Sometimes {@link CustomFolderIconTest#testDoCleanupFileNotDeletedWithException(JenkinsRule)} does not work.
     */
    @Test
    void testCleanupListenerFileNotDeletedWithMockedException(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child("userContent");
        FilePath iconDir = userContent.child("customFolderIcons");
        iconDir.mkdirs();
        String filename = System.currentTimeMillis() + ".png";
        FilePath file = iconDir.child(filename);
        file.touch(System.currentTimeMillis());

        CustomFolderIcon customIcon = new CustomFolderIcon(filename);

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        try (MockedConstruction<FilePath> mocked = Mockito.mockConstructionWithAnswer(FilePath.class, invocation -> {
            String call = invocation.toString();
            if (StringUtils.equals(call, "filePath.child(\"userContent\");")) {
                return userContent;
            } else if (StringUtils.equals(call, "filePath.exists();")) {
                return true;
            } else if (StringUtils.equals(call, "filePath.list();")) {
                return List.of(file);
            } else if (invocation.toString().equals("filePath.child(\"" + filename + "\");")) {
                throw new IOException("Mocked Exception!");
            }
            throw new IllegalStateException("Unexpected invocation '" + invocation + "' - Test is broken :(");
        })) {
            project.delete();
        }

        assertTrue(file.exists());
        project.delete();
        assertFalse(file.exists());
    }

}
