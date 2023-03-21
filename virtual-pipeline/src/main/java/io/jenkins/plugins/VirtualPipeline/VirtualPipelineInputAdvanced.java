package io.jenkins.plugins.VirtualPipeline;

import hudson.Extension;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VirtualPipelineInputAdvanced extends VirtualPipelineInput{

    private static final int DEFAULT_CONTENT_LENGTH = 100;

    private String startMark;
    private String endMark;

    private Boolean deleteMark;
    private int maxContentLenght = DEFAULT_CONTENT_LENGTH;

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
        public FormValidation doCheckStartMark(@QueryParameter String startMark) throws IOException, ServletException {
            if(startMark.isEmpty()){
                return FormValidation.error("Regex is empty");
            }
            try {
                Pattern.compile(startMark);
            }catch (PatternSyntaxException exception){
                return FormValidation.error("Regex is invalid");
            }
            return FormValidation.ok();
        }


    }
}
