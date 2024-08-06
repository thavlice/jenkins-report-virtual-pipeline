/*
 * The MIT License
 *
 * Copyright 2023
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.jenkins.plugins.VirtualPipeline.actions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Action;
import hudson.model.Run;
import io.jenkins.plugins.VirtualPipeline.model.HistoryType;
import io.jenkins.plugins.VirtualPipeline.model.VirtualPipelineLineOutput;
import io.jenkins.plugins.VirtualPipeline.model.VirtualPipelineOutputHistoryMarked;
import io.jenkins.plugins.VirtualPipeline.VirtualPipelineRecorder;
import io.jenkins.plugins.VirtualPipeline.input.VirtualPipelineInput;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class VirtualPipelineProjectAction implements SimpleBuildStep.LastBuildAction {

    private final Run<?, ?> run;

    private final List<VirtualPipelineInput> configurations;

    private final File cacheFile;

    private final Boolean compareAgainstLastStableBuild;


    public VirtualPipelineProjectAction(Run<?, ?> run, List<VirtualPipelineInput> configurations, File cacheFolder, Boolean compareAgainstLastStableBuild) {
        this.run = run;
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


    public List<VirtualPipelineOutputHistoryMarked> getHistoryMarkedLines() {
        File comparingFile; // build File can be changed here
        if (compareAgainstLastStableBuild) {
            comparingFile = this.getLastStableBuildFile();
        } else {
            comparingFile = this.getPreviousBuildFile();
        }


        List<VirtualPipelineLineOutput> currentBuildLines = getAllCacheFromFile();
        List<VirtualPipelineLineOutput> previousBuildLines = getAllCacheFromNamedFile(comparingFile);

        List<VirtualPipelineOutputHistoryMarked> result = new ArrayList<>();


        //comparing
        for (int lineIndex = 0; lineIndex < currentBuildLines.size(); lineIndex++) {
            VirtualPipelineLineOutput currentLine = currentBuildLines.get(lineIndex);
            VirtualPipelineLineOutput previousLine = previousBuildLines.get(lineIndex);

            if (currentLine.getDisplay() || previousLine.getDisplay()) { // comparing only lines to display

                // lines are same
                if (Objects.equals(currentLine.getLine(), previousLine.getLine())) {
                    result.add(new VirtualPipelineOutputHistoryMarked(currentLine.getRegex(), currentLine.getLine(),
                            currentLine.getIndex(), currentLine.getDeleteMark(), currentLine.getType(),
                            HistoryType.SAME, currentLine.getLineStartOffset(), currentLine.getDisplay()));

                } else if (Objects.equals(currentLine.getRegex(), previousLine.getRegex())) { //regex is same, line not
                    result.add(new VirtualPipelineOutputHistoryMarked(currentLine.getRegex(), currentLine.getLine(),
                            currentLine.getIndex(), currentLine.getDeleteMark(), currentLine.getType(),
                            HistoryType.JUST_SAME_REGEX, currentLine.getLineStartOffset(), currentLine.getDisplay()));

                } else { // different lines, different regex
                    result.add(new VirtualPipelineOutputHistoryMarked(currentLine.getRegex(), currentLine.getLine(),
                            currentLine.getIndex(), currentLine.getDeleteMark(), currentLine.getType(),
                            HistoryType.DIFFERENT_CURRENT, currentLine.getLineStartOffset(), currentLine.getDisplay()));
                    result.add(new VirtualPipelineOutputHistoryMarked(previousLine.getRegex(), previousLine.getLine(),
                            previousLine.getIndex(), previousLine.getDeleteMark(), previousLine.getType(),
                            HistoryType.DIFFERENT_PREVIOUS, previousLine.getLineStartOffset(), previousLine.getDisplay()));
                }

            }

        }

        return result;

    }


    // for shortened summary on build and project page
    public List<VirtualPipelineLineOutput> getOnlyMarkedLinesToDisplay() {
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

    private File getPreviousBuildFile() {
        Run<?, ?> previousBuild = this.getRun().getPreviousBuild();
        if (Objects.isNull(previousBuild)) {
            return null;
        }
        int buildNumber = previousBuild.getNumber();
        File buildFolder = getBuildFolderFromBuildNumber(buildNumber);
        return new File(buildFolder + File.separator + VirtualPipelineRecorder.cacheName);
    }

    private File getLastStableBuildFile() {
        Run<?, ?> previousLastStableBuild = this.getRun().getPreviousCompletedBuild();
        if (Objects.isNull(previousLastStableBuild)) {
            return null;
        }
        int buildNumber = previousLastStableBuild.getNumber();
        File buildFolder = getBuildFolderFromBuildNumber(buildNumber);
        return new File(buildFolder + File.separator + VirtualPipelineRecorder.cacheName);
    }

    public File getProjectDirFile() {
        return run.getRootDir();
    }

    public File getBuildFolderFromBuildNumber(int buildNumber) {
        return new File(this.getProjectDirFile() + File.separator + "builds" + File.separator + buildNumber);
    }

    private List<VirtualPipelineLineOutput> getAllCacheFromNamedFile(File file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(file, new TypeReference<List<VirtualPipelineLineOutput>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getJenkinsRootUrl(){
        Jenkins instance = Jenkins.getInstanceOrNull();
        if(instance == null){
            return "";
        }
        return instance.getRootUrlFromRequest();
    }

    public Run<?, ?> getRun() {
        return run;
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
        list.add(new VirtualPipelineProjectAction(this.getRun(), this.getConfigurations(), this.getCacheFile(), compareAgainstLastStableBuild));
        return list;
    }


}
