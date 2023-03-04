package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualPipelineFilter {

    public static List<VirtualPipelineLineOutput> filter(List<String> lines, List<VirtualPipelineFormInput> configs) throws IOException {
        List<VirtualPipelineLineOutput> result = new ArrayList<>();
        int lineIndex = 0;

        //TODO bytecount for offset goes here

        for (String line :
                lines) {

            for (VirtualPipelineFormInput config:
                    configs) {
                if(line.matches("^"+ config.getRegex() + ".*")){
                    //TODO check to include mark or not
                    if(config.getDeleteMark()){
                        String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, config.getRegex());
                        result.add(new VirtualPipelineLineOutput(config.getRegex(), lineWithoutRegex, lineIndex, config.getDeleteMark()));
                    }else {
                        result.add(new VirtualPipelineLineOutput(config.getRegex(), line, lineIndex, config.getDeleteMark()));
                    }

                    break;
                }
            }
            lineIndex++;
        }
        return result;
    }

    public static void saveToJSON(List<VirtualPipelineLineOutput> list, File file) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String removeRegexMark(String line, String regex){
        return  line.replaceAll(regex, "");
    }
}
