/*
 * The MIT License
 *
 * Copyright (c) 2023 strangelookingnerd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.foldericon;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;
import jenkins.plugins.foldericon.CustomFolderIcon.DescriptorImpl;
import jenkins.plugins.foldericon.utils.MockMultiPartRequest;
import jenkins.plugins.foldericon.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test various permission checks
 *
 * @author strangelookingnerd
 */
@WithJenkins
class PermissionTest {

    private static final String ADMIN_USER = "sloth";

    private static final String CONFIGURE_USER = "red_panda";

    private static final String READ_USER = "duck";

    private static final String FILE_NAME_PATTERN =
            "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\\.png$";


    /**
     * Test behavior of {@link DescriptorImpl#doUploadIcon(StaplerRequest, Item)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoUploadIcon(JenkinsRule r) throws Exception {
        Folder project = r.jenkins.createProject(Folder.class, "folder");

        File upload = new File("./src/main/webapp/icons/default.png");

        byte[] buffer = TestUtils.createMultipartEntityBuffer(upload);
        MockMultiPartRequest mockRequest = new MockMultiPartRequest(buffer);
        DescriptorImpl descriptor = new DescriptorImpl();

        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());

        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.ADMINISTER).onItems(project).to(ADMIN_USER);
        strategy.grant(Item.CONFIGURE).onItems(project).to(CONFIGURE_USER);
        strategy.grant(Item.READ).onItems(project).to(READ_USER);
        r.jenkins.setAuthorizationStrategy(strategy);

        try (ACLContext ignored = ACL.as(User.get(READ_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, null));
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, project));
        }

        try (ACLContext ignored = ACL.as(User.get(CONFIGURE_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, null));

            HttpResponse response = descriptor.doUploadIcon(mockRequest, project);
            TestUtils.validateResponse(response, 0, FILE_NAME_PATTERN, null);
        }

        try (ACLContext ignored = ACL.as(User.get(ADMIN_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doUploadIcon(mockRequest, null));

            HttpResponse response = descriptor.doUploadIcon(mockRequest, project);
            TestUtils.validateResponse(response, 0, FILE_NAME_PATTERN, null);

            strategy.grant(Jenkins.ADMINISTER).onRoot().to(ADMIN_USER);
            response = descriptor.doUploadIcon(mockRequest, project);
            TestUtils.validateResponse(response, 0, FILE_NAME_PATTERN, null);
        }
    }


    /**
     * Test behavior of {@link DescriptorImpl#doCleanup(StaplerRequest)}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testDoCleanup(JenkinsRule r) throws Exception {
        FilePath parent = r.jenkins.getRootPath().child("userContent").child("customFolderIcons");
        parent.mkdirs();

        FilePath file = parent.child(System.currentTimeMillis() + ".png");
        file.touch(System.currentTimeMillis());
        assertTrue(file.exists());

        DescriptorImpl descriptor = new DescriptorImpl();

        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());

        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.ADMINISTER).onRoot().to(ADMIN_USER);
        strategy.grant(Jenkins.MANAGE).onRoot().to(CONFIGURE_USER);
        strategy.grant(Jenkins.READ).onRoot().to(READ_USER);
        r.jenkins.setAuthorizationStrategy(strategy);

        try (ACLContext ignored = ACL.as(User.get(READ_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCleanup(null));
            assertTrue(file.exists());
        }

        try (ACLContext ignored = ACL.as(User.get(CONFIGURE_USER, true, Collections.emptyMap()))) {
            assertThrows(AccessDeniedException.class, () -> descriptor.doCleanup(null));
            assertTrue(file.exists());
        }

        try (ACLContext ignored = ACL.as(User.get(ADMIN_USER, true, Collections.emptyMap()))) {
            HttpResponse response = descriptor.doCleanup(null);
            TestUtils.validateResponse(response, HttpServletResponse.SC_OK, null, null);
            assertFalse(file.exists());
        }
    }
}
