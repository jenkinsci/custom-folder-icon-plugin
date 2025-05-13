package jenkins.plugins.foldericon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import hudson.model.Item;
import hudson.util.FormValidation;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.UrlFolderIcon.DescriptorImpl;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Url Folder Icon Tests
 */
@WithJenkins
class UrlFolderIconTest {

    private static final String DUMMY_ICON = "dummy";

    private static final String DEFAULT_ICON_PATH = "plugin/custom-folder-icon/icons/default.svg";

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
        UrlFolderIcon customIcon = new UrlFolderIcon(null);
        assertNull(customIcon.getUrl());
        assertEquals(r.jenkins.getRootUrl() + DEFAULT_ICON_PATH, customIcon.getImageOf(null));
        assertNull(customIcon.getIconClassName());

        customIcon = new UrlFolderIcon(DUMMY_ICON);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
        assertEquals(DUMMY_ICON, customIcon.getUrl());
        assertEquals(DUMMY_ICON, customIcon.getImageOf(null));
        assertNull(customIcon.getIconClassName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(UrlFolderIcon.class, icon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void organizationFolder() throws Exception {
        UrlFolderIcon customIcon = new UrlFolderIcon(null);
        assertNull(customIcon.getUrl());
        assertEquals(r.jenkins.getRootUrl() + DEFAULT_ICON_PATH, customIcon.getImageOf(null));
        assertNull(customIcon.getIconClassName());

        customIcon = new UrlFolderIcon(DUMMY_ICON);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
        assertEquals(DUMMY_ICON, customIcon.getUrl());
        assertEquals(DUMMY_ICON, customIcon.getImageOf(null));
        assertNull(customIcon.getIconClassName());

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertInstanceOf(UrlFolderIcon.class, icon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void descriptor() {
        UrlFolderIcon customIcon = new UrlFolderIcon(DUMMY_ICON);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertEquals(Messages.UrlFolderIcon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doCheckUrl(Item, String)}.
     */
    @Test
    void doCheckUrl() {
        UrlFolderIcon customIcon = new UrlFolderIcon(DUMMY_ICON);
        DescriptorImpl descriptor = customIcon.getDescriptor();

        assertEquals(FormValidation.ok().kind, descriptor.doCheckUrl(null, null).kind);
        assertEquals(FormValidation.ok().kind, descriptor.doCheckUrl(null, "").kind);
        assertEquals(FormValidation.ok().kind, descriptor.doCheckUrl(null, "http://jenkins.io").kind);
        assertEquals(FormValidation.ok().kind, descriptor.doCheckUrl(null, "https://jenkins.io").kind);
        assertEquals(FormValidation.ok().kind, descriptor.doCheckUrl(null, "HTTPS://jenkins.io").kind);

        FormValidation actual = descriptor.doCheckUrl(null, DUMMY_ICON);
        FormValidation expected = FormValidation.error(Messages.Url_invalidUrl());
        assertEquals(expected.kind, actual.kind);
        assertEquals(expected.getMessage(), actual.getMessage());
    }
}
