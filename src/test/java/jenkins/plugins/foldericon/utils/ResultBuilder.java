package jenkins.plugins.foldericon.utils;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

/**
 * Force build results.
 */
public class ResultBuilder extends Builder {

    private final Result result;

    /**
     * Ctor.
     *
     * @param result the desired result.
     */
    public ResultBuilder(Result result) {
        this.result = result;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        build.setResult(result);
        return true;
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
        return new BuildStepDescriptor<>() {
            @Override
            public boolean isApplicable(Class<? extends AbstractProject> jobType) {
                return true;
            }
        };
    }
}
