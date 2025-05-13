package jenkins.plugins.foldericon;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * An URL Folder Icon.
 */
public class UrlFolderIcon extends FolderIcon {

    private static final String DEFAULT_ICON_PATH = "plugin/custom-folder-icon/icons/default.svg";

    private final String url;

    private AbstractFolder<?> owner;

    /**
     * Ctor.
     *
     * @param url the url to use
     */
    @DataBoundConstructor
    public UrlFolderIcon(String url) {
        this.url = url;
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    @Override
    public String getImageOf(String size) {
        if (StringUtils.isNotBlank(getUrl())) {
            return getUrl();
        } else {
            return Jenkins.get().getRootUrl() + DEFAULT_ICON_PATH;
        }
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
            return Messages.UrlFolderIcon_description();
        }

        @RequirePOST
        public FormValidation doCheckUrl(@QueryParameter String value) {
            if (StringUtils.isNotBlank(value) && !StringUtils.startsWithIgnoreCase(value, "http")) {
                return FormValidation.error(Messages.Url_invalidUrl());
            }
            return FormValidation.ok();
        }
    }
}
