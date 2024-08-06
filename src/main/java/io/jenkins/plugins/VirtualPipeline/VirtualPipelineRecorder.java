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

package io.jenkins.plugins.VirtualPipeline;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.console.ConsoleNote;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import io.jenkins.plugins.VirtualPipeline.actions.VirtualPipelineHTMLAction;
import io.jenkins.plugins.VirtualPipeline.actions.VirtualPipelineHistoryDiffAction;
import io.jenkins.plugins.VirtualPipeline.actions.VirtualPipelineOffsetAction;
import io.jenkins.plugins.VirtualPipeline.actions.VirtualPipelineProjectAction;
import io.jenkins.plugins.VirtualPipeline.input.VirtualPipelineInput;
import io.jenkins.plugins.VirtualPipeline.input.VirtualPipelineInputDescriptor;
import io.jenkins.plugins.VirtualPipeline.model.LineWithOffset;
import io.jenkins.plugins.VirtualPipeline.model.VirtualPipelineLineOutput;
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
public class VirtualPipelineRecorder extends Recorder implements SimpleBuildStep {

    public static final String cacheName = "VirtualPipelineCache.json";
    public static final String cachePictureName = "VirtualPipelineResult.png";
    private List<VirtualPipelineInput> configurations;
    private Boolean generatePicture = false;
    private Boolean compareAgainstLastStableBuild = false;

    int DEFAULT_IMAGE_WIDTH = 1200;
    int DEFAULT_IMAGE_HEIGHT = 800;


    public VirtualPipelineRecorder() {
    }

    @DataBoundConstructor
    public VirtualPipelineRecorder(List<VirtualPipelineInput> configurations, Boolean generatePicture, Boolean compareAgainstLastStableBuild) {
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


    public List<VirtualPipelineInput> getConfigurations() {
        return configurations;
    }


    public void setConfigurations(List<VirtualPipelineInput> configurations) {
        this.configurations = configurations;
    }

    public DescriptorExtensionList<VirtualPipelineInput, VirtualPipelineInputDescriptor> getFormatDescriptors() {
        return Jenkins.get().getDescriptorList(VirtualPipelineInput.class);
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
            listener.getLogger().println("VP: configurations are empty");
            return ;
        }

        //here is the performance part, e.g. drawing, printing itself
        File rootDir = run.getRootDir();
        File currentBuildFolder = new File(rootDir.getPath());
        File defaultLogs = new File(currentBuildFolder + File.separator + "log");


        //creates necessary directories
        boolean mkdirsResult = currentBuildFolder.mkdirs();
        if (mkdirsResult) {
            listener.getLogger().println("VP: Created new directories");
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
        List<VirtualPipelineLineOutput> filterOutput = VirtualPipelineFilter.filter(logLines, getConfigurations());

        // writing in JSON format into output.json
        File jsonCacheFile = new File(currentBuildFolder.getPath() + File.separator + cacheName);

        boolean createFileResult = jsonCacheFile.createNewFile();
        if (!createFileResult) {
            listener.getLogger().println("VP: " + cacheName + " was not created");
            return;
        }


        VirtualPipelineFilter.saveToJSON(filterOutput, jsonCacheFile);



        //creating picture
        if (generatePicture) {
            if (createPicture(listener, filterOutput, currentBuildFolder)) return;
        }

        //todo remove
        System.out.println(jsonCacheFile.getPath());
        System.out.println(jsonCacheFile.getAbsolutePath());


        // adding actions to build in Jenkins
        VirtualPipelineProjectAction action = new VirtualPipelineProjectAction(run, this.getConfigurations(), jsonCacheFile, compareAgainstLastStableBuild);
        run.addAction(action);
        run.addAction(new VirtualPipelineHTMLAction(run, jsonCacheFile));
        run.addAction(new VirtualPipelineOffsetAction(run));
        run.addAction(new VirtualPipelineHistoryDiffAction(run, jsonCacheFile, compareAgainstLastStableBuild));

    }

    private boolean createPicture(TaskListener listener, List<VirtualPipelineLineOutput> filterOutput, File currentBuildFolder) throws IOException {
        int width = DEFAULT_IMAGE_WIDTH;
        int height = DEFAULT_IMAGE_HEIGHT;
        VirtualPipelinePictureMaker pm = new VirtualPipelinePictureMaker(width, height);
        BufferedImage image = pm.createPicture(filterOutput);
        File picturePath = new File(currentBuildFolder + File.separator + "archive" + File.separator + cachePictureName);
        boolean pictureMkdirResult = picturePath.mkdirs();
        if (!pictureMkdirResult) {
            listener.getLogger().println("VP: cache directories for picture were not successfully created");
            return true;
        }
        javax.imageio.ImageIO.write(image, "png", picturePath);
        return false;
    }

    /**
     * mainly used for configuration and input checks
     */
    @Symbol("virtualPipeline")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        @NonNull
        public String getDisplayName() {
            return "Virtual Pipeline";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }


}