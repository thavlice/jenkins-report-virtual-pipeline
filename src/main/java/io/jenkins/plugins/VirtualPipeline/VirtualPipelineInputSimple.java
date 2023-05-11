package io.jenkins.plugins.VirtualPipeline;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VirtualPipelineInputSimple extends VirtualPipelineInput {

    private final String regex;

    private final Boolean deleteMark;


    @DataBoundConstructor
    public VirtualPipelineInputSimple(String regex, Boolean deleteMark) {
        super();
        this.regex = regex;
        this.deleteMark = deleteMark;
    }

    public String getRegex() {
        return regex;
    }


    public Boolean getDeleteMark() {
        return deleteMark;
    }


    public DescriptorExtensionList<VirtualPipelineInput, VirtualPipelineInputDescriptor> getFormatDescriptors() {
        return Jenkins.get().getDescriptorList(VirtualPipelineInput.class);
    }


    @Extension
    public static final class DescriptorImpl extends VirtualPipelineInputDescriptor {

        public FormValidation doCheckRegex(@QueryParameter String regex) throws IOException, ServletException {
            if (regex.isEmpty()) {
                return FormValidation.error("Regex is empty");
            }
            try {
                Pattern.compile(regex);
            } catch (PatternSyntaxException exception) {
                return FormValidation.error("Regex is invalid");
            }
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Simple Regex Format";
        }


    }
}
