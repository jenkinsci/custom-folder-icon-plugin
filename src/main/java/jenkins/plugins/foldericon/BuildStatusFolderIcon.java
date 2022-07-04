package jenkins.plugins.foldericon;

import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;

import hudson.Extension;
import hudson.model.BallColor;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;

/**
 * A Build Status Folder Icon.
 * 
 * @author strangelookingnerd
 *
 */
public class BuildStatusFolderIcon extends FolderIcon {

    private AbstractFolder<?> owner;

    /**
     * Ctor.
     */
    @DataBoundConstructor
    public BuildStatusFolderIcon() {
	// NOP
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
	this.owner = folder;
    }

    private BallColor getCombinedBallColor() {
	boolean empty = false;
	boolean buildable = false;
	boolean running = false;

	Result combinedResult = null;

	if (owner != null) {
	    Collection<? extends Job> jobs = owner.getAllJobs();
	    empty = jobs.isEmpty();
	    for (Job<?, ?> job : jobs) {
		if (job.isBuildable()) {
		    buildable = true;
		    Run<?, ?> build = job.getLastBuild();
		    if (build != null && build.isBuilding()) {
			running = true;
			build = build.getPreviousBuild();
		    }
		    Result result = build != null ? build.getResult() : null;
		    combinedResult = Result.combine(combinedResult, result);
		}
	    }
	}

	BallColor color = combinedResult != null ? combinedResult.color
		: empty ? BallColor.NOTBUILT : buildable ? BallColor.NOTBUILT : BallColor.DISABLED;

	return running ? color.anime() : color;
    }

    @Override
    public String getIconClassName() {
	return getCombinedBallColor().getIconClassName();
    }

    @Override
    public String getImageOf(String size) {
	return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH + "/" + "images" + "/" + size + "/"
		+ getCombinedBallColor().getImage();
    }

    @Override
    public String getDescription() {
	if (owner != null) {
	    return owner.getPronoun() + " (" + getCombinedBallColor().getDescription() + ")";
	} else {
	    return Messages.Folder_description() + " (" + getCombinedBallColor().getDescription() + ")";
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

	@Override
	public String getDisplayName() {
	    return Messages.BuildStatusFolderIcon_description();
	}

	@Override
	public boolean isApplicable(Class<? extends AbstractFolder> folderType) {
	    return true;
	}
    }
}