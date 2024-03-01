package io.jenkins.plugins.VirtualPipeline;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.model.*;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;


public class VirtualPipelineBuildWrapper extends BuildWrapper {

    public static final String cacheName = "VirtualPipelineCache.json";
    public static final String cachePictureName = "VirtualPipelineResult.png";
    private List<VirtualPipelineInput> configurations;
    private Boolean generatePicture = false;
    private Boolean compareAgainstLastStableBuild = false;
    @DataBoundConstructor
    public VirtualPipelineBuildWrapper(List<VirtualPipelineInput> configurations, Boolean generatePicture, Boolean compareAgainstLastStableBuild){
        this.configurations =  configurations;
        this.generatePicture = generatePicture;
        this.compareAgainstLastStableBuild = compareAgainstLastStableBuild;
    }

    public VirtualPipelineBuildWrapper() {
    }

    public List<VirtualPipelineInput> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<VirtualPipelineInput> configurations) {
        this.configurations = configurations;
    }

    public Boolean getGeneratePicture() {
        return generatePicture;
    }

    public void setGeneratePicture(Boolean generatePicture) {
        this.generatePicture = generatePicture;
    }

    public Boolean getCompareAgainstLastStableBuild() {
        return compareAgainstLastStableBuild;
    }

    public void setCompareAgainstLastStableBuild(Boolean compareAgainstLastStableBuild) {
        this.compareAgainstLastStableBuild = compareAgainstLastStableBuild;
    }


    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        build.addAction(new VirtualPipelineOffsetAction(build));
        build.addAction(new VirtualPipelineOffsetAction(build));
        build.addAction(new VirtualPipelineOffsetAction(build));
        build.addAction(new VirtualPipelineHTMLAction(build, new File(cacheName)));


        return super.setUp(build, launcher, listener);
    }


    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        @NonNull
        @Override
        public String getDisplayName(){
            return "Log Flow visualizer BuildWrapper";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }
    }
}
