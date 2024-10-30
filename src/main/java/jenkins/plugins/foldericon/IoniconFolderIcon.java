package jenkins.plugins.foldericon;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.ionicons.Ionicons;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * An Ionicon Folder Icon.
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
     */
    @Extension
    public static class DescriptorImpl extends FolderIconDescriptor {

        @Override
        @NonNull
        public String getDisplayName() {
            return Messages.IoniconFolderIcon_description();
        }
    }
}
