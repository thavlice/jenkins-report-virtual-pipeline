package io.jenkins.plugins.VirtualPipeline;
///////////////

import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class VirtualPipelineOffsetAction implements SimpleBuildStep.LastBuildAction{
    private final AbstractBuild<?,?> build;

    public VirtualPipelineOffsetAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }


    @GET
    @WebMethod(name = "get-search-offset")
    public void doSearchOffset(StaplerRequest req, StaplerResponse res) throws IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        try {
            long from = Long.parseLong(req.getParameter("from"));
            long to = Long.parseLong(req.getParameter("to"));
        }catch (NumberFormatException e){
            res.setStatus(404);
            out.print("Couldn't parse input");
            return;
        }

        long from = Long.parseLong(req.getParameter("from"));
        long to = Long.parseLong(req.getParameter("to"));

        RandomAccessFile logs = new RandomAccessFile(this.build.getRootDir() + File.separator +  "log", "r");

        Long checkFrom = from;
        Long checkTo = to;

        List<String> resultLines = new ArrayList<>();

        if(from > to | from < 0 | to < 0 | checkFrom == null | checkTo == null){
            res.setStatus(404);
            out.print("Invalid parameters");
        }else {
            logs.seek(from);
            String line;
            // always prints the rest of the line of given offset
            while((line = logs.readLine()) != null){
                resultLines.add(line);

                //checking the upper limit
                if(logs.getFilePointer() >= to){
                    break;
                }
            }

            for (String resultLine :
                    resultLines) {
                out.println(resultLine);
            }

        }
        out.flush();
        logs.close();
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
