package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class CSVUtil {

    public static List<List<String>> CSVToListString(BufferedReader reader) throws IOException {
        List<List<String>> lines = new ArrayList<>();
        String s;
        StringBuilder sb = new StringBuilder();
        int index=0;
        while ((s = reader.readLine())!=null){
            List<String> line = new ArrayList<>();
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
                    "来自 第"+lines.size()+"行\n"+
                    "当前行的内容:"+line+"\n"+
                    "上一行的内容:"+lines.get(lines.size()-1));
            line.add(sb.toString());
            sb.delete(0,sb.length());
            lines.add(line);
        }
        return lines;
    }

    public static void ListToCSV(List<String[]> context, BufferedWriter writer) throws IOException {
        for (String[] line : context) {
            StringJoiner s = new StringJoiner(",");
            for (String o : line) {
                s.add(o);
            }
//            System.out.println(s);
            writer.write(s.toString());
            writer.newLine();
        }
        writer.flush();
    }

    public static void StringListToCSV(List<List<String>> context, BufferedWriter writer) throws IOException {
        for (List<String> line : context) {
            StringJoiner s = new StringJoiner(",");
            for (String o : line) {
                s.add(o);
            }
//            System.out.println(s);
            writer.write(s.toString());
            writer.newLine();
        }
        writer.flush();
    }

    public static String getValueFromJson(String json,String key){
        int Index=json.indexOf(key);
        if (Index != -1){
            int start = json.indexOf(":", Index);
            int end = json.indexOf(",", Index);
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
