package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class VirtualPipelineHTMLAction implements SimpleBuildStep.LastBuildAction {

    private final AbstractBuild<?, ?> build;
    private final File cacheFile;

    public VirtualPipelineHTMLAction(AbstractBuild<?, ?> build, File cacheFile) {
        this.build = build;
        this.cacheFile = cacheFile;
    }

    public List<String> getLogs() throws IOException {
        Reader reader = build.getLogReader();
        BufferedReader bufferedReader = new BufferedReader(reader);
        List<String> result = new ArrayList<>();
        String line = bufferedReader.readLine();
        while (line != null) {
            result.add(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return result;
    }
    
    public List<VirtualPipelineLineOutput> getMarkedLogs() throws IOException {
        List<String> fullLogs = this.getLogs();
        List<VirtualPipelineLineOutput> markedLogs = this.getAllCacheFromNamedFile(this.cacheFile);
        
        List<VirtualPipelineLineOutput> result = new ArrayList<>();

        ListIterator<VirtualPipelineLineOutput> iteratorMarked = markedLogs.listIterator();
        VirtualPipelineLineOutput currentMarkedLine = null;
        if(iteratorMarked.hasNext()){
            currentMarkedLine = iteratorMarked.next();
        }
        
        for (int index = 0; index < fullLogs.size(); index++) {

            if(iteratorMarked.hasNext() && (index == currentMarkedLine.getIndex())){
                result.add(currentMarkedLine);
                currentMarkedLine = iteratorMarked.next();
            }else {
                //default for line with no marked meaning
                result.add(new VirtualPipelineLineOutput("", fullLogs.get(index), index, false,
                        LineType.DEFAULT,0 , false));
            }
        }
        return result;
    }

    private List<VirtualPipelineLineOutput> getAllCacheFromNamedFile(File file){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<VirtualPipelineLineOutput> result = objectMapper.readValue(file, new TypeReference<List<VirtualPipelineLineOutput>>() {
            });
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        return "Virtual Pipeline HTML Logs";
    }

    @Override
    public String getUrlName() {
        return "html";
    }
}
