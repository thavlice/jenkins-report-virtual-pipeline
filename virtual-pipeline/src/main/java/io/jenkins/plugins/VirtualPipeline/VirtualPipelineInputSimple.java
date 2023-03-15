package io.jenkins.plugins.VirtualPipeline;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

public class VirtualPipelineInputSimple extends VirtualPipelineInput{

    private String regex;

    private Boolean deleteMark;


    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public Boolean getDeleteMark() {
        return deleteMark;
    }

    public void setDeleteMark(Boolean deleteMark) {
        this.deleteMark = deleteMark;
    }

    @DataBoundConstructor
    public VirtualPipelineInputSimple(String regex, Boolean deleteMark){
        super();
        this.regex = regex;
        this.deleteMark = deleteMark;
    }

    @Extension
    public static final class DescriptorImpl extends VirtualPipelineInputDescriptor{

    }

    public DescriptorExtensionList<VirtualPipelineInput, VirtualPipelineInputDescriptor> getFormatDescriptors(){
        return Jenkins.get().getDescriptorList(VirtualPipelineInput.class);
    }

    public VirtualPipelineInput getFormat(){
        return null;
    }
}
