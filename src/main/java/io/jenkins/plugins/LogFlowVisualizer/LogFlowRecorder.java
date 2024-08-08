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

package io.jenkins.plugins.LogFlowVisualizer;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.console.ConsoleNote;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import io.jenkins.plugins.LogFlowVisualizer.actions.LogFlowHTMLAction;
import io.jenkins.plugins.LogFlowVisualizer.actions.LogFlowHistoryDiffAction;
import io.jenkins.plugins.LogFlowVisualizer.actions.LogFlowOffsetAction;
import io.jenkins.plugins.LogFlowVisualizer.actions.LogFlowProjectAction;
import io.jenkins.plugins.LogFlowVisualizer.input.LogFlowInput;
import io.jenkins.plugins.LogFlowVisualizer.input.LogFlowInputDescriptor;
import io.jenkins.plugins.LogFlowVisualizer.model.LineWithOffset;
import io.jenkins.plugins.LogFlowVisualizer.model.LineOutput;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;




/**
 * responsible for performing all actions of the plugin
 */
@Extension
public class LogFlowRecorder extends Recorder implements SimpleBuildStep {

    public static final String cacheName = "LogFlowVisualizerCache.json";
    public static final String cachePictureName = "LogFlowVisualizerResult.png";
    private List<LogFlowInput> configurations;
    private Boolean generatePicture = false;
    private Boolean compareAgainstLastStableBuild = false;

    int DEFAULT_IMAGE_WIDTH = 1200;
    int DEFAULT_IMAGE_HEIGHT = 800;


    public LogFlowRecorder() {
    }

    @DataBoundConstructor
    public LogFlowRecorder(List<LogFlowInput> configurations, Boolean generatePicture, Boolean compareAgainstLastStableBuild) {
        this.configurations = configurations;
        this.generatePicture = generatePicture;
        this.compareAgainstLastStableBuild = compareAgainstLastStableBuild;
    }

    public Boolean getCompareAgainstLastStableBuild() {
        return compareAgainstLastStableBuild;
    }

    @DataBoundSetter
    public void setCompareAgainstLastStableBuild(Boolean compareAgainstLastStableBuild) {
        this.compareAgainstLastStableBuild = compareAgainstLastStableBuild;
    }

    public Boolean getGeneratePicture() {
        return generatePicture;
    }

    @DataBoundSetter
    public void setGeneratePicture(Boolean generatePicture) {
        this.generatePicture = generatePicture;
    }


    public List<LogFlowInput> getConfigurations() {
        return configurations;
    }


    public void setConfigurations(List<LogFlowInput> configurations) {
        this.configurations = configurations;
    }

    public DescriptorExtensionList<LogFlowInput, LogFlowInputDescriptor> getFormatDescriptors() {
        return Jenkins.get().getDescriptorList(LogFlowInput.class);
    }


    /**
     *
     * @param run a build this is running as a part of
     * @param workspace a workspace to use for any file operations
     * @param env environment variables applicable to this step
     * @param launcher a way to start processes
     * @param listener a place to send output
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {

        if (Objects.isNull(configurations) || configurations.isEmpty()) {
            listener.getLogger().println("LFV: configurations are empty");
            return ;
        }

        //here is the performance part, e.g. drawing, printing itself
        File rootDir = run.getRootDir();
        File currentBuildFolder = new File(rootDir.getPath());
        File defaultLogs = new File(currentBuildFolder + File.separator + "log");


        //creates necessary directories
        boolean mkdirsResult = currentBuildFolder.mkdirs();
        if (mkdirsResult) {
            listener.getLogger().println("LFV: Created new directories");
        }


        RandomAccessFile raf = new RandomAccessFile(defaultLogs, "r");

        //reading log file

        List<LineWithOffset> logLines = new ArrayList<>();
        long currentOffset = raf.getFilePointer();
        String line = raf.readLine();
        while (line != null) {
            line = ConsoleNote.removeNotes(line);
            logLines.add(new LineWithOffset(line, currentOffset));
            currentOffset = raf.getFilePointer();
            line = raf.readLine();
        }
        raf.close();

        //getting filtered lines
        List<LineOutput> filterOutput = LogFlowFilter.filter(logLines, getConfigurations());

        // writing in JSON format into output.json
        File jsonCacheFile = new File(currentBuildFolder.getPath() + File.separator + cacheName);

        boolean createFileResult = jsonCacheFile.createNewFile();
        if (!createFileResult) {
            listener.getLogger().println("LFV: " + cacheName + " was not created");
            return;
        }


        LogFlowFilter.saveToJSON(filterOutput, jsonCacheFile);



        //creating picture
        if (generatePicture) {
            if (createPicture(listener, filterOutput, currentBuildFolder)) return;
        }


        // adding actions to build in Jenkins
        LogFlowProjectAction action = new LogFlowProjectAction(run, this.getConfigurations(), jsonCacheFile, compareAgainstLastStableBuild);
        run.addAction(action);
        run.addAction(new LogFlowHTMLAction(run, jsonCacheFile));
        run.addAction(new LogFlowOffsetAction(run));
        run.addAction(new LogFlowHistoryDiffAction(run, jsonCacheFile, compareAgainstLastStableBuild));

    }

    private boolean createPicture(TaskListener listener, List<LineOutput> filterOutput, File currentBuildFolder) throws IOException {
        int width = DEFAULT_IMAGE_WIDTH;
        int height = DEFAULT_IMAGE_HEIGHT;
        LogFlowPictureMaker pm = new LogFlowPictureMaker(width, height);
        BufferedImage image = pm.createPicture(filterOutput);
        File picturePath = new File(currentBuildFolder + File.separator + "archive" + File.separator + cachePictureName);
        boolean pictureMkdirResult = picturePath.mkdirs();
        if (!pictureMkdirResult) {
            listener.getLogger().println("LFV: cache directories for picture were not successfully created");
            return true;
        }
        javax.imageio.ImageIO.write(image, "png", picturePath);
        return false;
    }

    /**
     * mainly used for configuration and input checks
     */
    @Symbol("logFlowVisualizer")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        @NonNull
        public String getDisplayName() {
            return "Log Flow Visualizer";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }


}