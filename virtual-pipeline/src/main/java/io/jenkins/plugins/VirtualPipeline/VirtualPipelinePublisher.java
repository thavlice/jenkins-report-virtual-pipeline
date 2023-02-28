package io.jenkins.plugins.VirtualPipeline;

import hudson.Extension;
import hudson.Launcher;
import hudson.console.AnnotatedLargeText;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * responsible for performing all actions of the plugin
 */
public class VirtualPipelinePublisher extends Recorder implements SimpleBuildStep {

    private List<VirtualPipelineFormInput> configurations;

    @DataBoundConstructor
    public VirtualPipelinePublisher(List<VirtualPipelineFormInput> configurations) {
        this.configurations = configurations;
    }

    public List<VirtualPipelineFormInput> getConfigurations() {
        return configurations;
    }


    public void setConfigurations(List<VirtualPipelineFormInput> configurations) {
        this.configurations = configurations;
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
        listener.getLogger().println("This is the output of Virtual Pipeline Plugin");

        File rootDir = build.getProject().getRootDir();
        File currentBuildFolder = new File(rootDir.getPath() + File.separator + "builds" + File.separator + build.getNumber());

        // check
        listener.getLogger().println("Path is :" + rootDir.getPath());
        listener.getLogger().println("Path 2 is :" + currentBuildFolder.getPath());

        //creates necessary directories
        currentBuildFolder.mkdirs();

        //getting filtered lines
        List<VirtualPipelineLineOutput> filterOutput = VirtualPipelineFilter.filter(build.getLog(10000), getConfigurations());
        listener.getLogger().println("LOGGGGGGGGGGGGGGGG");
        for (VirtualPipelineLineOutput fO :
                filterOutput) {
            listener.getLogger().println("OUTPUT: "+ fO.getLine() + "||"+ fO.getRegex()+ "||"+fO.getDeleteMark() +"||"+ fO.getIndex());
        }


        //writing in JSON format into output.json
        File jsonCacheFile = new File( currentBuildFolder.getPath() + File.separator + "cache.json");
        jsonCacheFile.createNewFile();

        VirtualPipelineFilter.saveToJSON(filterOutput, jsonCacheFile);


        // creating picture
        //VirtualPipelinePictureMaker pm = new VirtualPipelinePictureMaker(1200, 800);
        //BufferedImage image = pm.createPicture(filterOutput.get(0));


        //File picturePath = new File(currentBuildFolder + File.separator + "archive" + File.separator + "picture.png");
        //picturePath.mkdirs();
        //javax.imageio.ImageIO.write(image, "png", picturePath);


        // adding action to build in Jenkins
        VirtualPipelineProjectAction action =  new VirtualPipelineProjectAction(build, this.getConfigurations(), jsonCacheFile);
        List<VirtualPipelineLineOutput> allFiltered = action.getAllCacheFromFile();
        build.addAction(action);

        return true;
    }

    /**
     * mainly used for configuration and input checks
     */
    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public FormValidation doCheckName(@QueryParameter List<VirtualPipelineFormInput> configurations)
                throws IOException, ServletException {

            for (VirtualPipelineFormInput s :
                    configurations) {
                if (s.getRegex().length() == 0) {
                    return FormValidation.error("Entry Regex is empty");
                }
            }
            return FormValidation.ok();
        }

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