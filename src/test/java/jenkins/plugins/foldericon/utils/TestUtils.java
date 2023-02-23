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

package jenkins.plugins.foldericon.utils;

import com.cloudbees.hudson.plugins.folder.FolderIcon;
import jenkins.plugins.foldericon.Emojis;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Common utils for tests.
 *
 * @author strangelookingnerd
 */
public final class TestUtils {

    private static final String JENKINS_CONTEXT_PATH = "/jenkins";

    private TestUtils() {
        // hidden
    }

    /**
     * Common mocking of a {@link StaplerRequest}.
     *
     * @param stapler the static mock to use
     * @return the mocked request
     */
    public static StaplerRequest mockStaplerRequest(MockedStatic<Stapler> stapler) {
        StaplerRequest mockReq = Mockito.mock(StaplerRequest.class);
        stapler.when(Stapler::getCurrentRequest).thenReturn(mockReq);
        Mockito.when(mockReq.getContextPath()).thenReturn(JENKINS_CONTEXT_PATH);
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
     * @param expectedCode         the expected CODE or 0 if none
     * @param expectedTextPattern  the expected TEXT pattern
     * @param expectedErrorMessage the expected ERROR_MESSAGE
     * @throws Exception in case anything goes wrong
     */
    public static void validateResponse(HttpResponse response, int expectedCode, String expectedTextPattern, String expectedErrorMessage) throws Exception {
        if (expectedCode != 0) {
            Field code = response.getClass().getDeclaredField("val$code");
            code.setAccessible(true);
            assertEquals(expectedCode, code.get(response));
        }

        if (StringUtils.isNotEmpty(expectedTextPattern)) {
            Field text = response.getClass().getDeclaredField("val$text");
            text.setAccessible(true);
            assertThat((String) text.get(response), matchesPattern(expectedTextPattern));
        }

        if (StringUtils.isNotEmpty(expectedErrorMessage)) {
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
        try (HttpEntity entity = builder.build(); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            entity.writeTo(os);
            os.flush();
            buffer = os.toByteArray();
        }
        return buffer;
    }

    /**
     * Create a new instance of {@link Emojis} and validate it to be "empty".
     *
     * @throws Exception in case anything goes wrong
     */
    public static void validateEmojisInstance() throws Exception {
        Constructor<Emojis> ctor = Emojis.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Emojis emojis = ctor.newInstance();

        Field availableIconsField = emojis.getClass().getDeclaredField("availableIcons");
        availableIconsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> availableIcons = (Map<String, String>) availableIconsField.get(emojis);

        assertNotNull(availableIcons);
        assertTrue(availableIcons.isEmpty());

        Field availableEmojisField = emojis.getClass().getDeclaredField("availableEmojis");
        availableEmojisField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> availableEmojis = (Map<String, String>) availableEmojisField.get(emojis);

        assertNotNull(availableEmojis);
        assertTrue(availableEmojis.isEmpty());
    }

}
