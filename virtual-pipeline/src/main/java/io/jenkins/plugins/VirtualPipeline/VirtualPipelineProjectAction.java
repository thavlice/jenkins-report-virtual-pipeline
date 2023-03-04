package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VirtualPipelineProjectAction  implements SimpleBuildStep.LastBuildAction {

    private AbstractBuild<?, ?> build;

    private List<VirtualPipelineFormInput> configurations;

    private File cacheFile;


    public VirtualPipelineProjectAction(AbstractBuild<?, ?> build, List<VirtualPipelineFormInput> configurations, File cacheFolder) {
        this.build = build;
        this.configurations = configurations;
        this.cacheFile = cacheFolder;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public List<String> getLogsShort() throws IOException {
        return this.build.getLog(1000);
    }

    public  List<VirtualPipelineLineOutput> getAllCacheFromFile(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<VirtualPipelineLineOutput> result = objectMapper.readValue(cacheFile, new TypeReference<List<VirtualPipelineLineOutput>>() {});
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public List<VirtualPipelineFormInput> getConfigurations() {
        return configurations;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/virtual-pipeline/img/list.svg";
    }

    /**
     * used for displaying action in the sidebar
     * @return null because we don't use it
     */
    @Override
    public String getDisplayName() {
        return "Virtual Pipeline HTML Logs";
    }

    @Override
    public String getUrlName() {
        return "VP";
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        ArrayList<Action> list = new ArrayList<>();
        list.add(new VirtualPipelineProjectAction(this.getBuild(), this.getConfigurations(), this.getCacheFile()));
        return list;
    }
}
