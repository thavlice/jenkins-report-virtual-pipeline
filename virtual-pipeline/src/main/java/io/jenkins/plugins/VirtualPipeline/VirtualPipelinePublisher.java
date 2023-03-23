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
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


/**
 * responsible for performing all actions of the plugin
 */
public class VirtualPipelinePublisher extends Recorder implements SimpleBuildStep {

    private List<VirtualPipelineInput> configurations;
    private Boolean generatePicture;

    @DataBoundConstructor
    public VirtualPipelinePublisher(List<VirtualPipelineInput> configurations, Boolean generatePicture) {
        this.configurations = configurations;
        this.generatePicture = generatePicture;
    }

    public VirtualPipelineInput getFormat() {
        return new VirtualPipelineInputSimple("some", true);
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
     * every event from plugin is performed at this function as it is the main extention point for the plugin
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

        //creates necessary directories
        boolean mkdirsResult = currentBuildFolder.mkdirs();
        if (!mkdirsResult){
            listener.getLogger().println("VP: cache directories were not succesfully created");
        }


        //reading log file
        Reader reader = build.getLogReader();
        BufferedReader bufferedReader = new BufferedReader(reader);
        bufferedReader.readLine();

        List<String> logLines = new ArrayList<>();
        String line = bufferedReader.readLine();
        while (line != null) {
            logLines.add(line);
            line = bufferedReader.readLine();
        }

        bufferedReader.close();
        //getting filtered lines
        List<VirtualPipelineLineOutput> filterOutput = VirtualPipelineFilter.filter(logLines, getConfigurations());


        //writing in JSON format into output.json
        File jsonCacheFile = new File(currentBuildFolder.getPath() + File.separator + "cache.json");

        boolean createFileResult = jsonCacheFile.createNewFile();
        if (!createFileResult){
            listener.getLogger().println("VP: Json cacheFile was not created");
        }

        VirtualPipelineFilter.saveToJSON(filterOutput, jsonCacheFile);


        // creating picture
        //VirtualPipelinePictureMaker pm = new VirtualPipelinePictureMaker(1200, 800);
        //BufferedImage image = pm.createPicture(filterOutput.get(0));


        //File picturePath = new File(currentBuildFolder + File.separator + "archive" + File.separator + "picture.png");
        //picturePath.mkdirs();
        //javax.imageio.ImageIO.write(image, "png", picturePath);


        // adding actions to build in Jenkins
        VirtualPipelineProjectAction action = new VirtualPipelineProjectAction(build, this.getConfigurations(), jsonCacheFile);
        build.addAction(action);
        build.addAction(new VirtualPipelineHTMLAction(build));
        build.addAction(new VirtualPipelineOffsetAction());

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