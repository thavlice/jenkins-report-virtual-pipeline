package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class VirtualPipelineProjectAction implements SimpleBuildStep.LastBuildAction {

    private final AbstractBuild<?, ?> build;

    private final List<VirtualPipelineInput> configurations;

    private final File cacheFile;

    private final Boolean compareAgainstLastStableBuild;


    public VirtualPipelineProjectAction(AbstractBuild<?, ?> build, List<VirtualPipelineInput> configurations, File cacheFolder, Boolean compareAgainstLastStableBuild) {
        this.build = build;
        this.configurations = configurations;
        this.cacheFile = cacheFolder;
        this.compareAgainstLastStableBuild = compareAgainstLastStableBuild;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public List<VirtualPipelineLineOutput> getAllCacheFromFile() {
        return this.getAllCacheFromNamedFile(cacheFile);
    }

    public List<VirtualPipelineOutputHistoryMarked> getHistoryMarkedLines(){
        File comparingFile;; // build File can be changed here
        if(compareAgainstLastStableBuild){
            comparingFile = this.getLastStableBuildFile();
        }else {
            comparingFile = this.getPreviousBuildFile();
        }



        List<VirtualPipelineLineOutput> currentBuildLines = getAllCacheFromFile();
        List<VirtualPipelineLineOutput> previousBuildLines = getAllCacheFromNamedFile(comparingFile);

        List<VirtualPipelineOutputHistoryMarked> result = new ArrayList<>();


        //comparing
        for (int lineIndex = 0; lineIndex < currentBuildLines.size(); lineIndex++) {
            VirtualPipelineLineOutput currentLine = currentBuildLines.get(lineIndex);
            VirtualPipelineLineOutput previousLine = previousBuildLines.get(lineIndex);

            if(currentLine.getDisplay() || previousLine.getDisplay()){ // comparing only lines to display

                if(Objects.equals(currentLine.getLine(), previousLine.getLine())){
                    result.add(new VirtualPipelineOutputHistoryMarked(currentLine.getRegex(), currentLine.getLine(),
                            currentLine.getIndex(), currentLine.getDeleteMark(),  currentLine.getType(),
                            HistoryType.SAME, currentLine.getLineStartOffset(), currentLine.getDisplay()));
                }else {
                    result.add(new VirtualPipelineOutputHistoryMarked(currentLine.getRegex(), currentLine.getLine(),
                            currentLine.getIndex(), currentLine.getDeleteMark(),  currentLine.getType(),
                            HistoryType.DIFFERENT_CURRENT, currentLine.getLineStartOffset(), currentLine.getDisplay()));
                    result.add(new VirtualPipelineOutputHistoryMarked(previousLine.getRegex(), previousLine.getLine(),
                            previousLine.getIndex(), previousLine.getDeleteMark(),  previousLine.getType(),
                            HistoryType.DIFFERENT_PREVIOUS, previousLine.getLineStartOffset(), previousLine.getDisplay()));
                }

            }

        }

        return result;

    }


    // for shortened summary on build and project page
    public List<VirtualPipelineLineOutput> getOnlyMarkedLinesToDisplay(){
        List<VirtualPipelineLineOutput> list = this.getAllCacheFromFile();
        List<VirtualPipelineLineOutput> result = new ArrayList<>();
        for (VirtualPipelineLineOutput line :
                list) {
            if (line.getDisplay()) {
                result.add(line);
            }
            }
        return result;
    }
    private File getPreviousBuildFile(){
        File buildFolder = getBuildFolderFromBuildNumber(this.getBuild().getPreviousBuild().getNumber());
        return new File(buildFolder + File.separator + "cache.json");
    }

    private File getLastStableBuildFile(){
        File buildFolder = getBuildFolderFromBuildNumber(this.getBuild().getProject().getLastStableBuild().getNumber());
        return new File(buildFolder + File.separator + "cache.json");
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
            return objectMapper.readValue(file, new TypeReference<List<VirtualPipelineLineOutput>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public List<VirtualPipelineInput> getConfigurations() {
        return configurations;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    /**
     * used for displaying action in the sidebar
     *
     * @return null because we don't use it
     */
    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "VP";
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        ArrayList<Action> list = new ArrayList<>();
        list.add(new VirtualPipelineProjectAction(this.getBuild(), this.getConfigurations(), this.getCacheFile(), compareAgainstLastStableBuild));
        return list;
    }


}
