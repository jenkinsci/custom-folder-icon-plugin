package jenkins.plugins.foldericon.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloudbees.hudson.plugins.folder.FolderIcon;
import hudson.FilePath;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;
import jenkins.plugins.foldericon.CustomFolderIconConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.MockedStatic;

/**
 * Common utils for tests.
 */
public final class TestUtils {

    private static final String JENKINS_CONTEXT_PATH = "/jenkins";

    private TestUtils() {
        // hidden
    }

    /**
     * Common mocking of a {@link StaplerRequest2}.
     *
     * @param stapler the static mock to use
     * @return the mocked request
     */
    public static StaplerRequest2 mockStaplerRequest(MockedStatic<Stapler> stapler) {
        StaplerRequest2 mockReq = mock(StaplerRequest2.class);
        stapler.when(Stapler::getCurrentRequest2).thenReturn(mockReq);
        when(mockReq.getContextPath()).thenReturn(JENKINS_CONTEXT_PATH);
        return mockReq;
    }

    /**
     * Common validation of icons.
     *
     * @param icon              the icon to validate
     * @param expectedImageName the expected image name
     * @param expectedIconClass the expected icon class
     */
    public static void validateIcon(FolderIcon icon, String expectedImageName, String expectedIconClass) {
        String image = icon.getImageOf("42");
        assertTrue(StringUtils.endsWith(image, expectedImageName));
        assertTrue(StringUtils.contains(image, JENKINS_CONTEXT_PATH));
        assertFalse(StringUtils.contains(image, JENKINS_CONTEXT_PATH + JENKINS_CONTEXT_PATH));

        String iconClass = icon.getIconClassName();
        if (expectedIconClass != null) {
            assertTrue(StringUtils.endsWith(iconClass, expectedIconClass));
        } else {
            assertNull(iconClass);
        }
    }

    /**
     * Common validation of responses.
     *
     * @param response             the response to validate
     * @param expectedCode         the expected CODE or 0 if none expected
     * @param expectedTextPattern  the expected TEXT pattern
     * @param expectedErrorMessage the expected ERROR_MESSAGE
     * @throws Exception in case anything goes wrong
     */
    public static void validateResponse(
            HttpResponse response, int expectedCode, String expectedTextPattern, String expectedErrorMessage)
            throws Exception {
        if (expectedCode != 0) {
            Field code = response.getClass().getDeclaredField("val$code");
            code.setAccessible(true);
            assertEquals(expectedCode, code.get(response));
        }

        if (StringUtils.isNotBlank(expectedTextPattern)) {
            Field text = response.getClass().getDeclaredField("val$text");
            text.setAccessible(true);
            assertThat((String) text.get(response), matchesPattern(expectedTextPattern));
        }

        if (StringUtils.isNotBlank(expectedErrorMessage)) {
            Field message = response.getClass().getDeclaredField("val$errorMessage");
            message.setAccessible(true);
            assertEquals(expectedErrorMessage, message.get(response));
        }
    }

    /**
     * Create a Multipart Entity byte buffer of a file.
     *
     * @param file the file to convert
     * @return the byte buffer
     * @throws Exception in case anything goes wrong
     */
    public static byte[] createMultipartEntityBuffer(File file) throws Exception {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setBoundary("myboundary");
        builder.addBinaryBody(file.getName(), file, ContentType.DEFAULT_BINARY, file.getName());

        byte[] buffer;
        try (HttpEntity entity = builder.build();
                ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            entity.writeTo(os);
            os.flush();
            buffer = os.toByteArray();
        }
        return buffer;
    }

    /**
     * Create a file in the plugins user content directory
     *
     * @param r the JenkinsRule
     * @return the FilePath
     * @throws Exception in case anything goes wrong
     */
    public static FilePath createCustomIconFile(JenkinsRule r) throws Exception {
        FilePath userContent = r.jenkins.getRootPath().child(CustomFolderIconConfiguration.USER_CONTENT_PATH);
        FilePath iconDir = userContent.child(CustomFolderIconConfiguration.PLUGIN_PATH);
        iconDir.mkdirs();

        FilePath file = iconDir.child(UUID.randomUUID() + ".png");
        file.copyFrom(new FilePath(new File("./src/main/webapp/icons/default.svg")));

        assertTrue(file.exists());

        return file;
    }
}
