package jenkins.plugins.foldericon;

import static org.junit.jupiter.api.Assertions.*;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import io.jenkins.plugins.emoji.symbols.Emojis;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.EmojiFolderIcon.DescriptorImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Emoji Folder Icon Tests
 */
@WithJenkins
class EmojiFolderIconTest {

    private static final String DUMMY_ICON = "dummy";
    private static final String DEFAULT_ICON = "sloth";

    private static final String DUMMY_ICON_CLASS_NAME = Emojis.getIconClassName(DUMMY_ICON);
    private static final String DEFAULT_ICON_CLASS_NAME = Emojis.getIconClassName(DEFAULT_ICON);

    private JenkinsRule r;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        r = rule;
    }

    /**
     * Test behavior on a regular {@link Folder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void folder() throws Exception {
        EmojiFolderIcon customIcon = new EmojiFolderIcon(null);
        assertEquals(DEFAULT_ICON, customIcon.getEmoji());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DEFAULT_ICON_CLASS_NAME, customIcon.getIconClassName());

        customIcon = new EmojiFolderIcon(DUMMY_ICON);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
        assertEquals(DUMMY_ICON, customIcon.getEmoji());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DUMMY_ICON_CLASS_NAME, customIcon.getIconClassName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(EmojiFolderIcon.class, icon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void organizationFolder() throws Exception {
        EmojiFolderIcon customIcon = new EmojiFolderIcon(null);
        assertEquals(DEFAULT_ICON, customIcon.getEmoji());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DEFAULT_ICON_CLASS_NAME, customIcon.getIconClassName());

        customIcon = new EmojiFolderIcon(DUMMY_ICON);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
        assertEquals(DUMMY_ICON, customIcon.getEmoji());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DUMMY_ICON_CLASS_NAME, customIcon.getIconClassName());

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(EmojiFolderIcon.class, icon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void descriptor() {
        EmojiFolderIcon customIcon = new EmojiFolderIcon(DUMMY_ICON);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertEquals(Messages.EmojiFolderIcon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));
    }
}
