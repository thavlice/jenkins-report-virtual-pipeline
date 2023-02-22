package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualPipelineFilter {

    public static List<VirtualPipelineOutput> filter(List<String> lines, List<VirtualPipelineFormInput> configs) throws IOException {
        List<VirtualPipelineOutput> result = new ArrayList<>();

        for (VirtualPipelineFormInput config :
                configs) {
            List<String> filtered = new ArrayList<>();
            List<String> notFiltered = new ArrayList<>();

            for (String line :
                    lines) {
                if(line.matches(config.getRegex())){
                    filtered.add(line);
                }
                else {
                    notFiltered.add(line);
                }
            }
            result.add(new VirtualPipelineOutput(config.getName(), config.getRegex(), filtered, notFiltered));
        }
        return result;
    }

    public static void saveToJSON(List<VirtualPipelineOutput> list, File file) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
