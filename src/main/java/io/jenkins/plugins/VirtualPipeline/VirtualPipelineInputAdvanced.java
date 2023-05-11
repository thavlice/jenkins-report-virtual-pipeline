package io.jenkins.plugins.VirtualPipeline;

import hudson.Extension;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.lang.NonNull;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VirtualPipelineInputAdvanced extends VirtualPipelineInput {

    private static final int DEFAULT_CONTENT_LENGTH = 30;

    private final String startMark;

    // to find specified mark in content of mark private final String namedMark;
    private final String endMark;

    private final Boolean deleteMark;
    private int maxContentLength = DEFAULT_CONTENT_LENGTH;

    private int numberOfLineToDisplay = 0;

    @DataBoundConstructor
    public VirtualPipelineInputAdvanced(String startMark, String endMark, Boolean deleteMark, int maxContentLength, int numberOfLineToDisplay) {
        this.startMark = startMark;
        this.endMark = endMark;
        this.deleteMark = deleteMark;
        this.maxContentLength = maxContentLength;
        this.numberOfLineToDisplay = numberOfLineToDisplay;
    }

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
        return maxContentLength;
    }

    public int getNumberOfLineToDisplay() {
        return numberOfLineToDisplay;
    }


    @Extension
    public static final class DescriptorImpl extends VirtualPipelineInputDescriptor {
        public FormValidation doCheckStartMark(@QueryParameter String startMark) throws IOException, ServletException {
            if (startMark.isEmpty()) {
                return FormValidation.error("Regex is empty");
            }
            try {
                Pattern.compile(startMark);
            } catch (PatternSyntaxException exception) {
                return FormValidation.error("Regex is invalid");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckEndMark(@QueryParameter String endMark) throws IOException, ServletException {
            if (endMark.isEmpty()) {
                return FormValidation.error("Regex is empty");
            }
            try {
                Pattern.compile(endMark);
            } catch (PatternSyntaxException exception) {
                return FormValidation.error("Regex is invalid");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckMaxContentLength(@QueryParameter String maxContentLength) {
            try {
                int inputNumber = Integer.parseInt(maxContentLength);
                if(inputNumber < 1) {
                    return FormValidation.error("Max content length should be greater than 0");
                }
                return FormValidation.ok();

            }catch (NumberFormatException e){
                return FormValidation.error("Couldn't parse number input");
            }catch (Exception e){
            return FormValidation.error("Something went wrong");
            }

        }


        public FormValidation doCheckNumberOfLineToDisplay(@QueryParameter String numberOfLineToDisplay) {
            try{
            int inputNumber = Integer.parseInt(numberOfLineToDisplay);
            if(inputNumber < 0) {
                return FormValidation.error("Number of line to display should be 0 or greater");
            }
            return FormValidation.ok();
            }catch (NumberFormatException e){
                return FormValidation.error("Couldn't parse number input");
            }catch (Exception e){
                return FormValidation.error("Something went wrong");
            }
        }

        @Override @NonNull
        public String getDisplayName() {
            return "Advanced Regex Format";
        }

    }
}
