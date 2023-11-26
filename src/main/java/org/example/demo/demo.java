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
    Map<String, Map<String, Map<String, String>>> tagMap;//标签映射表

    String resourcesOriginPath;//资源的路径
    String resourcesResultPath;//存放结果的路径

    public demo() throws IOException {
        String resourcePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("src/main/resources/")).getPath().substring(1);
        this.resourcesOriginPath = resourcePath + "origin/";//资源路径
        this.resourcesResultPath = resourcePath + "result/";//资源结果路径
        if (!Files.exists(Path.of(this.resourcesResultPath)))
            Files.createDirectories(Path.of(this.resourcesResultPath));
    }

    public static void main(String[] args) throws IOException {

        demo demo = new demo();

        long l = System.currentTimeMillis();

        //解析sample文件
        demo.processOne();

        //将标签词库加载到内存中
        demo.processTwo();

        //根据标签词库 给解析后的sample文件打标签
        demo.processThree();

        System.out.println(System.currentTimeMillis()-l);

    }


    public void processOne() throws IOException {

        //storeName去重
        Set<String> storeNameSet = ConcurrentHashMap.newKeySet();

        //打开输入输出流
        Stream<String> stream = Files.lines(Path.of(resourcesOriginPath + "sample.csv")).skip(1);//跳过表头
        BufferedWriter writer = Files.newBufferedWriter(Path.of(resourcesResultPath + "newSample.csv"));

        //写入表头
        writer.write("\"task_id\",\"storeId\",\"storeName\"");writer.newLine();

        Pattern storeNamePattern = Pattern.compile("\"data\":\\{.*?\"storeName\":\"([^\"]+?)\"[,|}]");
        Pattern storeIdPattern = Pattern.compile("\"data\":\\{.*?\"storeId\":\"(\\w+?)\"[,|}]");

        stream
                .map(CSVUtil::stringLineToList)//解析行
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

        //关闭流
        writer.close();
    }

    public void processTwo() {
        //将词库信息读取到内存
        List<List<String>> context = CSVUtil.CSVToListStringByStream(Path.of(resourcesOriginPath + "标签词库1026.csv"), Charset.forName("GBK"));//词库表是UTF-8编码，这里需要设置一下编码格式
        tagMap = new HashMap<>();

        //解析到映射表
        for (int i = 1; i < context.size(); i++) {
            CSVUtil.MapBuilderMapMapMap(tagMap, context.get(i), context.get(i).get(4));
        }

    }

    public void processThree() throws IOException {

        Stream<Map<String, Map<String, Map<String, String>>>> tagMap1 = Stream.of(tagMap);


        Stream<String> stream = Files.lines(Path.of(resourcesResultPath + "newSample.csv")).skip(1);//跳过表头
        BufferedWriter writer = Files.newBufferedWriter(Path.of(resourcesResultPath + "result.csv"));

        //写入表头
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
            List<String> keys1 = matchKey(tagMap.keySet(), storeName);//获取符合关键字1的 keys
            if (keys1.isEmpty()) return;//没有符合的关键字
            writer.write(matchTag(keys1, storeName));
        }finally {
            writer.newLine();
            writer.flush();
        }
    }

    /**
     * @Description: 从keySet中查询所有匹配storeName 的key
     * @Param: [keySet, storeName]
     * @return： java.util.List<java.lang.String> keySet中所有符合条件的key
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
     * @Description: 匹配storeName的标签， 满足优先返回符合关键字多的标签  例：关键字1,关键字2,关键字3 优先与 关键字1,关键字2 优先与 关键字1
     * @Param: [keys1：关键字1的列表, storeName]
     * @return： java.lang.String 符合条件的标签
     */
    public String matchTag(List<String> keys1, String storeName) {
        String[] keys = {"", ""};
        for (String k1 : keys1) {
            List<String> keys2 = matchKey(tagMap.get(k1).keySet(), storeName);
            if (keys2.isEmpty()) continue;
            for (String k2 : keys2) {
                List<String> keys3 = matchKey(tagMap.get(k1).get(k2).keySet(), storeName);
                if (!keys3.isEmpty()) {
                    return tagMap.get(k1).get(k2).get(keys3.get(0));//三个关键字都匹配到了，直接返回
                }
                if(keys[1].isEmpty())keys[1] = k2;
            }
            if(keys[0].isEmpty())keys[0] = k1;
        }
        if(keys[0].isEmpty())keys[0] = keys1.get(0);//只符合一个关键字
        if (tagMap.get(keys[0]).get(keys[1]) != null) return tagMap.get(keys[0]).get(keys[1]).get("");//前两个关键字都匹配到了  或  匹配到第一个关键字
        return "";//如果关键字2没有匹配上,并且在tagMap中不存在,说明没有符合条件的标签；
    }

    public Map<String, Map<String, Map<String, String>>> getTagMap() {
        return tagMap;
    }
}
