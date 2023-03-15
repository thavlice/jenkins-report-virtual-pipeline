package io.jenkins.plugins.VirtualPipeline;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class VirtualPipelineInputAdvanced extends VirtualPipelineInput{

    private static final int DEFAULT_CONTENT_LENGTH = 100;

    private String startMark;
    private String endMark;

    private Boolean deleteMark;
    private int maxContentLenght;

    public String getStartMark() {
        return startMark;
    }

    public String getEndMark() {
        return endMark;
    }

    public Boolean getDeleteMark() {
        return deleteMark;
    }

    public int getMaxContentLength() {
        return maxContentLenght;
    }



    @DataBoundConstructor
    public VirtualPipelineInputAdvanced(String startMark, String endMark, Boolean deleteMark, int maxContentLenght){
        super();
        this.startMark =  startMark;
        this.endMark = endMark;
        this.deleteMark = deleteMark;
        this.maxContentLenght = maxContentLenght;
    }

    @Extension
    public static final class DescriptorImpl extends VirtualPipelineInputDescriptor{

    }
}
