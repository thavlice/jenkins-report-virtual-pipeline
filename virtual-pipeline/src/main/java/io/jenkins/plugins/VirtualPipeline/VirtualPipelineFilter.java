package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.security.core.parameters.P;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualPipelineFilter {

    public static List<VirtualPipelineLineOutput> filter(List<String> lines, List<VirtualPipelineInput> configs) throws IOException {
        List<VirtualPipelineLineOutput> result = new ArrayList<>();
        int lineIndex = 0;
        VirtualPipelineInputAdvanced activeConfig = null;

        //TODO bytecount for offset goes here

        Boolean advancedRegexLock = false;
        //todo replace by constant
        int lockCounter = 100;
        //going through every line
        for (String line :
                lines) {
            //matching first config, first come, first served principal
            for (VirtualPipelineInput config :
                    configs) {


                //determining the type of
                if (config instanceof VirtualPipelineInputSimple){
                    VirtualPipelineInputSimple simpleConfig = (VirtualPipelineInputSimple) config;
                    if (line.matches("^" + simpleConfig.getRegex() + ".*")) {
                        //TODO check to include mark or not
                        if (simpleConfig.getDeleteMark()) {
                            String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, simpleConfig.getRegex());
                            result.add(new VirtualPipelineLineOutput(simpleConfig.getRegex(), lineWithoutRegex, lineIndex, simpleConfig.getDeleteMark()));
                        } else {
                            result.add(new VirtualPipelineLineOutput(simpleConfig.getRegex(), line, lineIndex, simpleConfig.getDeleteMark()));
                        }
                        break;
                    }
                }else if (config instanceof VirtualPipelineInputAdvanced){
                    continue;
                    /**
                    VirtualPipelineInputAdvanced advancedConfig = (VirtualPipelineInputAdvanced) config;
                    //TODO advanced filtering
                    if(line.matches("^" + advancedConfig.getStartMark() + ".*" )){
                        if(advancedConfig.getDeleteMark()){
                            String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, advancedConfig.getStartMark());
                            result.add(new VirtualPipelineLineOutput(advancedConfig.getStartMark(), lineWithoutRegex, lineIndex, advancedConfig.getDeleteMark()));
                        }else {
                            result.add(new VirtualPipelineLineOutput(advancedConfig.getStartMark(), line, lineIndex, advancedConfig.getDeleteMark()));
                        }
                        activeConfig = advancedConfig;
                        advancedRegexLock = true;
                        lockCounter = advancedConfig.getMaxContentLength();
                    }**/

                }else{
                    //ERROR
                    System.err.println("Unknown instance of config");
                    return new ArrayList<>();
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


    public static String removeRegexMark(String line, String regex) {
        return line.replaceAll(regex, "");
    }
}
