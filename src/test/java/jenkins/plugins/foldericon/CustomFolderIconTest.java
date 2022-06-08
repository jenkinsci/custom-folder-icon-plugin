package jenkins.plugins.foldericon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;

import jenkins.branch.OrganizationFolder;

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
        project.setIcon(new CustomFolderIcon("dummy"));
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
        project.setIcon(new CustomFolderIcon("dummy"));
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof CustomFolderIcon);

        customIcon = ((CustomFolderIcon) icon);
        assertEquals("dummy", customIcon.getFoldericon());
        assertEquals(project.getPronoun(), customIcon.getDescription());
    }
}
