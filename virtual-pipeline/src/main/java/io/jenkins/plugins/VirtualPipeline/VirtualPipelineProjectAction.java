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

                // lines are same
                if(Objects.equals(currentLine.getLine(), previousLine.getLine())){
                    result.add(new VirtualPipelineOutputHistoryMarked(currentLine.getRegex(), currentLine.getLine(),
                            currentLine.getIndex(), currentLine.getDeleteMark(),  currentLine.getType(),
                            HistoryType.SAME, currentLine.getLineStartOffset(), currentLine.getDisplay()));

                }else if(Objects.equals(currentLine.getRegex(), previousLine.getRegex())){ //regex is same, line not
                    result.add(new VirtualPipelineOutputHistoryMarked(currentLine.getRegex(), currentLine.getLine(),
                            currentLine.getIndex(), currentLine.getDeleteMark(), currentLine.getType(),
                            HistoryType.JUST_SAME_REGEX, currentLine.getLineStartOffset(), currentLine.getDisplay()));

                }else{ // different lines, different regex
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
        AbstractBuild<?,?> previousBuild =  this.getBuild().getPreviousBuild();
        if (Objects.isNull(previousBuild)){
            return null;
        }
        int buildNumber = previousBuild.getNumber();
        File buildFolder = getBuildFolderFromBuildNumber(buildNumber);
        return new File(buildFolder + File.separator + VirtualPipelinePublisher.cacheName);
    }

    private File getLastStableBuildFile(){
        AbstractBuild<?,?> previousLastStableBuild =  this.getBuild().getProject().getLastStableBuild();
        if (Objects.isNull(previousLastStableBuild)){
            return null;
        }
        int buildNumber = previousLastStableBuild.getNumber();
        File buildFolder = getBuildFolderFromBuildNumber(buildNumber);
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
