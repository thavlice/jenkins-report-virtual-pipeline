package io.jenkins.plugins.VirtualPipeline;

import org.kohsuke.stapler.DataBoundConstructor;

public class VirtualPipelineFormInput {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    private String name;

    private String regex;

    @DataBoundConstructor
    public VirtualPipelineFormInput(String name, String regex){
        this.name = name;
        this.regex = regex;
    }



}
