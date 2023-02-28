package io.jenkins.plugins.VirtualPipeline;

import org.kohsuke.stapler.DataBoundConstructor;

public class VirtualPipelineFormInput {

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }


    public Boolean getGeneratePicture() {
        return generatePicture;
    }

    public void setGeneratePicture(Boolean generatePicture) {
        this.generatePicture = generatePicture;
    }

    private String regex;
    private Boolean generatePicture;

    private Boolean deleteMark;

    public Boolean getDeleteMark() {
        return deleteMark;
    }

    public void setDeleteMark(Boolean deleteMark) {
        this.deleteMark = deleteMark;
    }

    @DataBoundConstructor
    public VirtualPipelineFormInput(String regex, Boolean deleteMark, Boolean generatePicture){
        this.regex = regex;
        this.deleteMark = deleteMark;
        this.generatePicture = generatePicture;
    }



}
