package jenkins.plugins.foldericon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import hudson.model.Item;
import hudson.util.FormValidation;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.UrlFolderIcon.DescriptorImpl;
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
        assertThat(customIcon.getUrl(), nullValue());
        assertThat(customIcon.getImageOf(null), is(r.jenkins.getRootUrl() + DEFAULT_ICON_PATH));
        assertThat(customIcon.getIconClassName(), nullValue());

        customIcon = new UrlFolderIcon("");
        assertThat(customIcon.getUrl(), is(""));
        assertThat(customIcon.getImageOf(null), is(r.jenkins.getRootUrl() + DEFAULT_ICON_PATH));
        assertThat(customIcon.getIconClassName(), nullValue());

        customIcon = new UrlFolderIcon(DUMMY_ICON);
        assertThat(customIcon.getDescription(), startsWith(Messages.Folder_description()));
        assertThat(customIcon.getUrl(), is(DUMMY_ICON));
        assertThat(customIcon.getImageOf(null), is(DUMMY_ICON));
        assertThat(customIcon.getIconClassName(), nullValue());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertThat(icon, instanceOf(UrlFolderIcon.class));
        assertThat(icon.getDescription(), startsWith(project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void organizationFolder() throws Exception {
        UrlFolderIcon customIcon = new UrlFolderIcon(null);
        assertThat(customIcon.getUrl(), nullValue());
        assertThat(customIcon.getImageOf(null), is(r.jenkins.getRootUrl() + DEFAULT_ICON_PATH));
        assertThat(customIcon.getIconClassName(), nullValue());

        customIcon = new UrlFolderIcon("");
        assertThat(customIcon.getUrl(), is(""));
        assertThat(customIcon.getImageOf(null), is(r.jenkins.getRootUrl() + DEFAULT_ICON_PATH));
        assertThat(customIcon.getIconClassName(), nullValue());

        customIcon = new UrlFolderIcon(DUMMY_ICON);
        assertThat(customIcon.getDescription(), startsWith(Messages.Folder_description()));
        assertThat(customIcon.getUrl(), is(DUMMY_ICON));
        assertThat(customIcon.getImageOf(null), is(DUMMY_ICON));
        assertThat(customIcon.getIconClassName(), nullValue());

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertThat(icon, instanceOf(UrlFolderIcon.class));
        assertThat(icon.getDescription(), startsWith(project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void descriptor() {
        UrlFolderIcon customIcon = new UrlFolderIcon(DUMMY_ICON);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertThat(descriptor.getDisplayName(), is(Messages.UrlFolderIcon_description()));
        assertThat(descriptor.isApplicable(null), is(true));
    }

    /**
     * Test behavior of {@link DescriptorImpl#doCheckUrl(Item, String)}.
     */
    @Test
    void doCheckUrl() {
        UrlFolderIcon customIcon = new UrlFolderIcon(DUMMY_ICON);
        DescriptorImpl descriptor = customIcon.getDescriptor();

        assertThat(descriptor.doCheckUrl(null, null).kind, is(FormValidation.ok().kind));
        assertThat(descriptor.doCheckUrl(null, "").kind, is(FormValidation.ok().kind));
        assertThat(descriptor.doCheckUrl(null, "http://jenkins.io").kind, is(FormValidation.ok().kind));
        assertThat(descriptor.doCheckUrl(null, "https://jenkins.io").kind, is(FormValidation.ok().kind));
        assertThat(descriptor.doCheckUrl(null, "HTTPS://jenkins.io").kind, is(FormValidation.ok().kind));

        FormValidation actual = descriptor.doCheckUrl(null, DUMMY_ICON);
        FormValidation expected = FormValidation.error(Messages.Url_invalidUrl());
        assertThat(actual.kind, is(expected.kind));
        assertThat(actual.getMessage(), is(expected.getMessage()));
    }
}
