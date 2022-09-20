/*
 * The MIT License
 *
 * Copyright (c) 2022 strangelookingnerd
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;

import hudson.Extension;
import hudson.Plugin;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

/**
 * A Ionicon Folder Icon.
 * 
 * @author strangelookingnerd
 *
 */
public class IoniconFolderIcon extends FolderIcon {

    private static final Logger LOGGER = Logger.getLogger(IoniconFolderIcon.class.getName());

    private static final String DEFAULT_ICON = "jenkins";
    private static final String ICON_CLASS_NAME_PATTERN = "symbol-%s plugin-ionicons-api";

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
	return String.format(ICON_CLASS_NAME_PATTERN, getIonicon());
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

	private static final String SVG_FORMAT = ".svg";
	private static final String IMAGES_SYMBOLS_PATH = "images/symbols/";

	private final ListBoxModel listbox = new ListBoxModel();
	private final Set<String> availableIcons = new TreeSet<>();

	/**
	 * Ctor.
	 * 
	 * Populates the list of available icons.
	 */
	public DescriptorImpl() {
	    Plugin plugin = Jenkins.get().getPlugin("ionicons-api");

	    if (plugin != null) {
		URL baseUrl = plugin.getWrapper().baseResourceURL;

		try (InputStream is = new URL(baseUrl.toExternalForm() + "WEB-INF/lib/ionicons-api.jar").openStream();
			ZipInputStream zip = new ZipInputStream(is)) {
		    while (true) {
			ZipEntry entry = zip.getNextEntry();
			if (entry == null) {
			    break;
			}
			String name = entry.getName();
			if (StringUtils.startsWith(name, IMAGES_SYMBOLS_PATH) && StringUtils.endsWith(name, SVG_FORMAT)) {
			    availableIcons.add(StringUtils.substringAfter(StringUtils.removeEnd(name, SVG_FORMAT), IMAGES_SYMBOLS_PATH));
			}
		    }
		} catch (IOException ex) {
		    LOGGER.log(Level.WARNING, "Unable to read available ionicons.", ex);
		}
	    } else {
		LOGGER.warning("Unable to read available ionicons: Plugin unavailable.");
	    }
	    this.availableIcons.stream().forEach(listbox::add);
	}

	@Override
	public String getDisplayName() {
	    return Messages.IoniconFolderIcon_description();
	}

	@Override
	public boolean isApplicable(Class<? extends AbstractFolder> folderType) {
	    return true;
	}

	/**
	 * Get a drop-down list with all available icons.
	 * 
	 * @return the list of available icons.
	 */
	public ListBoxModel doFillIoniconItems() {
	    return this.listbox;
	}

	/**
	 * Get a set of all available icons.
	 * 
	 * @return the set of available icons.
	 */
	public Set<String> getAvailableIcons() {
	    return this.availableIcons;
	}
    }
}
