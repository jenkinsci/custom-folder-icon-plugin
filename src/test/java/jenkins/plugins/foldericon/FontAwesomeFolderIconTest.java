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
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import io.jenkins.plugins.fontawesome.FontAwesomeIcons;
import jenkins.branch.OrganizationFolder;
import jenkins.plugins.foldericon.FontAwesomeFolderIcon.DescriptorImpl;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Font Awesome Folder Icon Tests
 *
 * @author strangelookingnerd
 */
@WithJenkins
class FontAwesomeFolderIconTest {

    private static final String DUMMY_ICON = "dummy";
    private static final String DEFAULT_ICON = "brands/jenkins";

    private static final String DUMMY_ICON_CLASS_NAME = FontAwesomeIcons.getIconClassName(DUMMY_ICON);
    private static final String DEFAULT_ICON_CLASS_NAME = FontAwesomeIcons.getIconClassName(DEFAULT_ICON);

    /**
     * Test behavior on a regular {@link Folder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testFolder(JenkinsRule r) throws Exception {
        FontAwesomeFolderIcon customIcon = new FontAwesomeFolderIcon(null);
        assertEquals(DEFAULT_ICON, customIcon.getFontAwesome());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DEFAULT_ICON_CLASS_NAME, customIcon.getIconClassName());

        customIcon = new FontAwesomeFolderIcon(DUMMY_ICON);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
        assertEquals(DUMMY_ICON, customIcon.getFontAwesome());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DUMMY_ICON_CLASS_NAME, customIcon.getIconClassName());

        Folder project = r.jenkins.createProject(Folder.class, "folder");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof FontAwesomeFolderIcon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior on a {@link OrganizationFolder}.
     *
     * @throws Exception in case anything goes wrong
     */
    @Test
    void testOrganizationFolder(JenkinsRule r) throws Exception {
        FontAwesomeFolderIcon customIcon = new FontAwesomeFolderIcon(null);
        assertEquals(DEFAULT_ICON, customIcon.getFontAwesome());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DEFAULT_ICON_CLASS_NAME, customIcon.getIconClassName());

        customIcon = new FontAwesomeFolderIcon(DUMMY_ICON);
        assertTrue(StringUtils.startsWith(customIcon.getDescription(), Messages.Folder_description()));
        assertEquals(DUMMY_ICON, customIcon.getFontAwesome());
        assertNull(customIcon.getImageOf(null));
        assertEquals(DUMMY_ICON_CLASS_NAME, customIcon.getIconClassName());

        OrganizationFolder project = r.jenkins.createProject(OrganizationFolder.class, "org");
        project.setIcon(customIcon);
        FolderIcon icon = project.getIcon();

        assertTrue(icon instanceof FontAwesomeFolderIcon);
        assertTrue(StringUtils.startsWith(icon.getDescription(), project.getPronoun()));
    }

    /**
     * Test behavior of {@link DescriptorImpl}.
     */
    @Test
    void testDescriptor(@SuppressWarnings("unused") JenkinsRule r) {
        FontAwesomeFolderIcon customIcon = new FontAwesomeFolderIcon(DUMMY_ICON);
        DescriptorImpl descriptor = customIcon.getDescriptor();
        assertEquals(Messages.FontAwesomeFolderIcon_description(), descriptor.getDisplayName());
        assertTrue(descriptor.isApplicable(null));
    }

}
