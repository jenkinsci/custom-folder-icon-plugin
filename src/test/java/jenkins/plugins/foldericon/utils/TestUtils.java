/* ******************************************************************
 * Copyright (c) 2003-2022 by PROSTEP AG                             *
 * All rights reserved                                               *
 *                                                                   *
 *   ___  ___  ___  _  _  ___  __   _    _                           *
 *  | _ || _ || __|| \| || _ || _ \| \  / |                          *
 *  ||_||| /_|| _/ |  ' || /_|||_ ||  ''  |                          *
 *  |___||_|  |___||_|\_||_|  |__ /|_|\/|_|                          *
 *                                                                   *
 * This software is furnished under a license and may be used and    *
 * copied only in accordance with the terms of such license and      *
 * with the inclusion of the above copyright notice. This            *
 * software or any other copies thereof may not be provided or       *
 * otherwise made available to a third person. No title to and       *
 * ownership of the software is hereby transferred.                  *
 *                                                                   *
 * The information in this software is subject to change without     *
 * notice and should not be construed as a commitment by ProSTEP     *
 *                                                                   *
 * The PROSTEP AG assumes only responsibility defined in a contract  *
 * and no responsibility for the use or reliability of its software  *
 * on equipment which is not supplied by the PROSTEP AG.             *
 *                                                                   *
 *********************************************************************/

//
// Author      : dkraemer
// Start       : 05.07.2022
//
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
 * @author dkraemer
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
