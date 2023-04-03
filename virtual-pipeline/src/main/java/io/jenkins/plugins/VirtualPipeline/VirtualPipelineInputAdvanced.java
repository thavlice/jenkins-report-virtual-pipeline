package io.jenkins.plugins.VirtualPipeline;

import hudson.Extension;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VirtualPipelineInputAdvanced extends VirtualPipelineInput {

    private static final int DEFAULT_CONTENT_LENGTH = 100;

    private final String startMark;

    // to find specified mark in content of mark private final String namedMark;
    private final String endMark;

    private final Boolean deleteMark;
    private String maxContentLength = String.valueOf(DEFAULT_CONTENT_LENGTH);

    private int numberOfLineToDisplay = 0;

    public int getNumberOfLineToDisplay() {
        return numberOfLineToDisplay;
    }

    @DataBoundConstructor
    public VirtualPipelineInputAdvanced(String startMark, String endMark, Boolean deleteMark, String maxContentLength, int numberOfLineToDisplay) {
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

    public String getMaxContentLength() {
        return maxContentLength;
    }
    public int getMaxContentLengthToInt() {
        return Integer.parseInt(maxContentLength);
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

    }
}
