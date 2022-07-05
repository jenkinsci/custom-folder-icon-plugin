package jenkins.plugins.foldericon.utils;

import java.io.IOException;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;

/**
 * Delay execution.
 * 
 * @author strangelookingnerd
 *
 */
public class DelayBuilder extends Builder {

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
	Thread.sleep(5000);
	return true;
    }
}