package jenkins.plugins.foldericon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;

/**
 * Custom Folder Icon Tests
 * 
 * @author strangelookingnerd
 *
 */
public class JENKINS_68894_WorkaroundTest {

    /**
     * The rule.
     */
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @BeforeClass
    public static void setup() throws Exception {
        Field field = CustomFolderIcon.class.getDeclaredField("USE_WORKAROUND");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, true);
    }

    @AfterClass
    public static void shutdown() throws Exception {
        Field field = CustomFolderIcon.class.getDeclaredField("USE_WORKAROUND");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, false);
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
            assertFalse(StringUtils.contains(image, "/jenkins"));
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
            assertFalse(StringUtils.contains(image, "/jenkins"));
            assertFalse(StringUtils.contains(image, "/jenkins/jenkins"));
        }
    }
}
