package jenkins.plugins.foldericon;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import hudson.util.FormValidation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.security.csp.Contributor;
import jenkins.security.csp.CspBuilder;
import jenkins.security.csp.Directive;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.accmod.restrictions.suppressions.SuppressRestrictedWarnings;
import org.kohsuke.stapler.AncestorInPath;
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
        if (getUrl() != null && !getUrl().isBlank()) {
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
        public FormValidation doCheckUrl(@AncestorInPath Item item, @QueryParameter String value) {
            if (item != null) {
                item.checkPermission(Item.CONFIGURE);
            } else {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            }
            if (value != null
                    && !value.isBlank()
                    && !value.toLowerCase(Locale.ROOT).startsWith("http")) {
                return FormValidation.error(Messages.Url_invalidUrl());
            }
            return FormValidation.ok();
        }
    }

    @Extension
    @Restricted(NoExternalUse.class)
    @SuppressRestrictedWarnings({Contributor.class, CspBuilder.class})
    public static class UrlFolderIconContributor implements Contributor {

        private static final Logger LOGGER = Logger.getLogger(UrlFolderIconContributor.class.getName());

        @Override
        public void apply(CspBuilder cspBuilder) {
            Jenkins.get().allItems(AbstractFolder.class).forEach(folder -> {
                FolderIcon icon = folder.getIcon();
                if (icon instanceof UrlFolderIcon urlFolderIcon) {
                    String url = urlFolderIcon.getUrl();
                    if (url != null && !url.isBlank()) {
                        try {
                            URI uri = new URI(url);
                            if (uri.isAbsolute()) {
                                cspBuilder.add(Directive.IMG_SRC, uri.toASCIIString());
                            }
                            // Scheme-relative URLs are not supported by this plugin, so no need to handle them here.
                        } catch (URISyntaxException e) {
                            LOGGER.log(Level.FINE, "Invalid URL: " + url, e);
                        }
                    }
                }
            });
        }
    }
}
