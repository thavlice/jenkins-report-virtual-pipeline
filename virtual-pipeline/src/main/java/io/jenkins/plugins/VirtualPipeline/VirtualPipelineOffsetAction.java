package io.jenkins.plugins.VirtualPipeline;
///////////////
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.RootAction;
import hudson.model.View;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.jelly.JellyContext;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.json.JsonHttpResponse;
import org.kohsuke.stapler.json.SubmittedForm;
import org.kohsuke.stapler.verb.GET;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Extension
public class VirtualPipelineOffsetAction implements SimpleBuildStep.LastBuildAction, RootAction{


    @GET
    @WebMethod(name = "get-search-offset")
    public void doSearchOffset(StaplerRequest req, StaplerResponse res) throws IOException {
        String from = req.getParameter("from");
        String to = req.getParameter("to");

        String someLine = "LOREM IPSUM";
        res.setContentType("text/html;charset=UTF-8");

        PrintWriter out = res.getWriter();

        out.println("<html>");
        out.println("<head></head>");
        out.println("<body>");
        out.println("<h1>Here are lines between from offset " + from + "to offset " + to+ " </h1>");
        out.println("<p>" + someLine + "</p>");
        out.println("</body>");
        out.println("</html>");
        out.flush();

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
