package jenkins.plugins.foldericon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;

import hudson.FilePath;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.CustomFolderIcon.DescriptorImpl;

/**
 * Custom Folder Icon Tests
 * 
 * @author strangelookingnerd
 *
 */
public class CustomFolderIconTest {

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
        CustomFolderIcon customIcon = new CustomFolderIcon("dummy");
        assertEquals("dummy", customIcon.getFoldericon());
        assertEquals(Messages.Folder_description(), customIcon.getDescription());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        customIcon = ((CustomFolderIcon) icon);
        assertEquals("dummy", customIcon.getFoldericon());
        assertEquals(project.getPronoun(), customIcon.getDescription());
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     * 
     * @throws Exception
     */
    @Test
    public void testOrganzationFolder() throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon("dummy");
        assertEquals("dummy", customIcon.getFoldericon());
        assertEquals(Messages.Folder_description(), customIcon.getDescription());

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        customIcon = ((CustomFolderIcon) icon);
        assertEquals("dummy", customIcon.getFoldericon());
        assertEquals(project.getPronoun(), customIcon.getDescription());
    }

    /**
     * Test the default path of the image.
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultImagePath() throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon(null);
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        customIcon = ((CustomFolderIcon) icon);

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);
            stapler.when(Stapler::getCurrentRequest).thenReturn(mockReq);
            Mockito.when(mockReq.getContextPath()).thenReturn("/jenkins");

            String image = customIcon.getImageOf("42");
            assertTrue(StringUtils.endsWith(image, "default.png"));
            assertTrue(StringUtils.contains(image, "/jenkins"));
            assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));
        }
    }

    /**
     * Test the context path of the image.
     * 
     * @throws Exception
     */
    @Test
    public void testImagePath() throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon("dummy");
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        customIcon = ((CustomFolderIcon) icon);

        try (MockedStatic<Stapler> stapler = Mockito.mockStatic(Stapler.class)) {
            StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);
            stapler.when(Stapler::getCurrentRequest).thenReturn(mockReq);
            Mockito.when(mockReq.getContextPath()).thenReturn("/jenkins");

            String image = customIcon.getImageOf("42");
            assertTrue(StringUtils.endsWith(image, "dummy"));
            assertTrue(StringUtils.contains(image, "/jenkins"));
            assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     * 
     * @throws Exception
     */
    @Test
    public void testDescriptor() throws Exception {
        CustomFolderIcon customIcon = new CustomFolderIcon("dummy");
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertEquals(Messages.Icon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest)}.
     * 
     * @throws Exception
     */
    @Test
    public void testDoUploadIcon() throws Exception {
        File upload = new File("./src/main/webapp/icons/default.png");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setBoundary("myboundary");
        builder.addBinaryBody(upload.getName(), upload, ContentType.DEFAULT_BINARY, upload.getName());

        byte[] buffer;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            builder.build().writeTo(outputStream);
            outputStream.flush();

            buffer = outputStream.toByteArray();
        }

        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);

        DescriptorImpl descriptor = new DescriptorImpl();

        HttpResponse response = descriptor.doUploadIcon(mockRequest);
        Field field = response.getClass().getDeclaredField("val$text");
        field.setAccessible(true);
        String filename = (String) field.get(response);
        assertTrue(StringUtils.endsWith(filename, ".png"));

        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
        FilePath file = parent.child(filename);
        assertTrue(file.exists());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest)} when there is no file in the request.
     * 
     * @throws Exception
     */
    @Test
    public void testDoUploadIconNoFile() throws Exception {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setBoundary("myboundary");

        byte[] buffer;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            builder.build().writeTo(outputStream);
            outputStream.flush();
            buffer = outputStream.toByteArray();
        }

        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);

        DescriptorImpl descriptor = new DescriptorImpl();

        HttpResponse response = descriptor.doUploadIcon(mockRequest);
        Field field = response.getClass().getDeclaredField("val$code");
        field.setAccessible(true);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, field.get(response));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest)} with a broken request.
     * 
     * @throws Exception
     */
    @Test
    public void testDoUploadBrokenRequest() throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);

        HttpResponse response = descriptor.doUploadIcon(mockReq);
        Field field = response.getClass().getDeclaredField("val$code");
        field.setAccessible(true);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, field.get(response));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)}.
     * 
     * @throws Exception
     */
    @Test
    public void testDoCleanup() throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);

        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
        parent.mkdirs();
        FilePath file = parent.child(System.currentTimeMillis() + ".png");
        file.touch(System.currentTimeMillis());
        assertTrue(file.exists());

        HttpResponse response = descriptor.doCleanup(mockReq);
        Field field = response.getClass().getDeclaredField("val$code");
        field.setAccessible(true);
        assertEquals(HttpServletResponse.SC_OK, field.get(response));
        assertFalse(file.exists());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)} when root does not exist.
     * 
     * @throws Exception
     */
    @Test
    public void testDoCleanupNoRoot() throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);

        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
        assertTrue(parent.delete());

        HttpResponse response = descriptor.doCleanup(mockReq);
        Field field = response.getClass().getDeclaredField("val$code");
        field.setAccessible(true);
        assertEquals(HttpServletResponse.SC_OK, field.get(response));
        assertFalse(parent.exists());
    }

    /**
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)} if a file can not be deleted.
     * 
     * @throws Exception
     */
    @Test
    public void testDoCleanupFileNotDeleted() throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();

        StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);

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
        Field field = response.getClass().getDeclaredField("val$code");
        field.setAccessible(true);
        assertEquals(HttpServletResponse.SC_OK, field.get(response));

        blocker.interrupt();

        response = descriptor.doCleanup(mockReq);
        field = response.getClass().getDeclaredField("val$code");
        field.setAccessible(true);
        assertEquals(HttpServletResponse.SC_OK, field.get(response));
        assertFalse(file.exists());
    }

}
