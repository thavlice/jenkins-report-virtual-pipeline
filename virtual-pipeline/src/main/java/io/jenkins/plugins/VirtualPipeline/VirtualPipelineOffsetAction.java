package io.jenkins.plugins.VirtualPipeline;
///////////////
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.RootAction;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.json.JsonHttpResponse;
import org.kohsuke.stapler.json.SubmittedForm;
import org.kohsuke.stapler.verb.GET;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Extension
public class VirtualPipelineOffsetAction implements SimpleBuildStep.LastBuildAction, RootAction {


    @GET
    @WebMethod(name = "get-search-offset")
    public JsonHttpResponse doSearchOffset(@QueryParameter String from, @QueryParameter String to){
        JSONObject response = JSONObject.fromObject(new VirtualPipelineLineOutput(from, "huh", 42, true, LineType.CONTENT_LINE));
        return new JsonHttpResponse(response, 200);
    }
    @Override
    public Collection<? extends Action> getProjectActions() {
        ArrayList<Action> list = new ArrayList<>();
        return list;
    }

    @Override
    public String getIconFileName() {
        return "empty";
    }

    @Override
    public String getDisplayName() {
        return "Virtual Pipeline Offset Logs";
    }

    @Override
    public String getUrlName() {
        return "logsOffset";
    }
}
