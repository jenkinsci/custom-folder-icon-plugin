package jenkins.plugins.foldericon.utils;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;

/**
 * Delay execution.
 */
public class DelayBuilder extends Builder {

    private volatile boolean lock = true;

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        while (lock) {
            Thread.onSpinWait();
        }
        return true;
    }

    /**
     * Release the lock to allow {@link DelayBuilder#perform(AbstractBuild, Launcher, BuildListener)} to finish.
     */
    public void release() {
        lock = false;
    }
}
