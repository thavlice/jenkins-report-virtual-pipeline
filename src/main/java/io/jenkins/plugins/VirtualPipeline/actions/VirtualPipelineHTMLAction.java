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

package io.jenkins.plugins.VirtualPipeline.actions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.console.ConsoleNote;
import hudson.model.Action;
import hudson.model.Run;
import io.jenkins.plugins.VirtualPipeline.model.LineType;
import io.jenkins.plugins.VirtualPipeline.model.VirtualPipelineLineOutput;
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

    private final Run<?,?> run;
    private final File cacheFile;

    public VirtualPipelineHTMLAction(Run<?, ?> run, File cacheFile) {
        this.run = run;
        this.cacheFile = cacheFile;
    }

    public List<String> getLogs() throws IOException {
        Reader reader = run.getLogReader();
        BufferedReader bufferedReader = new BufferedReader(reader);
        List<String> result = new ArrayList<>();
        String line = bufferedReader.readLine();
        while (line != null) {
            // clear user notes
            line = ConsoleNote.removeNotes(line);
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
        if (iteratorMarked.hasNext()) {
            currentMarkedLine = iteratorMarked.next();
        }

        for (int index = 0; index < fullLogs.size(); index++) {

            if ((index == currentMarkedLine.getIndex())) {
                result.add(currentMarkedLine); //matched line with full information
                if (iteratorMarked.hasNext()){ //for the last match
                    currentMarkedLine = iteratorMarked.next();
                }
            }else {
                //default for line with no marked meaning
                result.add(new VirtualPipelineLineOutput("", fullLogs.get(index), index, false,
                        LineType.DEFAULT, 0, false));
            }
        }
        return result;
    }

    private List<VirtualPipelineLineOutput> getAllCacheFromNamedFile(File file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(file, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return new ArrayList<>();
    }

    @Override
    public String getIconFileName() {
        return "symbol-logs";
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
