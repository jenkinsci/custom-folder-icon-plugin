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

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.PageDecorator;
import hudson.security.Permission;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * The global Custom Folder Icon configuration.
 *
 * @author strangelookingnerd
 */
@Extension
public class CustomFolderIconConfiguration extends PageDecorator {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIconConfiguration.class.getName());

    public static final String PLUGIN_PATH = "customFolderIcons";

    public static final String USER_CONTENT_PATH = "userContent";

    @NonNull
    @Override
    public GlobalConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(AppearanceCategory.class);
    }

    @NonNull
    @Override
    public Permission getRequiredGlobalConfigPagePermission() {
        return Jenkins.MANAGE;
    }

    /**
     * Get human-readable disk-usage of all icons.
     *
     * @return human-readable disk-usage
     */
    @NonNull
    public String getDiskUsage() {
        Jenkins.get().checkPermission(Jenkins.MANAGE);

        FilePath iconDir = Jenkins.get().getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);

        Set<String> existingIcons = CustomFolderIcon.getAvailableIcons();

        long total = 0L;

        for (String icon : existingIcons) {
            try {
                total += iconDir.child(icon).length();
            } catch (IOException | InterruptedException ex) {
                LOGGER.log(Level.WARNING, ex, () -> "Unable to determine size for Folder Icon '" + icon + "'!");
            }
        }
        return FileUtils.byteCountToDisplaySize(total);
    }

    /**
     * Cleanup unused icons.
     *
     * @param req the request
     * @return OK
     */
    @RequirePOST
    public HttpResponse doCleanup(@SuppressWarnings("unused") StaplerRequest req) {
        Jenkins.get().checkPermission(Jenkins.MANAGE);

        FilePath iconDir = Jenkins.get().getRootPath().child(USER_CONTENT_PATH).child(PLUGIN_PATH);

        Set<String> existingIcons = CustomFolderIcon.getAvailableIcons();

        Set<String> usedIcons = Jenkins.get().getAllItems(AbstractFolder.class).stream()
                .filter(folder -> folder.getIcon() instanceof CustomFolderIcon)
                .map(folder -> ((CustomFolderIcon) folder.getIcon()).getFoldericon())
                .collect(Collectors.toSet());

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
