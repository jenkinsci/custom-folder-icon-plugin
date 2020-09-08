package jenkins.plugins.foldericon;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;

import hudson.Extension;
import jenkins.model.Jenkins;

/**
 * A Custom Folder Icon
 * 
 * @author strangelookingnerd
 *
 */
public class CustomFolderIcon extends FolderIcon {

    private AbstractFolder<?> owner;

    /**
     * Our constructor.
     */
    @DataBoundConstructor
    public CustomFolderIcon() {
        // NOP
    }

    @Override
    public String getDescription() {
        return "Folder";
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    @Override
    public String getImageOf(String size) {
        if (owner != null) {
            CustomFolderIconProperty prop = owner.getProperties().get(CustomFolderIconProperty.class);
            if (prop != null) {
                return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH + "/userContent/"
                        + CustomFolderIconProperty.PATH + "/" + prop.foldericon;
            }
        }
        String image = iconClassNameImageOf(size);
        return image != null ? image
                : (Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH
                        + "/plugin/cloudbees-folder/images/" + size + "/folder.png");
    }

    /**
     * Our descriptor.
     */
    @Extension
    public static class DescriptorImpl extends FolderIconDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Custom Folder Icon";
        }

    }
}
