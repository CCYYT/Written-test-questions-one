import util.CSVUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class demo {
    Map<String,Map<String, Map<String,String>>> tagMap;//��ǩӳ���

    String resourcesPath="./resources";

    public static void main(String[] args) throws IOException{

        demo demo = new demo();
        //����sample�ļ�
        demo.processOne();

        //����ǩ�ʿ���ص��ڴ���
        demo.processTwo();

        //���ݱ�ǩ�ʿ� ���������sample�ļ����ǩ
        demo.processThree();
    }



    public void processOne() throws IOException {
        //���ñ�ͷ
        List<String[]> context2 = new ArrayList<>(){{
            add(new String[]{"task_id", "storeId","storeName"});
        }};

        //�������������
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/sample.csv"), StandardCharsets.UTF_8));
        BufferedWriter writer = new BufferedWriter(new FileWriter(resourcesPath+"/newSample.csv"));

        //��ȡԭʼ�ļ�
        List<List<String>> context = CSVUtil.CSVToListString(reader);

        //�����ļ�
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

        //��������ɵ����ݱ��浽����
        CSVUtil.ListToCSV(context2,writer);

        //�ر���
        reader.close();
        writer.close();
    }

    public void processTwo() throws IOException {
        //����������
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/��ǩ�ʿ�1026.csv")));

        //���ʿ���Ϣ��ȡ���ڴ�
        List<List<String>> context = CSVUtil.CSVToListString(reader);
        tagMap = new HashMap<>();
        //������ӳ���
        for (int i = 1; i < context.size(); i++) {
            CSVUtil.MapBuilderMapMapMap(tagMap,context.get(i),context.get(i).get(4));
        }

        //�ر���
        reader.close();
    }

    private void processThree() throws IOException {
        //�������������
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resourcesPath+"/newSample.csv")));
        BufferedWriter writer = new BufferedWriter(new FileWriter(resourcesPath+"/result.csv"));

        //�ӽ����õ�Sample���ݼ��ص��ڴ���
        List<List<String>> context = CSVUtil.CSVToListString(reader);

        //���tag��ͷ
        context.get(0).add(3,"tag");

        //Ϊsample����ƥ���ǩ
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
        //д�����
        CSVUtil.StringListToCSV(context,writer);
        //�ر���
        reader.close();
        writer.close();
    }
}
