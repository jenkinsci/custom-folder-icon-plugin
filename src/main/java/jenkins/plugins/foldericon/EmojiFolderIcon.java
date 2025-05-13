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
        this.emoji = StringUtils.isBlank(emoji) ? DEFAULT_ICON : emoji;
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
