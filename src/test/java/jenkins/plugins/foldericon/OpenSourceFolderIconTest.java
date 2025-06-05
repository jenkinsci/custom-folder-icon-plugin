package jenkins.plugins.foldericon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import io.jenkins.plugins.oss.symbols.OpenSourceSymbols;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.OpenSourceFolderIcon.DescriptorImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * OpenSource Folder Icon Tests
 */
@WithJenkins
class OpenSourceFolderIconTest {

    private static final String DUMMY_ICON = "dummy";
    private static final String DEFAULT_ICON = "cdf-icon-color";

    private static final String DUMMY_ICON_CLASS_NAME = OpenSourceSymbols.getIconClassName(DUMMY_ICON);
    private static final String DEFAULT_ICON_CLASS_NAME = OpenSourceSymbols.getIconClassName(DEFAULT_ICON);

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
        OpenSourceFolderIcon customIcon = new OpenSourceFolderIcon(null);
        assertEquals(DEFAULT_ICON, customIcon.getOssicon());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DEFAULT_ICON_CLASS_NAME, customIcon.getIconClassName());

        customIcon = new OpenSourceFolderIcon(DUMMY_ICON);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
        assertEquals(DUMMY_ICON, customIcon.getOssicon());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DUMMY_ICON_CLASS_NAME, customIcon.getIconClassName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(OpenSourceFolderIcon.class, icon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void organizationFolder() throws Exception {
        OpenSourceFolderIcon customIcon = new OpenSourceFolderIcon(null);
        assertEquals(DEFAULT_ICON, customIcon.getOssicon());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DEFAULT_ICON_CLASS_NAME, customIcon.getIconClassName());

        customIcon = new OpenSourceFolderIcon(DUMMY_ICON);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
        assertEquals(DUMMY_ICON, customIcon.getOssicon());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DUMMY_ICON_CLASS_NAME, customIcon.getIconClassName());

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(OpenSourceFolderIcon.class, icon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void descriptor() {
        OpenSourceFolderIcon customIcon = new OpenSourceFolderIcon(DUMMY_ICON);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertEquals(Messages.OpenSourceFolderIcon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));
    }
}
