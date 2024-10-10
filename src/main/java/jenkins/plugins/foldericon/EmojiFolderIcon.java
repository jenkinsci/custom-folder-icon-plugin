/*
 * The MIT License
 *
 * Copyright (c) 2024 strangelookingnerd
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
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.emoji.symbols.Emojis;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * An Emoji Folder Icon.
 *
 * @author strangelookingnerd
 */
public class EmojiFolderIcon extends FolderIcon {

    private static final String DEFAULT_ICON = "sloth";

    private final String emoji;

    private AbstractFolder<?> owner;

    /**
     * Ctor.
     *
     * @param emoji the emoji to use
     */
    @DataBoundConstructor
    public EmojiFolderIcon(String emoji) {
        this.emoji = StringUtils.isEmpty(emoji) ? DEFAULT_ICON : emoji;
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    /**
     * @return the emoji
     */
    public String getEmoji() {
        return emoji;
    }

    @Override
    public String getImageOf(String size) {
        return null;
    }

    @Override
    public String getIconClassName() {
        return Emojis.getIconClassName(getEmoji());
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

        @Override
        @NonNull
        public String getDisplayName() {
            return Messages.EmojiFolderIcon_description();
        }
    }
}
