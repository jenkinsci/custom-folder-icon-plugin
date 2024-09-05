package jenkins.plugins.foldericon.utils;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;

/**
 * Force build results.
 */
public class ResultPublisher extends Publisher {
    private final Result result;

    /**
     * Ctor.
     *
     * @param result the desired result.
     */
    @SuppressWarnings("deprecation")
    public ResultPublisher(Result result) {
        this.result = result;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        build.setResult(result);
        return true;
    }

    @Override
    public Descriptor<Publisher> getDescriptor() {
        return new Descriptor<>(ResultPublisher.class) {};
    }
}
