package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.List;

public class VirtualPipelineOutput {
    private String name;

    private String regex;

    private List<String> filtered;

    private List<String> notFiltered;
    public VirtualPipelineOutput(String name, String regex, List<String> filtered, List<String> notFiltered) {
        this.name = name;
        this.regex = regex;
        this.filtered = filtered;
        this.notFiltered = notFiltered;
    }

    // just for the purpose of good deserialization
    public VirtualPipelineOutput(){
        super();
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public List<String> getFiltered() {
        return filtered;
    }

    public List<String> getNotFiltered() {
        return notFiltered;
    }
}
