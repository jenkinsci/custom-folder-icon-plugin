package jenkins.plugins.foldericon;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import org.acegisecurity.AccessDeniedException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.model.Descriptor.FormException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

/**
 * 
 * Property to store the folder icon file name.
 * 
 * @author strangelookingnerd
 *
 */
public class CustomFolderIconProperty extends FolderProperty<Folder> {

    private static final Logger LOGGER = Logger.getLogger(CustomFolderIconProperty.class.getName());

    private static final String FILENAME_FIELD = "filename";
    private static final String FOLDERICON_OBJ = "foldericon";

    private static final String MAX_FILE_SIZE_FIELD = "MAX_FILE_SIZE";

    protected static final String PATH = "customFolderIcons";

    /**
     * the current icon.
     */
    public String foldericon;


    /**
     * Constructor.
     * 
     * @param foldericon
     *            the folder icon
     * 
     */
    @DataBoundConstructor
    public CustomFolderIconProperty(String foldericon) {
        this.foldericon = foldericon;
    }

    @Override
    public AbstractFolderProperty<?> reconfigure(StaplerRequest req, JSONObject form) throws FormException {
        try {
            if (form != null && form.has(FOLDERICON_OBJ) && form.getJSONObject(FOLDERICON_OBJ).has(FILENAME_FIELD)
                    && req.getFileItem(form.getJSONObject(FOLDERICON_OBJ).getString(FILENAME_FIELD)).getSize() > 0L) {
                if (foldericon != null) {

                    Jenkins jenkins = Jenkins.get();
                    jenkins.checkPermission(Jenkins.ADMINISTER);

                    boolean unique = true;
                    for (Item item : jenkins.getAllItems()) {
                        if (item instanceof Folder) {
                            CustomFolderIconProperty prop = ((Folder) item).getProperties()
                                    .get(CustomFolderIconProperty.class);
                            if (prop != null && StringUtils.equals(prop.foldericon, foldericon) && prop != this) {
                                unique = false;
                                break;
                            }
                        }
                    }
                    if (unique) {
                        jenkins.getRootPath().child("userContent").child(PATH).child(foldericon).delete();
                    }
                }
                foldericon = handleFileUpload(req, form);
                return this;
            }
        } catch (IOException | InterruptedException | AccessDeniedException | ServletException ex) {
            LOGGER.log(Level.WARNING, "Unable to delete old icon file!", ex);
        }
        return this;
    }

    private static String handleFileUpload(StaplerRequest req, JSONObject form) throws FormException {
        try {
            Jenkins jenkins = Jenkins.get();
            jenkins.checkPermission(Jenkins.ADMINISTER);
            FileItem file = req.getFileItem(form.getJSONObject(FOLDERICON_OBJ).getString(FILENAME_FIELD));
            
            if (file.getSize() > form.getJSONObject(FOLDERICON_OBJ).getLong(MAX_FILE_SIZE_FIELD)) {
                throw new FormException(Messages.Upload_maxsize(), FOLDERICON_OBJ);
            }

            String filename = UUID.randomUUID().toString() + ".png";
            FilePath iconDir = jenkins.getRootPath().child("userContent").child(PATH);
            iconDir.mkdirs();
            FilePath icon = iconDir.child(filename);
            icon.copyFrom(file.getInputStream());
            icon.chmod(0644);
            return filename;
        } catch (IOException | InterruptedException | ServletException ex) {
            throw new FormException(Messages.Upload_failed(), ex, FOLDERICON_OBJ);
        }
    }

    /**
     * Descriptor class.
     */
    @Extension
    public static class DescriptorImpl extends FolderPropertyDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Custom Folder Icon";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractFolder> folder) {
            return true;
        }

        @Override
        public FolderProperty<?> newInstance(StaplerRequest req, JSONObject form) throws FormException {
            try {
                if (form != null && form.has(FOLDERICON_OBJ) && form.getJSONObject(FOLDERICON_OBJ).has(FILENAME_FIELD)
                        && req.getFileItem(form.getJSONObject(FOLDERICON_OBJ).getString(FILENAME_FIELD))
                                .getSize() > 0L) {
                    return new CustomFolderIconProperty(handleFileUpload(req, form));
                }
            } catch (IOException | ServletException ex) {
                throw new FormException(Messages.Upload_failed(), ex, FOLDERICON_OBJ);
            }
            return null;
        }
    }
}
