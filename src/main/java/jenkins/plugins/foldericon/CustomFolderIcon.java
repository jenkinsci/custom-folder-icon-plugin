/*
 * The MIT License
 *
 * Copyright (c) 2023 strangelookingnerd
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
import hudson.FilePath;
import hudson.model.Item;
import jenkins.model.Jenkins;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A Custom Folder Icon.
 *
 * @author strangelookingnerd
 */
public class CustomFolderIcon extends FolderIcon {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIcon.class.getName());

    private static final String PLUGIN_PATH = "customFolderIcons";
    private static final String USER_CONTENT_PATH = "userContent";
    private static final String DEFAULT_ICON_PATH = "plugin/custom-folder-icon/icons/default.png";

    private final String foldericon;

    private AbstractFolder<?> owner;

    /**
     * Ctor.
     *
     * @param foldericon the icon to use
     */
    @DataBoundConstructor
    public CustomFolderIcon(String foldericon) {
        this.foldericon = foldericon;
    }

    /**
     * Get all icons that are currently available.
     *
     * @return all the icons that have been uploaded, sorted descending by {@link FilePath#lastModified()}.
     */
    public static List<String> getAvailableIcons() {
        try {
            FilePath iconDir = Jenkins.get().getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);

            if (iconDir.exists()) {
                return iconDir.list().stream().sorted((file1, file2) -> {
                    try {
                        return Long.compare(file2.lastModified(), file1.lastModified());
                    } catch (Exception ex) {
                        return 0;
                    }
                }).map(FilePath::getName).collect(Collectors.toList());
            } else {
                return List.of();
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.WARNING, ex, () -> "Unable to list available icons!");
            return List.of();
        }
    }

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
            return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH + "/" + USER_CONTENT_PATH + "/" + PLUGIN_PATH + "/"
                    + getFoldericon();
        } else {
            return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH + "/" + DEFAULT_ICON_PATH;
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
     */
    @Extension
    public static class DescriptorImpl extends FolderIconDescriptor {

        private static final int CHMOD = 0644;
        private static final long FILE_SIZE_MAX = 1024L * 1024L;

        @Override
        @NonNull
        public String getDisplayName() {
            return Messages.CustomFolderIcon_description();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractFolder> folderType) {
            return true;
        }

        /**
         * Uploads an icon.
         *
         * @param req  the request containing the file
         * @param item the item to configure
         * @return the filename or an error message
         */
        @RequirePOST
        public HttpResponse doUploadIcon(StaplerRequest req, @AncestorInPath Item item) {
            if (item != null) {
                item.checkPermission(Item.CONFIGURE);
            } else {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            }

            try {
                FileItem file = req.getFileItem("file");
                if (file == null || file.getSize() == 0) {
                    return HttpResponses.errorWithoutStack(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Messages.Upload_invalidFile());
                } else if (file.getSize() > FILE_SIZE_MAX) {
                    return HttpResponses.errorWithoutStack(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Messages.Upload_exceedsFileSize(file.getSize(), FILE_SIZE_MAX));
                }

                String filename = UUID.randomUUID() + ".png";
                FilePath iconDir = Jenkins.get().getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);
                iconDir.mkdirs();
                FilePath icon = iconDir.child(filename);
                icon.copyFrom(file.getInputStream());
                icon.chmod(CHMOD);

                return HttpResponses.text(filename);
            } catch (IOException | InterruptedException | ServletException ex) {
                LOGGER.log(Level.WARNING, "Error during Folder Icon upload!", ex);
                return HttpResponses.errorWithoutStack(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        }

        /**
         * Cleanup unused icons.
         *
         * @param req the request
         * @return OK
         */
        @RequirePOST
        public HttpResponse doCleanup(StaplerRequest req) {
            Jenkins jenkins = Jenkins.get();
            jenkins.checkPermission(Jenkins.ADMINISTER);

            FilePath iconDir = jenkins.getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);

            List<String> existingIcons = getAvailableIcons();

            List<String> usedIcons = jenkins.getAllItems(AbstractFolder.class).stream()
                    .filter(folder -> folder.getIcon() instanceof CustomFolderIcon)
                    .map(folder -> ((CustomFolderIcon) folder.getIcon()).getFoldericon()).collect(Collectors.toList());

            if (usedIcons.isEmpty() || existingIcons.removeAll(usedIcons)) {
                for (String icon : existingIcons) {
                    try {
                        if (!iconDir.child(icon).delete()) {
                            LOGGER.warning(() -> "Unable to delete unused Folder Icon '" + icon + "'!");
                        }
                    } catch (IOException | InterruptedException ex) {
                        LOGGER.log(Level.WARNING, ex, () -> "Unable to delete unused Folder Icon '" + icon + "'!");
                    }
                }
            }
            return HttpResponses.ok();
        }
    }
}
