import util.CSVUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class demo {
    Map<String,Map<String, Map<String,String>>> tagMap;//标签映射表

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
        //设置表头
        List<String[]> context2 = new ArrayList<>(){{
            add(new String[]{"task_id", "storeId","storeName"});
        }};

        //创建输入输出流
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/sample.csv"), StandardCharsets.UTF_8));
        BufferedWriter writer = new BufferedWriter(new FileWriter(resourcesPath+"/newSample.csv"));

        //读取原始文件
        List<List<String>> context = CSVUtil.CSVToListString(reader);

        //解析文件
        for (int i = 1; i < context.size(); i++) {
            List<String> line = context.get(i);
            String data = line.get(4);

            context2.add(
                    new String[]{
                            line.get(1),
                            CSVUtil.getValueFromJson(data, "storeId"),
                            CSVUtil.getValueFromJson(data, "storeName")
                    });
        }

        //将解析完成的数据保存到磁盘
        CSVUtil.ListToCSV(context2,writer);

        //关闭流
        reader.close();
        writer.close();
    }

    public void processTwo() throws IOException {
        //创建输入流
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/标签词库1026.csv")));

        //将词库信息读取到内存
        List<List<String>> context = CSVUtil.CSVToListString(reader);
        tagMap = new HashMap<>();
        //解析到映射表
        for (int i = 1; i < context.size(); i++) {
            CSVUtil.MapBuilderMapMapMap(tagMap,context.get(i),context.get(i).get(4));
        }

        //关闭流
        reader.close();
    }

    private void processThree() throws IOException {
        //创建输入输出流
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/newSample.csv")));
        BufferedWriter writer = new BufferedWriter(new FileWriter(resourcesPath+"/result.csv"));

        //加解析好的Sample数据加载到内存中
        List<List<String>> context = CSVUtil.CSVToListString(reader);

        //添加tag表头
        context.get(0).add(3,"tag");

        //为sample数据匹配标签
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
        //写入磁盘
        CSVUtil.StringListToCSV(context,writer);
        //关闭流
        reader.close();
        writer.close();
    }
}
