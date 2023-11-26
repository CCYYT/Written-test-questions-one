package org.example.demo;

import org.example.demo.util.CSVUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class demo {
    Map<String, Map<String, Map<String, String>>> tagMap;//��ǩӳ���

    String resourcesOriginPath;//��Դ��·��
    String resourcesResultPath;//��Ž����·��

    public demo() throws IOException {
        String resourcePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("src/main/resources/")).getPath().substring(1);
        this.resourcesOriginPath = resourcePath + "origin/";//��Դ·��
        this.resourcesResultPath = resourcePath + "result/";//��Դ���·��
        if (!Files.exists(Path.of(this.resourcesResultPath)))
            Files.createDirectories(Path.of(this.resourcesResultPath));
    }

    public static void main(String[] args) throws IOException {

        demo demo = new demo();

        long l = System.currentTimeMillis();

        //����sample�ļ�
        demo.processOne();

        //����ǩ�ʿ���ص��ڴ���
        demo.processTwo();

        //���ݱ�ǩ�ʿ� ���������sample�ļ����ǩ
        demo.processThree();

        System.out.println(System.currentTimeMillis()-l);

    }


    public void processOne() throws IOException {

        //storeNameȥ��
        Set<String> storeNameSet = ConcurrentHashMap.newKeySet();

        //�����������
        Stream<String> stream = Files.lines(Path.of(resourcesOriginPath + "sample.csv")).skip(1);//������ͷ
        BufferedWriter writer = Files.newBufferedWriter(Path.of(resourcesResultPath + "newSample.csv"));

        //д���ͷ
        writer.write("\"task_id\",\"storeId\",\"storeName\"");writer.newLine();

        Pattern storeNamePattern = Pattern.compile("\"data\":\\{.*?\"storeName\":\"([^\"]+?)\"[,|}]");
        Pattern storeIdPattern = Pattern.compile("\"data\":\\{.*?\"storeId\":\"(\\w+?)\"[,|}]");

        stream
                .map(CSVUtil::stringLineToList)//������
                .forEach(line ->{
                    String data = line.get(4);
                    Matcher storeNameMatcher = storeNamePattern.matcher(data);
                    Matcher storeIdMatcher = storeIdPattern.matcher(data);
                    try {
                        while (storeNameMatcher.find() && storeIdMatcher.find()){
                            String storeName = storeNameMatcher.group(1);
                            if(storeNameSet.contains(storeName))continue;
                            storeNameSet.add(storeName);
                            writer.write(line.get(1)+","+storeIdMatcher.group(1)+","+storeNameMatcher.group(1));
                            writer.newLine();
                        }
                        writer.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        //�ر���
        writer.close();
    }

    public void processTwo() {
        //���ʿ���Ϣ��ȡ���ڴ�
        List<List<String>> context = CSVUtil.CSVToListStringByStream(Path.of(resourcesOriginPath + "��ǩ�ʿ�1026.csv"), Charset.forName("GBK"));//�ʿ����UTF-8���룬������Ҫ����һ�±����ʽ
        tagMap = new HashMap<>();

        //������ӳ���
        for (int i = 1; i < context.size(); i++) {
            CSVUtil.MapBuilderMapMapMap(tagMap, context.get(i), context.get(i).get(4));
        }

    }

    public void processThree() throws IOException {

        Stream<Map<String, Map<String, Map<String, String>>>> tagMap1 = Stream.of(tagMap);


        Stream<String> stream = Files.lines(Path.of(resourcesResultPath + "newSample.csv")).skip(1);//������ͷ
        BufferedWriter writer = Files.newBufferedWriter(Path.of(resourcesResultPath + "result.csv"));

        //д���ͷ
        writer.write("\"task_id\",\"storeId\",\"storeName\",\"tag\"");writer.newLine();

        stream.forEach(line ->{
            try {
                addTag(line,writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        writer.close();

    }

    public void addTag(String line,BufferedWriter writer) throws IOException {
        try {
            writer.write(line+",");
            String storeName = line.split(",")[2];
            List<String> keys1 = matchKey(tagMap.keySet(), storeName);//��ȡ���Ϲؼ���1�� keys
            if (keys1.isEmpty()) return;//û�з��ϵĹؼ���
            writer.write(matchTag(keys1, storeName));
        }finally {
            writer.newLine();
            writer.flush();
        }
    }

    /**
     * @Description: ��keySet�в�ѯ����ƥ��storeName ��key
     * @Param: [keySet, storeName]
     * @return�� java.util.List<java.lang.String> keySet�����з���������key
     */
    public List<String> matchKey(Set<String> keySet, String storeName) {
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
    public String matchTag(List<String> keys1, String storeName) {
        String[] keys = {"", ""};
        for (String k1 : keys1) {
            List<String> keys2 = matchKey(tagMap.get(k1).keySet(), storeName);
            if (keys2.isEmpty()) continue;
            for (String k2 : keys2) {
                List<String> keys3 = matchKey(tagMap.get(k1).get(k2).keySet(), storeName);
                if (!keys3.isEmpty()) {
                    return tagMap.get(k1).get(k2).get(keys3.get(0));//�����ؼ��ֶ�ƥ�䵽�ˣ�ֱ�ӷ���
                }
                if(keys[1].isEmpty())keys[1] = k2;
            }
            if(keys[0].isEmpty())keys[0] = k1;
        }
        if(keys[0].isEmpty())keys[0] = keys1.get(0);//ֻ����һ���ؼ���
        if (tagMap.get(keys[0]).get(keys[1]) != null) return tagMap.get(keys[0]).get(keys[1]).get("");//ǰ�����ؼ��ֶ�ƥ�䵽��  ��  ƥ�䵽��һ���ؼ���
        return "";//����ؼ���2û��ƥ����,������tagMap�в�����,˵��û�з��������ı�ǩ��
    }

    public Map<String, Map<String, Map<String, String>>> getTagMap() {
        return tagMap;
    }
}
