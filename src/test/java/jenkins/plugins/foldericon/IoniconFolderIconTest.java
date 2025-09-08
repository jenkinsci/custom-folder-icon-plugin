package jenkins.plugins.foldericon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import io.jenkins.plugins.ionicons.Ionicons;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.IoniconFolderIcon.DescriptorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Ionicon Folder Icon Tests
 */
@WithJenkins
class IoniconFolderIconTest {

    private static final String DUMMY_ICON = "dummy";
    private static final String DEFAULT_ICON = "jenkins";

    private static final String DUMMY_ICON_CLASS_NAME = Ionicons.getIconClassName(DUMMY_ICON);
    private static final String DEFAULT_ICON_CLASS_NAME = Ionicons.getIconClassName(DEFAULT_ICON);

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
        IoniconFolderIcon customIcon = new IoniconFolderIcon(null);
        assertThat(customIcon.getIonicon(), is(DEFAULT_ICON));
        assertThat(customIcon.getImageOf(null), nullValue());
        assertThat(customIcon.getImageOf(""), nullValue());
        assertThat(customIcon.getIconClassName(), is(DEFAULT_ICON_CLASS_NAME));

        customIcon = new IoniconFolderIcon(DUMMY_ICON);
        assertThat(customIcon.getDescription(), startsWith(Messages.Folder_description()));
        assertThat(customIcon.getIonicon(), is(DUMMY_ICON));
        assertThat(customIcon.getImageOf(null), nullValue());
        assertThat(customIcon.getImageOf(""), nullValue());
        assertThat(customIcon.getIconClassName(), is(DUMMY_ICON_CLASS_NAME));

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertThat(icon, instanceOf(IoniconFolderIcon.class));
        assertThat(icon.getDescription(), startsWith(project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void organizationFolder() throws Exception {
        IoniconFolderIcon customIcon = new IoniconFolderIcon(null);
        assertThat(customIcon.getIonicon(), is(DEFAULT_ICON));
        assertThat(customIcon.getImageOf(null), nullValue());
        assertThat(customIcon.getImageOf(""), nullValue());
        assertThat(customIcon.getIconClassName(), is(DEFAULT_ICON_CLASS_NAME));

        customIcon = new IoniconFolderIcon(DUMMY_ICON);
        assertThat(customIcon.getDescription(), startsWith(Messages.Folder_description()));
        assertThat(customIcon.getIonicon(), is(DUMMY_ICON));
        assertThat(customIcon.getImageOf(null), nullValue());
        assertThat(customIcon.getImageOf(""), nullValue());
        assertThat(customIcon.getIconClassName(), is(DUMMY_ICON_CLASS_NAME));

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertThat(icon, instanceOf(IoniconFolderIcon.class));
        assertThat(icon.getDescription(), startsWith(project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void descriptor() {
        IoniconFolderIcon customIcon = new IoniconFolderIcon(DUMMY_ICON);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertThat(descriptor.getDisplayName(), is(Messages.IoniconFolderIcon_description()));
        assertThat(descriptor.isApplicable(null), is(true));
    }
}
