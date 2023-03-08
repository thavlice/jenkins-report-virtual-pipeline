package io.jenkins.plugins.VirtualPipeline;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VirtualPipelineHTMLAction implements SimpleBuildStep.LastBuildAction {

    private final AbstractBuild<?, ?> build;

    public VirtualPipelineHTMLAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    public List<String> getLogs() throws IOException {
        Reader reader = build.getLogReader();
        BufferedReader bufferedReader = new BufferedReader(reader);
        bufferedReader.readLine();

        List<String> result = new ArrayList<>();
        String line = bufferedReader.readLine();
        while (line != null) {
            result.add(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return result;
    }

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
        return "Virtual Pipeline HTML Logs";
    }

    @Override
    public String getUrlName() {
        return "html";
    }
}
