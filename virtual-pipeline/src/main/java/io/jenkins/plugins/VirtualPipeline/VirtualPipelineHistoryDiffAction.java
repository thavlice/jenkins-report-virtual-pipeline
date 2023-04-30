package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.difflib.text.*;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class VirtualPipelineHistoryDiffAction implements SimpleBuildStep.LastBuildAction{
    private final AbstractBuild<?, ?> build;
    private final File cacheFile;

    private Boolean compareAgainstLastStableBuild = false;

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public Boolean getCompareAgainstLastStableBuild() {
        return compareAgainstLastStableBuild;
    }

    public VirtualPipelineHistoryDiffAction(AbstractBuild<?, ?> build, File cacheFile, Boolean compareAgainstLastStableBuild) {
        this.build = build;
        this.cacheFile = cacheFile;
        this.compareAgainstLastStableBuild = compareAgainstLastStableBuild;
    }


    public List<DiffRow> getDiffLines(){
        File comparingFile; // build File can be changed here
        if(compareAgainstLastStableBuild){
            comparingFile = this.getLastStableBuildFile();
        }else {
            comparingFile = this.getPreviousBuildFile();
        }


        List<VirtualPipelineLineOutput> currentBuildLines = getAllCacheFromFile();
        List<VirtualPipelineLineOutput> previousBuildLines = getAllCacheFromNamedFile(comparingFile);

        List<String> current = extractStringFromVirtualPipelineOutput(currentBuildLines);
        List<String> previous = extractStringFromVirtualPipelineOutput(previousBuildLines);

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();

        return generator.generateDiffRows(previous, current);
    }

    private List<String> extractStringFromVirtualPipelineOutput(List<VirtualPipelineLineOutput> inputList){
        List<String> result = new ArrayList<>();
        for (VirtualPipelineLineOutput input :
             inputList) {
            result.add(input.getLine());
        }
        return result;
    }

    public List<VirtualPipelineLineOutput> getAllCacheFromFile() {
        return this.getAllCacheFromNamedFile(cacheFile);
    }

    private File getPreviousBuildFile(){
        File buildFolder = getBuildFolderFromBuildNumber(this.getBuild().getPreviousBuild().getNumber());
        return new File(buildFolder + File.separator + VirtualPipelinePublisher.cacheName);
    }

    private File getLastStableBuildFile(){
        File buildFolder = getBuildFolderFromBuildNumber(this.getBuild().getProject().getLastStableBuild().getNumber());
        return new File(buildFolder + File.separator + VirtualPipelinePublisher.cacheName);
    }

    public File getProjectDirFile(){
        return build.getProject().getRootDir();
    }

    public File getBuildFolderFromBuildNumber(int buildNumber){
        return new File(this.getProjectDirFile() + File.separator + "builds" + File.separator + buildNumber);
    }
    private List<VirtualPipelineLineOutput> getAllCacheFromNamedFile(File file){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<VirtualPipelineLineOutput> result = objectMapper.readValue(file, new TypeReference<List<VirtualPipelineLineOutput>>() {
            });
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        return "Virtual Pipeline Diff";
    }

    @Override
    public String getUrlName() {
        return "historyDiff";
    }
}
