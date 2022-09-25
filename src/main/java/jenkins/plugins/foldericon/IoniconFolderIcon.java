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

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;
import hudson.Extension;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.ionicons.Ionicons;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A Ionicon Folder Icon.
 *
 * @author strangelookingnerd
 */
public class IoniconFolderIcon extends FolderIcon {

    private static final String DEFAULT_ICON = "jenkins";

    private final String ionicon;

    private AbstractFolder<?> owner;

    /**
     * Ctor.
     *
     * @param ionicon the icon to use
     */
    @DataBoundConstructor
    public IoniconFolderIcon(String ionicon) {
        this.ionicon = StringUtils.isEmpty(ionicon) ? DEFAULT_ICON : ionicon;
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    /**
     * @return the icon
     */
    public String getIonicon() {
        return ionicon;
    }

    @Override
    public String getImageOf(String size) {
        return null;
    }

    @Override
    public String getIconClassName() {
        return Ionicons.getIconClassName(getIonicon());
    }

    @Override
    public String getDescription() {
        if (owner != null) {
            return owner.getPronoun();
        } else {
            return Messages.Folder_description();
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.get().getDescriptorOrDie(getClass());
    }

    /**
     * The Descriptor.
     *
     * @author strangelookingnerd
     */
    @Extension
    public static class DescriptorImpl extends FolderIconDescriptor {

        private final ListBoxModel listbox = new ListBoxModel();

        /**
         * Ctor.
         * <p>
         * Populates the list of available icons.
         */
        public DescriptorImpl() {
            Ionicons.getAvailableIcons().keySet().forEach(listbox::add);
        }

        @Override
        public String getDisplayName() {
            return Messages.IoniconFolderIcon_description();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractFolder> folderType) {
            return true;
        }

        /**
         * Get a drop-down list with all available icons.
         *
         * @return the list of available icons.
         */
        public ListBoxModel doFillIoniconItems() {
            return this.listbox;
        }
    }
}
