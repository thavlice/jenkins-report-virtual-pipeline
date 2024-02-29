package io.jenkins.plugins.VirtualPipeline;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Extension
public class VirtualPipelineBuildWrapper extends SimpleBuildWrapper {

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





    public static class VirtualPipelineLogFilterImpl extends ConsoleLogFilter {


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
