package io.jenkins.plugins.VirtualPipeline;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VirtualPipelineProjectAction  implements SimpleBuildStep.LastBuildAction {

    private AbstractBuild<?, ?> build;

    private List<VirtualPipelineFormInput> configurations;

    private File cacheFile;


    public VirtualPipelineProjectAction(AbstractBuild<?, ?> build, List<VirtualPipelineFormInput> configurations, File cacheFolder) {
        this.build = build;
        this.configurations = configurations;
        this.cacheFile = cacheFolder;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public  List<VirtualPipelineOutput> getAllFiltered(){
        List<VirtualPipelineOutput> result = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray array = (JSONArray) jsonParser.parse(new FileReader(this.getCacheFile().getPath()));

            for (Object obj :
                    array) {
                JSONObject jsonObj = (JSONObject) obj;
                // int number = (int) jsonObj.get("number");
                String name = (String) jsonObj.get("name");
                String regex = (String) jsonObj.get("regex");
                //JSONArray filteredArray = (JSONArray) jsonObj.get("filtered");
                /**List<String> filtered = new ArrayList<>();
                for (Object objLine :
                        filteredArray) {
                    JSONObject jsonObjLine = (JSONObject) objLine;
                    filtered.add(objLine.toString());
                }**/
                //List<String> notFiltered = (List<String>) jsonObj.get("notFiltered");
                VirtualPipelineOutput output = new VirtualPipelineOutput(name,  regex, new ArrayList<>(), new ArrayList<>());
                result.add(output);

            }
               return result;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public List<VirtualPipelineFormInput> getConfigurations() {
        return configurations;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    /**
     * used for displaying action in the sidebar, not needed
     * @return null because we don't use it
     */
    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        ArrayList<Action> list = new ArrayList<>();
        list.add(new VirtualPipelineProjectAction(this.getBuild(), this.getConfigurations(), this.getCacheFile()));
        return list;
    }
}
