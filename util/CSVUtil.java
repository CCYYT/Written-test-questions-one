package util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class CSVUtil {
    private static boolean isParallel=false;//全局的并行流控制  读取大文件时开启
    private static final Charset defaultEncoding=StandardCharsets.UTF_8;

    public static void setIsParallel(boolean isParallel) {
        CSVUtil.isParallel = isParallel;
    }

    public static List<List<String>> CSVToListStringByStream(Path path){
        return CSVToListStringByStream(path,isParallel);
    }

    public static List<List<String>> CSVToListStringByStream(Path path,boolean isParallel){
        return CSVToListStringByStream(path, defaultEncoding,isParallel);
    }

    public static List<List<String>> CSVToListStringByStream(Path path, Charset charset){
        return CSVToListStringByStream(path,charset,isParallel);
    }

    public static List<List<String>> CSVToListStringByStream(Path path, Charset charset,boolean isParallel){
        Stream<String> lines;
        try {
            lines = Files.lines(path,charset).skip(1);
            String firstLine = getFirstLine(path,charset);//获取表头
            if (isParallel) lines = lines.parallel();
            return CSVStreamToListString(firstLine,lines,isParallel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFirstLine(Path path,Charset charset) throws IOException {
        return Files.newBufferedReader(path,charset).readLine();
    }

    private static List<List<String>> CSVStreamToListString(String firstLine,Stream<String> lines,boolean isParallel){
        List<List<String>> context;
        if(isParallel){
            context = new CopyOnWriteArrayList<>(){{
                add(new CopyOnWriteArrayList<>(Arrays.asList(firstLine.split(","))));
            }};
        }else {
            context = new ArrayList<>(){{
                add(new ArrayList<>(Arrays.asList(firstLine.split(","))));
            }};
        }

        lines.forEach(s -> {
            StringBuilder sb = new StringBuilder();
            List<String> line = new CopyOnWriteArrayList<>();
            int index=0;
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '{' -> index++;
                    case '}' -> index--;
                    case ','-> {
                        if (index == 0) {
                            line.add(sb.toString());
                            sb.delete(0,sb.length());
                            continue;
                        }
                    }
                }
                sb.append(c);
            }
            if(index != 0) throw new RuntimeException("数据解析错误，Json数据不完整\n"+
                    "当前行的内容:"+line+"\n");
            line.add(sb.toString());
            sb.delete(0,sb.length());
            context.add(line);
        });
        return context;
    }

    public static void StringListToCSVByStream(List<List<String>> context,Path path) throws IOException{
        StringListToCSVByStream(context,path,isParallel);
    }

    public static void StringListToCSVByStream(List<List<String>> context,Path path,boolean isParallel) throws IOException {
        String s = String.join(",",context.get(0));//读取第一行表头
        List<String> l = new ArrayList<>(){{
            add(s);
        }};
        //是否以并行流来解析数据
        if (isParallel){
            l.addAll(
                    context.stream().skip(1).parallel().map(line -> String.join(",", line)).toList()
            );
        }else{
            l.addAll(
                    context.stream().skip(1).map(line -> String.join(",", line)).toList()
            );
        }
        Files.write(path, l);
    }

    public static String getValueFromJson(String json,String key){
        int index=json.indexOf(key);
        if (index != -1){
            int start = json.indexOf(":", index);
            int end = json.indexOf(",", index);
            return json.substring(start+1,end);
        }
        return null;
    }

    public static void MapBuilderMapMapMap(Map<String,Map<String,Map<String,String>>> map, List<String> keys, String tag){
        if(keys.size() <3)return;
        if (!map.containsKey(keys.get(0))) map.put(keys.get(0),new HashMap<>());
        MapBuilderMapMap(map.get(keys.get(0)),keys,tag);
    }
    private static void MapBuilderMapMap(Map<String,Map<String,String>> map,List<String> keys,String tag){
        if (!map.containsKey(keys.get(1)))map.put(keys.get(1),new HashMap<>());
        MapBuilderMap(map.get(keys.get(1)),keys,tag);
    }
    private static void MapBuilderMap(Map<String,String> map,List<String> keys,String tag){
            map.put(keys.get(2),tag);
    }


}
