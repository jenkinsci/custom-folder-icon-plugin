package jenkins.plugins.foldericon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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

            assertTrue(StringUtils.contains(customIcon.getImageOf("42"), "default.png"));
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

            assertTrue(StringUtils.contains(customIcon.getImageOf("42"), "dummy"));
        }
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     * 
     * @throws Exception
     */
    @Test
    public void testDescriptor() throws Exception {
        DescriptorImpl descriptor = new DescriptorImpl();
        assertEquals(Messages.Icon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));

        StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);

        HttpResponse response = descriptor.doUploadIcon(mockReq);
        Field field = response.getClass().getDeclaredField("val$code");
        field.setAccessible(true);
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, field.get(response));

        response = descriptor.doCleanup(mockReq);
        field = response.getClass().getDeclaredField("val$code");
        field.setAccessible(true);
        assertEquals(HttpServletResponse.SC_OK, field.get(response));
    }
}
