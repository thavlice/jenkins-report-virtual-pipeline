package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualPipelineFilter {

    public static List<VirtualPipelineLineOutput> filter(List<String> lines, List<VirtualPipelineInput> configs) throws IOException {
        List<VirtualPipelineLineOutput> result = new ArrayList<>();

        int lineIndex = 0;

        int activeRegexCount = 0;

        int currentOffset = 0;


        //TODO bytecount for offset goes here

        //advanced setup
        Boolean advancedRegexLock = false;
        VirtualPipelineInputAdvanced activeConfig = null;


        //going through every line
        for (String line :
                lines) {

            //check if advanced regex is currently active
            if (advancedRegexLock) {
                if (line.matches(VirtualPipelineFilter.wrapRegexMark(activeConfig.getEndMark()))) {
                    //end mark found
                    boolean display = activeConfig.getNumberOfLineToDisplay() == activeRegexCount + 1 ; // line 0 is start mark
                    if(activeConfig.getDeleteMark()){
                        String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, activeConfig.getStartMark());
                        result.add(new VirtualPipelineLineOutput(activeConfig.getEndMark(), lineWithoutRegex, lineIndex, activeConfig.getDeleteMark(), LineType.END_MARK, currentOffset, display));
                    } else {
                        result.add(new VirtualPipelineLineOutput(activeConfig.getEndMark(), line, lineIndex, activeConfig.getDeleteMark(), LineType.END_MARK, currentOffset, display));
                    }

                    advancedRegexLock = false;

                    //todo could cause errors
                    activeRegexCount = 0;
                    activeConfig = null;
                } else {
                    // filling content of an advanced regex

                    boolean display = activeConfig.getNumberOfLineToDisplay() == activeRegexCount + 1; // line 0 is start mark
                    if (activeRegexCount >=  activeConfig.getMaxContentLengthToInt()){
                        //end regex because of reaching the max limit
                        result.add(new VirtualPipelineLineOutput(activeConfig.getStartMark(), line, lineIndex, activeConfig.getDeleteMark(), LineType.LIMIT_REACHED_LINE, currentOffset, display));
                        currentOffset += line.length();

                        advancedRegexLock = false;

                        //todo could cause errors
                        activeConfig = null;

                        activeRegexCount = 0;
                    }else {
                        result.add(new VirtualPipelineLineOutput(activeConfig.getStartMark(), line, lineIndex, activeConfig.getDeleteMark(), LineType.CONTENT_LINE, currentOffset, display));
                        currentOffset += line.length();
                    }

                }
                activeRegexCount++;
                lineIndex++;
                continue; //skipping to next line
            }



            //matching first config, first come, first served principal
            for (VirtualPipelineInput config :
                    configs) {


                //determining the type of
                if (config instanceof VirtualPipelineInputSimple) {

                    // SIMPLE
                    VirtualPipelineInputSimple simpleConfig = (VirtualPipelineInputSimple) config;

                    if (line.matches(VirtualPipelineFilter.wrapRegexMark(simpleConfig.getRegex()))) {
                        //check to include mark or not
                        if (simpleConfig.getDeleteMark()) {
                            String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, simpleConfig.getRegex());
                            result.add(new VirtualPipelineLineOutput(simpleConfig.getRegex(), lineWithoutRegex, lineIndex, simpleConfig.getDeleteMark(), LineType.ONE_LINE, currentOffset, true));
                        } else {
                            result.add(new VirtualPipelineLineOutput(simpleConfig.getRegex(), line, lineIndex, simpleConfig.getDeleteMark(), LineType.ONE_LINE, currentOffset, true));
                        }
                        currentOffset += line.length();

                        break;
                    }

                } else if (config instanceof VirtualPipelineInputAdvanced) {

                    // ADVANCED

                    VirtualPipelineInputAdvanced advancedConfig = (VirtualPipelineInputAdvanced) config;

                    if (line.matches(VirtualPipelineFilter.wrapRegexMark(advancedConfig.getStartMark()))) {
                        boolean display = advancedConfig.getNumberOfLineToDisplay() == 0;
                        if (advancedConfig.getDeleteMark()) {
                            String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, advancedConfig.getStartMark());
                            result.add(new VirtualPipelineLineOutput(advancedConfig.getStartMark(), lineWithoutRegex, lineIndex, advancedConfig.getDeleteMark(), LineType.START_MARK, currentOffset, display));
                        } else {
                            result.add(new VirtualPipelineLineOutput(advancedConfig.getStartMark(), line, lineIndex, advancedConfig.getDeleteMark(), LineType.START_MARK, currentOffset,display));
                        }
                        currentOffset += line.length();
                        activeConfig = advancedConfig;
                        advancedRegexLock = true;
                        break;
                    }

                } else {
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

    public static String wrapRegexMark(String regex){
        // behaviour on start and end line can be modified here
        return "^" + regex + ".*";
    }
}
