package io.jenkins.plugins.VirtualPipeline;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collection;

public class VirtualPipelineHTMLAction implements SimpleBuildStep.LastBuildAction {

    @Override
    public Collection<? extends Action> getProjectActions() {
        ArrayList<Action> list = new ArrayList<>();
        return list;
    }

    @Override
    public String getIconFileName() {
        return "empty";
    }

    @Override
    public String getDisplayName() {
        return "HTML Logs";
    }

    @Override
    public String getUrlName() {
        return "html";
    }
}
