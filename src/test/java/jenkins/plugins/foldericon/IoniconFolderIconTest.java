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

package jenkins.plugins.foldericon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;

import hudson.PluginManager;
import hudson.PluginWrapper;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.IoniconFolderIcon.DescriptorImpl;

/**
 * Custom Folder Icon Tests
 * 
 * @author strangelookingnerd
 *
 */

@WithJenkins
class IoniconFolderIconTest {

    private static final String DUMMY_ICON = "dummy";
    private static final String DEFAULT_ICON = "jenkins";

    private static final String ICON_CLASS_NAME_PATTERN = "symbol-%s plugin-ionicons-api";
    private static final String DUMMY_ICON_CLASS_NAME = String.format(ICON_CLASS_NAME_PATTERN, DUMMY_ICON);
    private static final String DEFAULT_ICON_CLASS_NAME = String.format(ICON_CLASS_NAME_PATTERN, DEFAULT_ICON);

    /**
     * Test behavior on a regular {@link Folder}.
     * 
     * @throws Exception
     */
    @Test
    void testFolder(JenkinsRule r) throws Exception {
	IoniconFolderIcon customIcon = new IoniconFolderIcon(null);
	assertEquals(DEFAULT_ICON, customIcon.getIonicon());
	assertNull(customIcon.getImageOf(null));
	assertEquals(DEFAULT_ICON_CLASS_NAME, customIcon.getIconClassName());

	customIcon = new IoniconFolderIcon(DUMMY_ICON);
	assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
	assertEquals(DUMMY_ICON, customIcon.getIonicon());
	assertNull(customIcon.getImageOf(null));
	assertEquals(DUMMY_ICON_CLASS_NAME, customIcon.getIconClassName());

	Folder project = r.jenkins.createProject(Folder.class, "folder");
	project.setIcon(customIcon);
	FolderIcon icon = project.getIcon();

	assertTrue(icon instanceof IoniconFolderIcon);
	assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     * 
     * @throws Exception
     */
    @Test
    void testOrganzationFolder(JenkinsRule r) throws Exception {
	IoniconFolderIcon customIcon = new IoniconFolderIcon(null);
	assertEquals(DEFAULT_ICON, customIcon.getIonicon());
	assertNull(customIcon.getImageOf(null));
	assertEquals(DEFAULT_ICON_CLASS_NAME, customIcon.getIconClassName());

	customIcon = new IoniconFolderIcon(DUMMY_ICON);
	assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
	assertEquals(DUMMY_ICON, customIcon.getIonicon());
	assertNull(customIcon.getImageOf(null));
	assertEquals(DUMMY_ICON_CLASS_NAME, customIcon.getIconClassName());

	OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
	project.setIcon(customIcon);
	FolderIcon icon = project.getIcon();

	assertTrue(icon instanceof IoniconFolderIcon);
	assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     * 
     * @throws Exception
     */
    @Test
    void testDescriptor(JenkinsRule r) throws Exception {
	IoniconFolderIcon customIcon = new IoniconFolderIcon(DUMMY_ICON);
	DescriptorImpl descriptor = customIcon.getDescriptor();
	assertEquals(Messages.IoniconFolderIcon_description(), descriptor.getDisplayName());
	assertTrue(descriptor.isApplicable(null));

	assertNotNull(descriptor.getAvailableIcons());
	assertNotNull(descriptor.doFillIoniconItems());

	assertFalse(descriptor.getAvailableIcons().isEmpty());
	assertFalse(descriptor.doFillIoniconItems().isEmpty());

	assertEquals(descriptor.getAvailableIcons().size(), descriptor.doFillIoniconItems().size());
    }

    /**
     * Test behavior of {@link DescriptorImpl} when the ionicons-api plugin is missing.
     * 
     * @throws Exception
     */
    @Test
    void testMissingIoniconPlugin(JenkinsRule r) throws Exception {
	Field field = PluginManager.class.getDeclaredField("plugins");
	field.setAccessible(true);
	List<PluginWrapper> plugins = (List<PluginWrapper>) field.get(r.getPluginManager());
	List<PluginWrapper> copy = new CopyOnWriteArrayList<>(plugins);

	plugins.removeIf(plugin -> StringUtils.equals(plugin.getShortName(), "ionicons-api"));

	try {
	    DescriptorImpl descriptor = new DescriptorImpl();
	    assertEquals(Messages.IoniconFolderIcon_description(), descriptor.getDisplayName());
	    assertTrue(descriptor.isApplicable(null));

	    assertNotNull(descriptor.getAvailableIcons());
	    assertNotNull(descriptor.doFillIoniconItems());

	    assertTrue(descriptor.getAvailableIcons().isEmpty());
	    assertTrue(descriptor.doFillIoniconItems().isEmpty());

	    assertEquals(descriptor.getAvailableIcons().size(), descriptor.doFillIoniconItems().size());
	} finally {
	    field.set(r.getPluginManager(), copy);
	}
    }

}