package jenkins.plugins.foldericon;

import static jenkins.plugins.foldericon.utils.TestUtils.createCustomIconFile;
import static jenkins.plugins.foldericon.utils.TestUtils.createMultipartEntityBuffer;
import static jenkins.plugins.foldericon.utils.TestUtils.validateResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.FormValidation;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Collections;
import jenkins.model.Jenkins;
import jenkins.plugins.foldericon.CustomFolderIcon.DescriptorImpl;
import jenkins.plugins.foldericon.utils.MockMultiPartRequest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.springframework.security.access.AccessDeniedException;

/**
 * Test various permission checks
 */
@WithJenkins
class PermissionTest {

    private static final String ADMINISTRATOR_USER = "administering_sloth";
    private static final String MANAGE_USER = "managing_axolotl";
    private static final String CONFIGURE_USER = "configuring_red_panda";
    private static final String READ_USER = "reading_duck";

    private static final String FILE_NAME_PATTERN =
            "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\\.png$";

    private JenkinsRule r;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        r = rule;
    }

    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest2, Item)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doUploadIcon() throws Exception {
        Folder project = r.jenkins.createProject(Folder.class, "folder");

        File upload = new File("./src/main/webapp/icons/default.svg");

        byte[] buffer = createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);
        DescriptorImpl descriptor = new DescriptorImpl();

        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());

        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.ADMINISTER).onItems(project).to(ADMINISTRATOR_USER);
        strategy.grant(Jenkins.MANAGE).onItems(project).to(MANAGE_USER);
        strategy.grant(Item.CONFIGURE).onItems(project).to(CONFIGURE_USER);
        strategy.grant(Item.READ).onItems(project).to(READ_USER);
        r.jenkins.setAuthorizationStrategy(strategy);

        // unauthenticated
        try (ACLContext ignored = ACL.as2(Jenkins.ANONYMOUS2)) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, null));
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, project));
        }

        // Item.READ
        try (ACLContext ignored = ACL.as(User.get(READ_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, null));
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, project));

            strategy.grant(Jenkins.READ).onRoot().to(READ_USER);
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, project));
        }

        // Item.CONFIGURE
        try (ACLContext ignored = ACL.as(User.get(CONFIGURE_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, null));

            HttpResponse response = descriptor.doUploadIcon(mockRequest, project);
            validateResponse(response, 0, FILE_NAME_PATTERN, null);
        }

        // Jenkins.MANAGE
        try (ACLContext ignored = ACL.as(User.get(MANAGE_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, null));
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, project));

            strategy.grant(Jenkins.MANAGE).onRoot().to(MANAGE_USER);
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, project));
        }

        // Jenkins.ADMINISTER
        try (ACLContext ignored = ACL.as(User.get(ADMINISTRATOR_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, null));

            HttpResponse response = descriptor.doUploadIcon(mockRequest, project);
            validateResponse(response, 0, FILE_NAME_PATTERN, null);

            strategy.grant(Jenkins.ADMINISTER).onRoot().to(ADMINISTRATOR_USER);
            response = descriptor.doUploadIcon(mockRequest, project);
            validateResponse(response, 0, FILE_NAME_PATTERN, null);
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#doCleanup(StaplerRequest2)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCleanup() throws Exception {
        FilePath file = createCustomIconFile(r);

        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());

        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.ADMINISTER).onRoot().to(ADMINISTRATOR_USER);
        strategy.grant(Jenkins.MANAGE).onRoot().to(MANAGE_USER);
        strategy.grant(Jenkins.READ).onRoot().to(READ_USER);
        r.jenkins.setAuthorizationStrategy(strategy);

        // unauthenticated
        try (ACLContext ignored = ACL.as2(Jenkins.ANONYMOUS2)) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCleanup(null));
            assertThat(file.exists(), is(true));
        }

        // Jenkins.READ
        try (ACLContext ignored = ACL.as(User.get(READ_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCleanup(null));
            assertThat(file.exists(), is(true));
        }

        // Jenkins.MANAGE
        try (ACLContext ignored = ACL.as(User.get(MANAGE_USER, true, Collections.emptyMap()))) {
            HttpResponse response = descriptor.doCleanup(null);
            validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertThat(file.exists(), is(false));
        }

        // Jenkins.ADMINISTER
        try (ACLContext ignored = ACL.as(User.get(ADMINISTRATOR_USER, true, Collections.emptyMap()))) {
            HttpResponse response = descriptor.doCleanup(null);
            validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertThat(file.exists(), is(false));
        }
    }

    /**
     * Test behavior of {@link CustomFolderIconConfiguration#getDiskUsage()}.
     */
    @Test
    void getDiskUsage() {
        CustomFolderIconConfiguration descriptor = new CustomFolderIconConfiguration();

        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());

        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.ADMINISTER).onRoot().to(ADMINISTRATOR_USER);
        strategy.grant(Jenkins.MANAGE).onRoot().to(MANAGE_USER);
        strategy.grant(Jenkins.READ).onRoot().to(READ_USER);
        r.jenkins.setAuthorizationStrategy(strategy);

        // unauthenticated
        try (ACLContext ignored = ACL.as2(Jenkins.ANONYMOUS2)) {
            assertThrows(AccessDeniedException.class, descriptor::getDiskUsage);
        }

        // Jenkins.READ
        try (ACLContext ignored = ACL.as(User.get(READ_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, descriptor::getDiskUsage);
        }

        // Jenkins.MANAGE
        try (ACLContext ignored = ACL.as(User.get(MANAGE_USER, true, Collections.emptyMap()))) {
            String size = descriptor.getDiskUsage();
            assertThat(size, is(FileUtils.byteCountToDisplaySize(0)));
        }

        // Jenkins.ADMINISTER
        try (ACLContext ignored = ACL.as(User.get(ADMINISTRATOR_USER, true, Collections.emptyMap()))) {
            String size = descriptor.getDiskUsage();
            assertThat(size, is(FileUtils.byteCountToDisplaySize(0)));
        }
    }

    /**
     * Test behavior of {@link UrlFolderIcon.DescriptorImpl#doCheckUrl(Item, String)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void doCheckUrl() throws Exception {
        Folder project = r.jenkins.createProject(Folder.class, "folder");
        UrlFolderIcon.DescriptorImpl descriptor = new UrlFolderIcon.DescriptorImpl();

        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());

        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.ADMINISTER).onItems(project).to(ADMINISTRATOR_USER);
        strategy.grant(Jenkins.MANAGE).onItems(project).to(MANAGE_USER);
        strategy.grant(Item.CONFIGURE).onItems(project).to(CONFIGURE_USER);
        strategy.grant(Item.READ).onItems(project).to(READ_USER);
        r.jenkins.setAuthorizationStrategy(strategy);

        // unauthenticated
        try (ACLContext ignored = ACL.as2(Jenkins.ANONYMOUS2)) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(null, "https://jenkins.io"));
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(project, "https://jenkins.io"));
        }

        // Item.READ
        try (ACLContext ignored = ACL.as(User.get(READ_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(null, "https://jenkins.io"));
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(project, "https://jenkins.io"));

            strategy.grant(Jenkins.READ).onRoot().to(READ_USER);
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(project, "https://jenkins.io"));
        }

        // Item.CONFIGURE
        try (ACLContext ignored = ACL.as(User.get(CONFIGURE_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(null, "https://jenkins.io"));

            FormValidation result = descriptor.doCheckUrl(project, "https://jenkins.io");
            assertThat(result.kind, is(FormValidation.ok().kind));
        }

        // Jenkins.MANAGE
        try (ACLContext ignored = ACL.as(User.get(MANAGE_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(null, "https://jenkins.io"));
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(project, "https://jenkins.io"));

            strategy.grant(Jenkins.MANAGE).onRoot().to(MANAGE_USER);
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(project, "https://jenkins.io"));
        }

        // Jenkins.ADMINISTER
        try (ACLContext ignored = ACL.as(User.get(ADMINISTRATOR_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCheckUrl(null, "https://jenkins.io"));

            FormValidation result = descriptor.doCheckUrl(project, "https://jenkins.io");
            assertThat(result.kind, is(FormValidation.ok().kind));

            strategy.grant(Jenkins.ADMINISTER).onRoot().to(ADMINISTRATOR_USER);
            result = descriptor.doCheckUrl(project, "https://jenkins.io");
            assertThat(result.kind, is(FormValidation.ok().kind));
        }
    }
}
