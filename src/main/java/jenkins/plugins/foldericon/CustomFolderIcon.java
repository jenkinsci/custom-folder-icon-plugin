/*
 * The MIT License
 *
 * Copyright (c) 2024 strangelookingnerd
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

import static jenkins.plugins.foldericon.CustomFolderIconConfiguration.PLUGIN_PATH;
import static jenkins.plugins.foldericon.CustomFolderIconConfiguration.USER_CONTENT_PATH;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * A Custom Folder Icon.
 *
 * @author strangelookingnerd
 */
public class CustomFolderIcon extends FolderIcon {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIcon.class.getName());

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
    @NonNull
    public static Set<String> getAvailableIcons() {
        try {
            FilePath iconDir =
                    Jenkins.get().getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);

            if (iconDir.exists()) {
                return iconDir.list().stream()
                        .sorted(Comparator.comparingLong((FilePath file) -> {
                                    try {
                                        return file.lastModified();
                                    } catch (IOException | InterruptedException ex) {
                                        return 0;
                                    }
                                })
                                .reversed())
                        .map(FilePath::getName)
                        .collect(Collectors.toSet());
            } else {
                return Set.of();
            }
        } catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.WARNING, ex, () -> "Unable to list available icons!");
            return Set.of();
        }
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    /**
     * @return the foldericon.
     */
    public String getFoldericon() {
        return foldericon;
    }

    @Override
    public String getImageOf(String size) {
        if (StringUtils.isNotEmpty(getFoldericon())) {
            return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH + "/" + USER_CONTENT_PATH + "/"
                    + PLUGIN_PATH + "/" + getFoldericon();
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
                    return HttpResponses.errorWithoutStack(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Messages.Upload_invalidFile());
                } else if (file.getSize() > FILE_SIZE_MAX) {
                    return HttpResponses.errorWithoutStack(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            Messages.Upload_exceedsFileSize(file.getSize(), FILE_SIZE_MAX));
                }

                String filename = UUID.randomUUID() + ".png";
                FilePath iconDir =
                        Jenkins.get().getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);
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
    }

    /**
     * Item Listener to clean up unused icons when the folder is deleted.
     *
     * @author strangelookingnerd
     */
    @Extension
    public static class CustomFolderIconCleanup extends ItemListener {

        @Override
        public void onDeleted(Item item) {
            if (item instanceof AbstractFolder<?>) {
                FolderIcon icon = ((AbstractFolder<?>) item).getIcon();
                if (icon instanceof CustomFolderIcon) {
                    String foldericon = ((CustomFolderIcon) icon).getFoldericon();
                    if (StringUtils.isNotEmpty(foldericon)) {
                        // delete the icon only if there is no other usage
                        boolean orphan = Jenkins.get().getAllItems(AbstractFolder.class).stream()
                                        .filter(folder -> folder.getIcon() instanceof CustomFolderIcon
                                                && StringUtils.equals(
                                                        foldericon,
                                                        ((CustomFolderIcon) folder.getIcon()).getFoldericon()))
                                        .limit(2)
                                        .count()
                                <= 1;

                        if (orphan) {
                            FilePath iconDir = Jenkins.get()
                                    .getRootPath()
                                    .child(USER_CONTENT_PATH)
                                    .child(PLUGIN_PATH);
                            try {
                                if (!iconDir.child(foldericon).delete()) {
                                    LOGGER.warning(() -> "Unable to delete Folder Icon '" + foldericon
                                            + "' for Folder '" + item.getFullName() + "'!");
                                }
                            } catch (IOException | InterruptedException ex) {
                                LOGGER.log(
                                        Level.WARNING,
                                        ex,
                                        () -> "Unable to delete Folder Icon '" + foldericon + "' for Folder '"
                                                + item.getFullName() + "'!");
                            }
                        }
                    }
                }
            }
        }
    }
}
