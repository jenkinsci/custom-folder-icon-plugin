package jenkins.plugins.foldericon.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.cloudbees.hudson.plugins.folder.FolderIcon;

/**
 * Common utils for tests.
 * 
 * @author strangelookingnerd
 *
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
     * 
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
