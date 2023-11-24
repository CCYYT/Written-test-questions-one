import util.CSVUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;


public class demo {
    Map<String,Map<String, Map<String,String>>> tagMap;//��ǩӳ���

    String resourcesPath="./resources";//��Դ·��

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
        List<List<String>> context2 = new ArrayList<>(){{
            add(Arrays.asList("task_id", "storeId","storeName"));
        }};

        //����storeNameȥ��
        Set<String> storeNameSet = new HashSet<>();

        //��ȡԭʼ�ļ�
        List<List<String>> context = CSVUtil.CSVToListStringByStream(Path.of(resourcesPath+"/sample.csv"));

        //�����ļ�
        List<String> line;
        String data,storeName;
        for (int i = 1; i < context.size(); i++) {
            line = context.get(i);
            data = line.get(4);
            storeName = CSVUtil.getValueFromJson(data, "storeName");
            if(storeNameSet.contains(storeName))continue;//������storeName�Ѿ���ӹ��� �Ͳ������
            context2.add(
                    Arrays.asList(
                            line.get(1),
                            CSVUtil.getValueFromJson(data, "storeId"),
                            storeName
                    )
                 );
            storeNameSet.add(storeName);
        }

        CSVUtil.StringListToCSVByStream(context2,Path.of(resourcesPath+"/newSample.csv"));

    }

    public void processTwo(){
        //���ʿ���Ϣ��ȡ���ڴ�
        List<List<String>> context = CSVUtil.CSVToListStringByStream(Path.of(resourcesPath+"/��ǩ�ʿ�1026.csv"), Charset.forName("GBK"));//�ʿ����UTF-8���룬������Ҫ����һ�±����ʽ
        tagMap = new HashMap<>();

        //������ӳ���
        for (int i = 1; i < context.size(); i++) {
            CSVUtil.MapBuilderMapMapMap(tagMap,context.get(i),context.get(i).get(4));
        }

    }

    private void processThree() throws IOException {

        //�ӽ����õ�Sample���ݼ��ص��ڴ���
        List<List<String>> context = CSVUtil.CSVToListStringByStream(Path.of(resourcesPath+"/newSample.csv"));

        //���tag��ͷ
        context.get(0).add(3,"tag");

        //Ϊsample����ƥ���ǩ
        String storeName;
        for (int i = 1; i < context.size(); i++) {
            storeName = context.get(i).get(2);
            List<String> keys1 = matchKey(tagMap.keySet(), storeName);//��ȡ���Ϲؼ���1�� keys
            if (keys1.isEmpty()) continue;//û�з��ϵĹؼ���
            context.get(i).add(3,matchTag(keys1, storeName));//��ȡ��ǩ����ӱ�ǩ
        }

        //д�����
        CSVUtil.StringListToCSVByStream(context,Path.of(resourcesPath+"/result.csv"));

    }

    /**
    * @Description: ��keySet�в�ѯ����ƥ��storeName ��key
    * @Param: [keySet, storeName]
    * @return�� java.util.List<java.lang.String> keySet�����з���������key
    */
    private List<String> matchKey(Set<String> keySet,String storeName){
        List<String> keys = new ArrayList<>();
        for (String s : keySet) {
            if (!s.isEmpty() && storeName.contains(s)) {
                keys.add(s);
            }
        }
        return keys;
    }



    /**
    * @Description: ƥ��storeName�ı�ǩ�� �������ȷ��ط��Ϲؼ��ֶ�ı�ǩ  �����ؼ���1,�ؼ���2,�ؼ���3 ������ �ؼ���1,�ؼ���2 ������ �ؼ���1
    * @Param: [keys1���ؼ���1���б�, storeName]
    * @return�� java.lang.String ���������ı�ǩ
    */
    private String matchTag(List<String> keys1,String storeName){
        String[] keys = {"","",""};
        for (String k1 : keys1) {
            keys[0] = k1;
            List<String> keys2 = matchKey(tagMap.get(keys[0]).keySet(), storeName);
            if (keys2.isEmpty()) continue;
            for (String k2 : keys2) {
                keys[1] = k2;
                List<String> keys3 = matchKey(tagMap.get(keys[0]).get(keys[1]).keySet(), storeName);
                if (!keys3.isEmpty()) {
                    return tagMap.get(keys[0]).get(keys[1]).get(keys[2]);//�����ؼ��ֶ�ƥ�䵽�ˣ�ֱ�ӷ���
                }
            }
        }
        if(tagMap.get(keys[0]).get(keys[1])!=null) return tagMap.get(keys[0]).get(keys[1]).get(keys[2]);//ǰ�����ؼ��ֶ�ƥ�䵽��
        return "";//����ؼ���2û��ƥ����,������tagMap�в�����,˵��û�з��������ı�ǩ��
    }
}
