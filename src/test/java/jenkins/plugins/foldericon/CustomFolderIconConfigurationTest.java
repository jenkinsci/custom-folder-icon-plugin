package jenkins.plugins.foldericon;

import static jenkins.plugins.foldericon.utils.TestUtils.createCustomIconFile;
import static jenkins.plugins.foldericon.utils.TestUtils.mockStaplerRequest;
import static jenkins.plugins.foldericon.utils.TestUtils.validateResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstructionWithAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.FilePath;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import jenkins.appearance.AppearanceCategory;
import jenkins.branch.OrganizationFolder;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

/**
 * Custom Folder Icon Configuration Tests
 */
@WithJenkins
class CustomFolderIconConfigurationTest {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIconConfigurationTest.class.getName());

    private static final String DUMMY_PNG = "dummy.png";

    private JenkinsRule r;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        r = rule;
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getCategory()}.
     */
    @Test
    void getCategory() {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        assertThat(descriptor.getCategory(), is(GlobalConfigurationCategory.get(AppearanceCategory.class)));
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getRequiredGlobalConfigPagePermission()}.
     */
    @Test
    void getRequiredGlobalConfigPagePermission() {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        assertThat(descriptor.getRequiredGlobalConfigPagePermission(), is(Jenkins.MANAGE));
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void getDiskUsage() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        FilePath file = createCustomIconFile(r);

        String usage = descriptor.getDiskUsage();
        assertThat(usage, is(FileUtils.byteCountToDisplaySize(file.length())));
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void getDiskUsageNoIcons() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        FilePath parent = r.jenkins
                .getRootPath()
                .child(CustomFolderIconConfiguration.USER_CONTENT_PATH)
                .child(CustomFolderIconConfiguration.PLUGIN_PATH);
        parent.mkdirs();

        String usage = descriptor.getDiskUsage();
        assertThat(usage, is(FileUtils.byteCountToDisplaySize(0L)));
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}}.
     */
    @Test
    void getDiskUsageNoRoot() {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        String usage = descriptor.getDiskUsage();
        assertThat(usage, is(FileUtils.byteCountToDisplaySize(0L)));
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}}.
     */
    @Test
    void getDiskUsageWithException() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
        FilePath file = createCustomIconFile(r);

        try (@SuppressWarnings("unused")
                MockedConstruction<FilePath> mocked = mockConstructionWithAnswer(FilePath.class, invocation -> {
                    String call = invocation.toString();
                    if (call != null) {
                        if (call.equals("filePath.child(\"userContent\");")) {
                            return userContent;
                        } else if (call.equals("filePath.exists();")) {
                            return true;
                        } else if (call.equals("filePath.list();")) {
                            return List.of(file);
                        } else if (call.equals("filePath.child(\n    \"" + file.getName() + "\"\n);")) {
                            throw new IOException("Mocked Exception!");
                        }
                    }
                    return fail("Unexpected invocation '" + call + "' - Test is broken!");
                })) {
            String usage = descriptor.getDiskUsage();
            assertThat(usage, is(FileUtils.byteCountToDisplaySize(0L)));
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCleanupNoItems() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest2 mockReq = mockStaplerRequest(stapler);

            FilePath file = createCustomIconFile(r);
            HttpResponse response = descriptor.doCleanup(mockReq);

            validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertThat(file.exists(), is(false));
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCleanupMissingIcon() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);

        FilePath parent = r.jenkins
                .getRootPath()
                .child(CustomFolderIconConfiguration.USER_CONTENT_PATH)
                .child(CustomFolderIconConfiguration.PLUGIN_PATH);
        parent.mkdirs();
        assertThat(parent.exists(), is(true));

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest2 mockReq = mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doCleanup(mockReq);

            validateResponse(response, HttpServletResponse.SC_OK, null, null);
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCleanupOnlyUsedIcons() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        CustomFolderIcon customIcon = new CustomFolderIcon(DUMMY_PNG);

        Folder project1 = r.jenkins.createProject(Folder.class, "folder");
        OrganizationFolder project2 = r.jenkins.createProject(OrganizationFolder.class, "org");

        project1.setIcon(customIcon);
        project2.setIcon(customIcon);

        FilePath dummy = createCustomIconFile(r);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest2 mockReq = mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doCleanup(mockReq);

            validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertThat(dummy.exists(), is(true));
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCleanupUsedAndUnusedIcons() throws Exception {
        FilePath dummy = createCustomIconFile(r);
        FilePath unused = createCustomIconFile(r);

        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();
        CustomFolderIcon customIcon = new CustomFolderIcon(dummy.getName());

        Folder project1 = r.jenkins.createProject(Folder.class, "folder");
        OrganizationFolder project2 = r.jenkins.createProject(OrganizationFolder.class, "org");

        project1.setIcon(customIcon);
        project2.setIcon(customIcon);

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest2 mockReq = mockStaplerRequest(stapler);
            HttpResponse response = descriptor.doCleanup(mockReq);

            validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertThat(dummy.exists(), is(true));
            assertThat(unused.exists(), is(false));
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)} when root does not exist.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCleanupNoRoot() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest2 mockReq = mockStaplerRequest(stapler);

            FilePath parent = r.jenkins
                    .getRootPath()
                    .child(CustomFolderIconConfiguration.USER_CONTENT_PATH)
                    .child(CustomFolderIconConfiguration.PLUGIN_PATH);
            assertThat(parent.delete(), is(true));

            HttpResponse response = descriptor.doCleanup(mockReq);

            validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertThat(parent.exists(), is(false));
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)} if a file cannot be deleted.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCleanupFileNotDeleted() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest2 mockReq = mockStaplerRequest(stapler);

            FilePath file = createCustomIconFile(r);
            FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);

            try (@SuppressWarnings("unused")
                    MockedConstruction<FilePath> mocked = mockConstructionWithAnswer(FilePath.class, invocation -> {
                        String call = invocation.toString();
                        if (call != null) {
                            if (call.equals("filePath.child(\"userContent\");")) {
                                return userContent;
                            } else if (call.equals("filePath.exists();")) {
                                return true;
                            } else if (call.equals("filePath.list();")) {
                                return List.of(file);
                            } else if (call.equals("filePath.child(\n    \"" + file.getName() + "\"\n);")) {
                                FilePath mock = mock(FilePath.class);
                                when(mock.delete()).thenReturn(false);
                                return mock;
                            }
                        }
                        return fail("Unexpected invocation '" + call + "' - Test is broken!");
                    })) {
                HttpResponse response = descriptor.doCleanup(mockReq);
                validateResponse(response, HttpServletResponse.SC_OK, null, null);
            }

            assertThat(file.exists(), is(true));
            assertThat(file.delete(), is(true));
            assertThat(file.exists(), is(false));
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)} if a file cannot be deleted due to an exception.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCleanupFileNotDeletedWithException() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest2 mockReq = mockStaplerRequest(stapler);

            FilePath file = createCustomIconFile(r);
            File remoteFile = new File(file.getRemote());

            // jenkins is pretty brutal when deleting files...
            Thread blocker = new Thread() {
                @Override
                public void run() {
                    while (!this.isInterrupted()) {
                        if (!remoteFile.setReadOnly()) {
                            LOGGER.warning("Unable to set file to read-only!");
                        }
                    }
                }
            };

            blocker.start();
            assertThat(file.exists(), is(true));

            HttpResponse response = descriptor.doCleanup(mockReq);
            validateResponse(response, HttpServletResponse.SC_OK, null, null);

            blocker.interrupt();
            response = descriptor.doCleanup(mockReq);

            validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertThat(file.exists(), is(false));
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)} if a file cannot be deleted due to an exception.
     *
     * @throws Exception in case anything goes wrong
     * @implNote Sometimes {@link #doCleanupFileNotDeletedWithException()} does not work.
     */
    @Test
    void doCleanupFileNotDeletedWithMockedException() throws Exception {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        try (MockedStatic<Stapler> stapler = mockStatic(Stapler.class)) {
            StaplerRequest2 mockReq = mockStaplerRequest(stapler);

            FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
            FilePath file = createCustomIconFile(r);

            try (@SuppressWarnings("unused")
                    MockedConstruction<FilePath> mocked = mockConstructionWithAnswer(FilePath.class, invocation -> {
                        String call = invocation.toString();
                        if (call != null) {
                            if (call.equals("filePath.child(\"userContent\");")) {
                                return userContent;
                            } else if (call.equals("filePath.exists();")) {
                                return true;
                            } else if (call.equals("filePath.list();")) {
                                return List.of(file);
                            } else if (call.equals("filePath.child(\n    \"" + file.getName() + "\"\n);")) {
                                throw new IOException("Mocked Exception!");
                            }
                        }
                        return fail("Unexpected invocation '" + call + "' - Test is broken!");
                    })) {
                HttpResponse response = descriptor.doCleanup(mockReq);
                validateResponse(response, HttpServletResponse.SC_OK, null, null);
            }

            assertThat(file.exists(), is(true));
            assertThat(file.delete(), is(true));
            assertThat(file.exists(), is(false));
        }
    }
}
