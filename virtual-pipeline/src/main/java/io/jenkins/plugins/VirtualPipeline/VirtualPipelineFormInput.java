package io.jenkins.plugins.VirtualPipeline;

import org.kohsuke.stapler.DataBoundConstructor;

public class VirtualPipelineFormInput {

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
    public VirtualPipelineFormInput(String regex, Boolean deleteMark) {
        this.regex = regex;
        this.deleteMark = deleteMark;
    }


}
