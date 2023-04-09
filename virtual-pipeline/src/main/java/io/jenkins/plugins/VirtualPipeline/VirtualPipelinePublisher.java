package io.jenkins.plugins.VirtualPipeline;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


/**
 * responsible for performing all actions of the plugin
 */
public class VirtualPipelinePublisher extends Recorder implements SimpleBuildStep {

    private List<VirtualPipelineInput> configurations;
    private Boolean generatePicture;

    private Boolean generateAgainstLastStableBuild;

    @DataBoundConstructor
    public VirtualPipelinePublisher(List<VirtualPipelineInput> configurations, Boolean generatePicture, Boolean generateAgainstLastStableBuild) {
        this.configurations = configurations;
        this.generatePicture = generatePicture;
        this.generateAgainstLastStableBuild = generateAgainstLastStableBuild;
    }

    public void setGenerateAgainstLastStableBuild(Boolean generateAgainstLastStableBuild) {
        this.generateAgainstLastStableBuild = generateAgainstLastStableBuild;
    }

    public Boolean getGenerateAgainstLastStableBuild() {
        return generateAgainstLastStableBuild;
    }

    public Boolean getGeneratePicture() {
        return generatePicture;
    }

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
     * every event from plugin is performed at this function as it is the main extension point for the plugin
     *
     * @param build
     * @param launcher
     * @param listener
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        //here is the performance part, e.g. drawing, printing itself
        File rootDir = build.getProject().getRootDir();
        File currentBuildFolder = new File(rootDir.getPath() + File.separator + "builds" + File.separator + build.getNumber());
        File defaultLogs = new File(currentBuildFolder + File.separator + "log");

        //creates necessary directories
        boolean mkdirsResult = currentBuildFolder.mkdirs();
        if (!mkdirsResult) {
            listener.getLogger().println("VP: cache directories were not successfully created");
        }

        RandomAccessFile raf = new RandomAccessFile(defaultLogs, "r");

        //reading log file

        List<LineWithOffset> logLines = new ArrayList<>();
        long currentOffset = raf.getFilePointer();
        String line = raf.readLine();
        while (line != null) {
            logLines.add(new LineWithOffset(line, currentOffset));
            currentOffset = raf.getFilePointer();
            line = raf.readLine();
        }
        raf.close();
        //getting filtered lines
        List<VirtualPipelineLineOutput> filterOutput = VirtualPipelineFilter.filter(logLines, getConfigurations());


        //writing in JSON format into output.json
        File jsonCacheFile = new File(currentBuildFolder.getPath() + File.separator + "cache.json");

        boolean createFileResult = jsonCacheFile.createNewFile();
        if (!createFileResult) {
            listener.getLogger().println("VP: Json cacheFile was not created");
        }

        VirtualPipelineFilter.saveToJSON(filterOutput, jsonCacheFile);


        // creating picture
        if(generatePicture){
            int width = 1200;
            int height = 800;
            VirtualPipelinePictureMaker pm = new VirtualPipelinePictureMaker(width, height);
            BufferedImage image = pm.createPicture(filterOutput);
            File picturePath = new File(currentBuildFolder + File.separator + "archive" + File.separator + "picture.png");
            picturePath.mkdirs();
            javax.imageio.ImageIO.write(image, "png", picturePath);
        }



        // adding actions to build in Jenkins
        VirtualPipelineProjectAction action = new VirtualPipelineProjectAction(build, this.getConfigurations(), jsonCacheFile);
        build.addAction(action);
        build.addAction(new VirtualPipelineHTMLAction(build, jsonCacheFile));
        build.addAction(new VirtualPipelineOffsetAction(build));

        return true;
    }


    /**
     * mainly used for configuration and input checks
     */
    @Symbol("virtualPipeline")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "Add virtual pipeline";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }


}