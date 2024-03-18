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
import com.cloudbees.hudson.plugins.folder.FolderIcon;
import com.cloudbees.hudson.plugins.folder.FolderIconDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.BallColor;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;

/**
 * A Build Status Folder Icon.
 *
 * @author strangelookingnerd
 */
public class BuildStatusFolderIcon extends FolderIcon {

    private final Set<String> jobs;

    private AbstractFolder<?> owner;

    /**
     * Ctor.
     *
     * @param jobs the jobs to consider for combined build status (null / empty means all jobs).
     */
    @DataBoundConstructor
    public BuildStatusFolderIcon(Set<String> jobs) {
        this.jobs = jobs;
    }

    @Override
    protected void setOwner(AbstractFolder<?> folder) {
        this.owner = folder;
    }

    /**
     * @return the jobs to consider for combined build status (null / empty means all jobs).
     */
    public Set<String> getJobs() {
        return jobs;
    }

    /**
     * @return all available jobs in the current folder.
     */
    public Set<String> getAvailableJobs() {
        return getAllJobs().stream()
                .map(job -> job.getRelativeDisplayNameFrom(owner))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @SuppressWarnings("rawtypes")
    private Collection<? extends Job> getAllJobs() {
        if (owner != null) {
            return owner.getAllJobs();
        } else {
            return Set.of();
        }
    }

    @SuppressWarnings("rawtypes")
    private Collection<? extends Job> getConfiguredJobs() {
        Collection<? extends Job> availableJobs = getAllJobs();
        Collection<? extends Job> filteredJobs = new HashSet<>();
        Set<String> configuredJobs = getJobs();

        if (configuredJobs != null) {
            // filter jobs that exist and are configured
            filteredJobs = availableJobs.stream()
                    .filter(job -> configuredJobs.contains(job.getRelativeDisplayNameFrom(owner)))
                    .collect(Collectors.toSet());
        }

        // if filtered result is empty return all available instead
        return filteredJobs.isEmpty() ? availableJobs : filteredJobs;
    }

    private BallColor getCombinedBallColor() {
        var configuredJobs = getConfiguredJobs();

        Result combinedResult = null;
        boolean buildable = false;
        boolean running = false;
        boolean empty = configuredJobs.isEmpty();

        for (var job : configuredJobs) {
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

        BallColor color;
        if (combinedResult != null) {
            color = combinedResult.color;
        } else if (empty || buildable) {
            color = BallColor.NOTBUILT;
        } else {
            color = BallColor.DISABLED;
        }

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
     */
    @Extension
    public static class DescriptorImpl extends FolderIconDescriptor {

        @Override
        @NonNull
        public String getDisplayName() {
            return Messages.BuildStatusFolderIcon_description();
        }
    }
}
