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

package io.jenkins.plugins.VirtualPipeline;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualPipelineFilter {

    public static List<VirtualPipelineLineOutput> filter(List<LineWithOffset> lines, List<VirtualPipelineInput> configs) {
        List<VirtualPipelineLineOutput> result = new ArrayList<>();

        if (configs.isEmpty()) {
            return result;
        }


        int lineIndex = 0;

        int activeRegexCount = 0;

        //advanced setup
        boolean advancedRegexLock = false;
        VirtualPipelineInputAdvanced activeConfig = null;


        //going through every line
        for (LineWithOffset lineWithOffset :
                lines) {
            String line = lineWithOffset.getLine();

            //check if advanced regex is currently active
            if (advancedRegexLock) {
                if (line.matches(activeConfig.getEndMark())) {
                    //end mark found
                    boolean display = activeConfig.getNumberOfLineToDisplay() == activeRegexCount + 1; // line 0 is start mark
                    if (activeConfig.getDeleteMark()) {
                        String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, activeConfig.getStartMark());
                        result.add(new VirtualPipelineLineOutput(activeConfig.getEndMark(), lineWithoutRegex, lineIndex, activeConfig.getDeleteMark(), LineType.END_MARK, lineWithOffset.getOffset(), display));
                    } else {
                        result.add(new VirtualPipelineLineOutput(activeConfig.getEndMark(), line, lineIndex, activeConfig.getDeleteMark(), LineType.END_MARK, lineWithOffset.getOffset(), display));
                    }

                    advancedRegexLock = false;

                    activeRegexCount = 0;
                    activeConfig = null;
                } else {
                    // filling content of an advanced regex

                    boolean display = activeConfig.getNumberOfLineToDisplay() == activeRegexCount + 1; // line 0 is start mark
                    if (activeRegexCount >= activeConfig.getMaxContentLength()) {
                        //end regex because of reaching the max limit
                        result.add(new VirtualPipelineLineOutput(activeConfig.getStartMark(), line, lineIndex, activeConfig.getDeleteMark(), LineType.LIMIT_REACHED_LINE, lineWithOffset.getOffset(), display));


                        advancedRegexLock = false;

                        activeConfig = null;

                        activeRegexCount = 0;
                    } else {
                        result.add(new VirtualPipelineLineOutput(activeConfig.getStartMark(), line, lineIndex, activeConfig.getDeleteMark(), LineType.CONTENT_LINE, lineWithOffset.getOffset(), display));
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

                    if (simpleConfig.getRegex().isEmpty()) { // empty regex would match all lines
                        continue;
                    }

                    if (line.matches(simpleConfig.getRegex())) {
                        //check to include mark or not
                        if (simpleConfig.getDeleteMark()) {
                            String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, simpleConfig.getRegex());
                            result.add(new VirtualPipelineLineOutput(simpleConfig.getRegex(), lineWithoutRegex, lineIndex, simpleConfig.getDeleteMark(), LineType.ONE_LINE, lineWithOffset.getOffset(), true));
                        } else {
                            result.add(new VirtualPipelineLineOutput(simpleConfig.getRegex(), line, lineIndex, simpleConfig.getDeleteMark(), LineType.ONE_LINE, lineWithOffset.getOffset(), true));
                        }
                        break;
                    }

                } else if (config instanceof VirtualPipelineInputAdvanced) {

                    // ADVANCED

                    VirtualPipelineInputAdvanced advancedConfig = (VirtualPipelineInputAdvanced) config;

                    if (advancedConfig.getStartMark().isEmpty() || advancedConfig.getEndMark().isEmpty()) { // empty regex would match all lines
                        continue;
                    }

                    if (line.matches(advancedConfig.getStartMark())) {
                        boolean display = advancedConfig.getNumberOfLineToDisplay() == 0;
                        if (advancedConfig.getDeleteMark()) {
                            String lineWithoutRegex = VirtualPipelineFilter.removeRegexMark(line, advancedConfig.getStartMark());
                            result.add(new VirtualPipelineLineOutput(advancedConfig.getStartMark(), lineWithoutRegex, lineIndex, advancedConfig.getDeleteMark(), LineType.START_MARK, lineWithOffset.getOffset(), display));
                        } else {
                            result.add(new VirtualPipelineLineOutput(advancedConfig.getStartMark(), line, lineIndex, advancedConfig.getDeleteMark(), LineType.START_MARK, lineWithOffset.getOffset(), display));
                        }
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

}
