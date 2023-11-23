import util.CSVUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class demo {
    CSVUtil csvUtil = new CSVUtil();
    Map<String,Map<String, Map<String,String>>> tagMap;

    String resourcesPath="./resources";

    public static void main(String[] args) throws IOException{

        demo demo = new demo();
        //解析sample文件
        demo.processOne();

        //将标签词库加载到内存中
        demo.processTwo();

        //根据标签词库 给解析后的sample文件打标签
        demo.processThree();
    }



    public void processOne() throws IOException {
        //        BufferedReader csvReader = new BufferedReader(new FileReader("./sample.csv"));
//        String s;
//        while ((s = csvReader.readLine()) != null){
//            System.out.println(s);
//        }
//        CSVReader csvReader = new CSVReader(new FileReader("./sample.csv", StandardCharsets.UTF_8));
        System.out.println("=============================");
//        CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream("./sample.csv"), Charset.forName("GBK")));

        List<String[]> context2 = new ArrayList<>(){{
            add(new String[]{"task_id", "storeId","storeName"});
        }};


//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("./sample.csv"), Charset.forName("GBK")));
//        BufferedReader reader = new BufferedReader(new FileReader("./sample.csv"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/sample.csv"), StandardCharsets.UTF_8));

        BufferedWriter writer = new BufferedWriter(new FileWriter(resourcesPath+"/newSample.csv"));
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./newSample1.csv"),"UTF-8"));

        List<List<String>> context = csvUtil.CSVToListString(reader);

        for (int i = 1; i < context.size(); i++) {
            List<String> line = context.get(i);
            String data = line.get(4);

            context2.add(
                    new String[]{
                            line.get(1),
                            csvUtil.getValueFromJson(data, "storeId"),
                            csvUtil.getValueFromJson(data, "storeName")
                    });
        }

        csvUtil.ListToCSV(context2,writer);

        reader.close();
        writer.close();
    }

    public void processTwo() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/标签词库1026.csv")));

        List<List<String>> context = csvUtil.CSVToListString(reader);

        tagMap = new HashMap<>();


        for (int i = 1; i < context.size(); i++) {
            csvUtil.MapBuilderMapMapMap(tagMap,context.get(i),context.get(i).get(4));
        }

//        tagMap.forEach(
//                (s, stringMapMap) ->
//                        stringMapMap.forEach(
//                                (s1, stringStringMap) ->
//                                        stringStringMap.forEach((s2, s3) ->
//                                                System.out.println(s+"  "+s1+"   "+s2+"   "+s3)
//                                        )
//                        )
//        );
        reader.close();
    }

    private void processThree() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/newSample.csv")));
        BufferedWriter writer = new BufferedWriter(new FileWriter(resourcesPath+"/result.csv"));
        List<List<String>> context = csvUtil.CSVToListString(reader);
        context.get(0).add(3,"tag");
        for (int i = 1; i < context.size(); i++) {
            String storeName = context.get(i).get(2);
            for (String key1 : tagMap.keySet()) {
                if (storeName.contains(key1)) {
                    for (String key2 : tagMap.get(key1).keySet()) {
                        if(key2.isEmpty()){
                            context.get(i).add(3,tagMap.get(key1).get("").get(""));
                            break;
                        }else if (storeName.contains(key2)) {
                            for (String key3 : tagMap.get(key1).get(key2).keySet()) {
                                if (storeName.contains(key3)) {
                                    context.get(i).add(3,tagMap.get(key1).get(key2).get(""));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        csvUtil.StringListToCSV(context,writer);
        reader.close();
        writer.close();
    }
}
