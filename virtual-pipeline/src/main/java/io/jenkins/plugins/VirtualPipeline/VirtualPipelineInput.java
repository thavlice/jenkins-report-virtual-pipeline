package io.jenkins.plugins.VirtualPipeline;

import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

public abstract class VirtualPipelineInput implements Describable<VirtualPipelineInput>, ExtensionPoint {
    //TODO change to protected
    public VirtualPipelineInput(){

    }

    @Override
    public Descriptor<VirtualPipelineInput> getDescriptor(){
        return Jenkins.get().getDescriptorOrDie(this.getClass());
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<VirtualPipelineInput> {
        public String getDisplayName() { return ""; }
    }
}
