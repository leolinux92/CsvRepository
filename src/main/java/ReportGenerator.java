import org.json.JSONArray;
import org.json.JSONObject;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {


    public static void main(String[] args) throws IOException {

        if(args.length==0||args.length!=2){
            System.err.println("Specificare path di input e di output");
            return;
        }

        String inputPath=args[0];
        String outputPath=args[1];
        if((inputPath.isEmpty())||(outputPath.isEmpty())){
            System.err.println("Specificare path di input e di output");
            return;
        }
        final String nomeFileInput="historical-stocks-data.csv";
        final String nomeFileOutput="result.json";

        String fileSeparator = File.separator;
        if(!inputPath.endsWith(fileSeparator)){
            inputPath=inputPath+fileSeparator;
        }
        if(!outputPath.endsWith(fileSeparator)){
            outputPath=outputPath+fileSeparator;
        }
        String csvFile = inputPath+nomeFileInput;
        String jsonFilePath = outputPath+nomeFileOutput;

        if(!new File(csvFile).exists()){
            System.err.println("File historical-stocks-data.csv non trovato");
            return;
        }

        final long numLines = Files.lines(new File(csvFile).toPath()).count();


        Map<String, Map<String, Double>> endMap = organizeMap(csvFile);
        String json = jsonCreator(numLines, endMap);
        writeCsv(jsonFilePath, json);


    }


    private static Map<String, Map<String,Double>> organizeMap(String csvFile){
        Map<String, Map<String,Double>> endMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            Map<String, List<String[]>> valueMap =
                    br.lines().map(l->l.split(","))
                            .collect(
                                    Collectors.groupingBy(col->col[0],TreeMap::new, Collectors.toList())
                            );
            for(Map.Entry<String,List<String[]>> x: valueMap.entrySet()) {

                double avgOpen = 0;
                double avgClose = 0;
                double maxHigh = 0;
                double minLow = 0;
                List<Double> listMax = new ArrayList<>();
                List<Double> listMin = new ArrayList<>();

                Map<String, Double> inMap = new HashMap<>();

                for (String[] q : x.getValue()) {

                    for (int u = 1; u < q.length; u++) {
                        if (u == 1) {
                            avgOpen += Double.parseDouble(q[u]);
                        } else if (u == 2) {
                            avgClose += Double.parseDouble(q[u]);
                        } else if (u == 5) {
                            listMax.add(Double.parseDouble(q[u]));
                        } else if (u == 4) {
                            listMin.add(Double.parseDouble(q[u]));
                        }
                    }
                }

                avgOpen = avgOpen / x.getValue().size();
                avgClose = avgClose / x.getValue().size();
                maxHigh = Collections.max(listMax);
                minLow = Collections.min(listMin);
                inMap.put("mean_open", avgOpen);
                inMap.put("mean_close", avgClose);
                inMap.put("max_high", maxHigh);
                inMap.put("min_low", minLow);
                endMap.put(x.getKey(), inMap);

            }

        }catch(IOException e){
            e.printStackTrace();
        }
        return endMap;
    }


    private static String jsonCreator(long numLines, Map<String, Map<String,Double>> endMap){
        JSONObject json = new JSONObject();
        json.put("entry_number", numLines);
        JSONArray arrayEst = new JSONArray();
        for(Map.Entry<String,Map<String,Double>> j: endMap.entrySet()){
            JSONArray array = new JSONArray();
            JSONObject it = new JSONObject();
            it.put("symbol",j.getKey());
            array.put(it);
            for(Map.Entry<String,Double> f :j.getValue().entrySet()){
                JSONObject item = new JSONObject();
                item.put(f.getKey(), f.getValue());
                array.put(item);
            }

            arrayEst.put(array);

        }
        json.put("tickers", arrayEst);
        return json.toString(4);
    }

    private static void writeCsv(String jsonFilePath, String json){
        try(FileWriter fw = new FileWriter(new File(jsonFilePath))){
            fw.write(json);
            System.out.println("File csv generato: " + jsonFilePath);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
