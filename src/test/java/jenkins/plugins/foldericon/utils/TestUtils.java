/*
 * The MIT License
 *
 * Copyright (c) 2022 strangelookingnerd
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
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

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
     * @param stapler
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

}
