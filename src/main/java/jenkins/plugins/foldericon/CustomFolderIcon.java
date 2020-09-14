package jenkins.plugins.foldericon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
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
import jenkins.model.Jenkins;

/**
 * A Custom Folder Icon
 * 
 * @author strangelookingnerd
 *
 */
public class CustomFolderIcon extends FolderIcon {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIcon.class.getName());

    private static final String PATH = "customFolderIcons";
    private static final String USER_CONTENT = "userContent";

    public String foldericon;

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

    @Override
    public String getImageOf(String size) {
        if (foldericon != null) {
            return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH + "/" + USER_CONTENT + "/"
                    + PATH + "/"
                    + foldericon;
        } else {
            return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH
                    + "/plugin/custom-folder-icon/icons/default.png";
        }
    }

    @Override
    public String getDescription() {
        return Messages.Folder_description();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.get().getDescriptorOrDie(getClass());
    }

    /**
     * @author dkraemer
     *
     */
    @Extension
    public static class DescriptorImpl extends FolderIconDescriptor {

        private static final int CHMOD = 0644;
        private static final long FILE_SIZE_MAX = 1024L * 1024L;

        @Nonnull
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
                Jenkins jenkins = Jenkins.get();
                jenkins.checkPermission(Jenkins.ADMINISTER);

                ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                upload.setFileSizeMax(FILE_SIZE_MAX);

                // Parse the request
                List<FileItem> files = upload.parseRequest(req);
                if (files == null || files.isEmpty()) {
                    return HttpResponses.errorWithoutStack(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            Messages.Upload_invalidFile());
                }
                FileItem fileItem = files.get(0);
                if (fileItem == null || StringUtils.isEmpty(fileItem.getName())) {
                    return HttpResponses.errorWithoutStack(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            Messages.Upload_invalidFile());
                }
                String fileName = fileItem.getName();
                // we allow the upload of the new png and jpg
                if (!StringUtils.endsWithIgnoreCase(fileName, ".png")
                        && !StringUtils.endsWithIgnoreCase(fileName, ".jpg")
                        && !StringUtils.endsWithIgnoreCase(fileName, ".jpeg")) {
                    return HttpResponses.errorWithoutStack(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            Messages.Upload_invalidType());
                }

                String filename = UUID.randomUUID().toString() + ".png";
                FilePath iconDir = jenkins.getRootPath().child(USER_CONTENT).child(CustomFolderIcon.PATH);
                iconDir.mkdirs();
                FilePath icon = iconDir.child(filename);
                icon.copyFrom(fileItem.getInputStream());
                icon.chmod(CHMOD);

                return HttpResponses.text(filename);
            } catch (IOException | FileUploadException | InterruptedException ex) {
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
         * @throws IOException
         */
        public HttpResponse doCleanup(StaplerRequest req) throws InterruptedException, IOException {
            Jenkins jenkins = Jenkins.get();
            jenkins.checkPermission(Jenkins.ADMINISTER);
            
            FilePath iconDir = jenkins.getRootPath().child(USER_CONTENT).child(PATH);

            if (iconDir.exists()) {
                List<String> existingIcons = new ArrayList<>();
                for (FilePath fp : iconDir.list()) {
                    existingIcons.add(fp.getName());
                }

                List<String> usedIcons = new ArrayList<>();
                for (AbstractFolder folder : jenkins.getAllItems(AbstractFolder.class)) {
                    if (folder.getIcon() instanceof CustomFolderIcon) {
                        CustomFolderIcon icon = (CustomFolderIcon) folder.getIcon();
                        usedIcons.add(icon.foldericon);
                    }
                }

                if (existingIcons.removeAll(usedIcons)) {
                    for (String icon : existingIcons) {
                        try {
                            if (!iconDir.child(icon).delete()) {
                                LOGGER.warning("Unable to delete unused Folder Icon '" + icon + "'!");
                            }
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE, "Unable to delete unused Folder Icon '" + icon + "'!", ex);
                        }
                    }
                }
            }
            return HttpResponses.ok();
        }
    }
}

