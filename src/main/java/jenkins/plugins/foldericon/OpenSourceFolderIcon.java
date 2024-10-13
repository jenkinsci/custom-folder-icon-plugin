package jenkins.plugins.foldericon;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.oss.symbols.OpenSourceSymbols;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * An Open Source Folder Icon.
 */
public class OpenSourceFolderIcon extends FolderIcon {

    private static final String DEFAULT_ICON = "cdf-icon-color";

    private final String ossicon;

    private AbstractFolder<?> owner;

    /**
     * Ctor.
     *
     * @param ossicon the icon to use
     */
    @DataBoundConstructor
    public OpenSourceFolderIcon(String ossicon) {
        this.ossicon = StringUtils.isEmpty(ossicon) ? DEFAULT_ICON : ossicon;
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    /**
     * @return the icon
     */
    public String getOssicon() {
        return ossicon;
    }

    @Override
    public String getImageOf(String size) {
        return null;
    }

    @Override
    public String getIconClassName() {
        return OpenSourceSymbols.getIconClassName(getOssicon());
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
            return Messages.OpenSourceFolderIcon_description();
        }
    }
}
