package jenkins.plugins.foldericon;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;

import hudson.Extension;
import hudson.FilePath;
import hudson.util.VersionNumber;
import jenkins.model.Jenkins;

/**
 * A Custom Folder Icon.
 * 
 * @author strangelookingnerd
 *
 */
public class CustomFolderIcon extends FolderIcon {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIcon.class.getName());

    private static final String PLUGIN_PATH = "customFolderIcons";
    private static final String USER_CONTENT_PATH = "userContent";
    private static final String DEFAULT_ICON_PATH = "plugin/custom-folder-icon/icons/default.png";

    private final String foldericon;

    private AbstractFolder<?> owner;

    private static final boolean USE_WORKAROUND;

    static {
        // workaround for https://issues.jenkins.io/browse/JENKINS-68894 that was introduces in 2.334 and fixed in 2.357
        // this can be removed once minimal version is >= 2.357
        VersionNumber version = Jenkins.getVersion();
        USE_WORKAROUND = version != null 
                && version.isNewerThan(new VersionNumber("2.334"))
                && version.isOlderThan(new VersionNumber("2.357"));
    }

    /**
     * Ctor.
     * 
     * @param foldericon
     *            the icon to use
     */
    @DataBoundConstructor
    public CustomFolderIcon(String foldericon) {
        this.foldericon = foldericon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    /**
     * @return the foldericon
     */
    public String getFoldericon() {
        return foldericon;
    }

    @Override
    public String getImageOf(String size) {

        if (StringUtils.isNotEmpty(getFoldericon())) {
            return (USE_WORKAROUND ? "" : Stapler.getCurrentRequest().getContextPath()) + Jenkins.RESOURCE_PATH + "/"
                    + USER_CONTENT_PATH + "/" + PLUGIN_PATH + "/" + getFoldericon();
        } else {
            return (USE_WORKAROUND ? "" : Stapler.getCurrentRequest().getContextPath()) + Jenkins.RESOURCE_PATH + "/"
                    + DEFAULT_ICON_PATH;
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
     * 
     * @author strangelookingnerd
     *
     */
    @Extension
    public static class DescriptorImpl extends FolderIconDescriptor {

        private static final int CHMOD = 0644;
        private static final long FILE_SIZE_MAX = 1024L * 1024L;

        @Override
        public String getDisplayName() {
            return Messages.Icon_description();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractFolder> folderType) {
            return true;
        }

        /**
         * Uploads an icon.
         * 
         * @param req
         *            the request containing the file
         * 
         * @return the filename or an error message
         * 
         */
        @RequirePOST
        public HttpResponse doUploadIcon(StaplerRequest req) {
            try {
                ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                upload.setFileSizeMax(FILE_SIZE_MAX);

                // Parse the request
                List<FileItem> files = upload.parseRequest(req);
                if (files == null || files.isEmpty() || files.get(0) == null) {
                    return HttpResponses.errorWithoutStack(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            Messages.Upload_invalidFile());
                }

                String filename = UUID.randomUUID().toString() + ".png";
                FilePath iconDir = Jenkins.get().getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);
                iconDir.mkdirs();
                FilePath icon = iconDir.child(filename);
                icon.copyFrom(files.get(0).getInputStream());
                icon.chmod(CHMOD);

                return HttpResponses.text(filename);
            } catch (IOException | FileUploadException | InterruptedException ex) {
                LOGGER.log(Level.WARNING, "Error during Folder Icon upload!", ex);
                return HttpResponses.errorWithoutStack(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }

        /**
         * Cleanup unused icons.
         * 
         * @param req
         *            the request
         * @return OK
         * 
         * @throws InterruptedException
         *             if there is a file handling error
         * @throws IOException
         *             if there is a file handling error
         */
        public HttpResponse doCleanup(StaplerRequest req) throws InterruptedException, IOException {
            Jenkins jenkins = Jenkins.get();
            jenkins.checkPermission(Jenkins.ADMINISTER);

            FilePath iconDir = jenkins.getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);

            if (iconDir.exists()) {
                List<String> existingIcons = iconDir.list().stream().map(FilePath::getName)
                        .collect(Collectors.toList());

                List<String> usedIcons = jenkins.getAllItems(AbstractFolder.class).stream()
                        .filter(CustomFolderIcon.class::isInstance)
                        .map(folder -> ((CustomFolderIcon) folder.getIcon()).getFoldericon())
                        .collect(Collectors.toList());

                if (usedIcons.isEmpty() || existingIcons.removeAll(usedIcons)) {
                    for (String icon : existingIcons) {
                        try {
                            if (!iconDir.child(icon).delete()) {
                                LOGGER.warning(() -> "Unable to delete unused Folder Icon '" + icon + "'!");
                            }
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, ex, () -> "Unable to delete unused Folder Icon '" + icon + "'!");
                        }
                    }
                }
            }
            return HttpResponses.ok();
        }
    }
}
