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
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import hudson.model.Action;
import hudson.model.Run;
import io.jenkins.plugins.VirtualPipeline.model.VirtualPipelineLineOutput;
import io.jenkins.plugins.VirtualPipeline.VirtualPipelineRecorder;
import jenkins.tasks.SimpleBuildStep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class VirtualPipelineHistoryDiffAction implements SimpleBuildStep.LastBuildAction {
    private final Run<?, ?> run;
    private final File cacheFile;

    private Boolean compareAgainstLastStableBuild = false;

    public VirtualPipelineHistoryDiffAction(Run<?, ?> run, File cacheFile, Boolean compareAgainstLastStableBuild) {
        this.run = run;
        this.cacheFile = cacheFile;
        this.compareAgainstLastStableBuild = compareAgainstLastStableBuild;
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public Boolean getCompareAgainstLastStableBuild() {
        return compareAgainstLastStableBuild;
    }

    public int getCurrentBuildNumber() {
        return this.run.getNumber();
    }

    public int getCompareBuildNumber() {
        if (compareAgainstLastStableBuild) {
            Run<?,?> previousCompletedBuild = this.run.getPreviousCompletedBuild();
            if (previousCompletedBuild != null) {
                return previousCompletedBuild.getNumber();
            }
        }
        Run<?, ?> previousBuild = this.run.getPreviousBuild();
        if (Objects.isNull(previousBuild)) {
            return this.getCurrentBuildNumber();
        }
        return previousBuild.getNumber();
    }


    public List<DiffRow> getDiffLines() {
        File comparingFile; // build File can be changed here
        if (compareAgainstLastStableBuild) {
            comparingFile = this.getLastStableBuildFile();
        } else {
            comparingFile = this.getPreviousBuildFile();
        }
        if(comparingFile == null){
            return new ArrayList<>();
        }


        List<VirtualPipelineLineOutput> currentBuildLines = getAllCacheFromFile();

        List<VirtualPipelineLineOutput> currentBuildLinesToDisplay = currentBuildLines
                .stream()
                .filter(VirtualPipelineLineOutput::getDisplay)
                .collect(Collectors.toList());
        List<VirtualPipelineLineOutput> previousBuildLines = getAllCacheFromNamedFile(comparingFile);
        List<VirtualPipelineLineOutput> previousBuildLinesToDisplay = previousBuildLines
                .stream()
                .filter(VirtualPipelineLineOutput::getDisplay)
                .collect(Collectors.toList());

        List<String> current = extractStringFromVirtualPipelineOutput(currentBuildLinesToDisplay);
        List<String> previous = extractStringFromVirtualPipelineOutput(previousBuildLinesToDisplay);

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();

        return generator.generateDiffRows(previous, current);
    }

    private List<String> extractStringFromVirtualPipelineOutput(List<VirtualPipelineLineOutput> inputList) {
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

    private File getPreviousBuildFile() {
        Run<?, ?> previousBuild = this.getRun().getPreviousBuild();
        if (Objects.isNull(previousBuild)) {
            return null;
        }
        File buildFolder = previousBuild.getRootDir();
        return new File(buildFolder + File.separator + VirtualPipelineRecorder.cacheName);
    }

    private File getLastStableBuildFile() {
        Run<?, ?> previousLastStableBuild = this.getRun().getPreviousCompletedBuild();
        if (Objects.isNull(previousLastStableBuild)) {
            return null;
        }
        File buildFolder = previousLastStableBuild.getRootDir();
        return new File(buildFolder + File.separator + VirtualPipelineRecorder.cacheName);
    }

    private List<VirtualPipelineLineOutput> getAllCacheFromNamedFile(File file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public Collection<? extends Action> getProjectActions() {
        return new ArrayList<>();
    }

    @Override
    public String getIconFileName() {
        return "symbol-changes";
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
