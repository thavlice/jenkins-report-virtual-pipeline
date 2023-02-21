package io.jenkins.plugins.VirtualPipeline;

import org.json.simple.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class VirtualPipelineInputFilter {

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

    public static void saveToJSON(JSONArray array, File file){

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(array.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
