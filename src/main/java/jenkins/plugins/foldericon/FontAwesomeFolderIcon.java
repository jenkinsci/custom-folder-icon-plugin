package jenkins.plugins.foldericon;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.fontawesome.FontAwesomeIcons;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A Font Awesome Folder Icon.
 */
public class FontAwesomeFolderIcon extends FolderIcon {

    private static final String DEFAULT_ICON = "brands/jenkins";

    private final String fontAwesome;

    private AbstractFolder<?> owner;

    /**
     * Ctor.
     *
     * @param fontAwesome the icon to use
     */
    @DataBoundConstructor
    public FontAwesomeFolderIcon(String fontAwesome) {
        this.fontAwesome = StringUtils.isBlank(fontAwesome) ? DEFAULT_ICON : fontAwesome;
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    /**
     * @return the icon
     */
    public String getFontAwesome() {
        return fontAwesome;
    }

    @Override
    public String getImageOf(String size) {
        return null;
    }

    @Override
    public String getIconClassName() {
        return FontAwesomeIcons.getIconClassName(getFontAwesome());
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
            return Messages.FontAwesomeFolderIcon_description();
        }
    }
}
