import util.CSVUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;


public class demo {
    Map<String,Map<String, Map<String,String>>> tagMap;//标签映射表

    String resourcesPath="./resources";//资源路径

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
        List<List<String>> context2 = new ArrayList<>(){{
            add(Arrays.asList("task_id", "storeId","storeName"));
        }};

        //用了storeName去重
        Set<String> storeNameSet = new HashSet<>();

        //读取原始文件
        List<List<String>> context = CSVUtil.CSVToListStringByStream(Path.of(resourcesPath+"/sample.csv"));

        //解析文件
        List<String> line;
        String data,storeName;
        for (int i = 1; i < context.size(); i++) {
            line = context.get(i);
            data = line.get(4);
            storeName = CSVUtil.getValueFromJson(data, "storeName");
            if(storeNameSet.contains(storeName))continue;//如果这个storeName已经添加过了 就不添加了
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
        //将词库信息读取到内存
        List<List<String>> context = CSVUtil.CSVToListStringByStream(Path.of(resourcesPath+"/标签词库1026.csv"), Charset.forName("GBK"));//词库表是UTF-8编码，这里需要设置一下编码格式
        tagMap = new HashMap<>();

        //解析到映射表
        for (int i = 1; i < context.size(); i++) {
            CSVUtil.MapBuilderMapMapMap(tagMap,context.get(i),context.get(i).get(4));
        }

    }

    private void processThree() throws IOException {

        //加解析好的Sample数据加载到内存中
        List<List<String>> context = CSVUtil.CSVToListStringByStream(Path.of(resourcesPath+"/newSample.csv"));

        //添加tag表头
        context.get(0).add(3,"tag");

        //为sample数据匹配标签
        String storeName;
        for (int i = 1; i < context.size(); i++) {
            storeName = context.get(i).get(2);
            List<String> keys1 = matchKey(tagMap.keySet(), storeName);//获取符合关键字1的 keys
            if (keys1.isEmpty()) continue;//没有符合的关键字
            context.get(i).add(3,matchTag(keys1, storeName));//获取标签并添加标签
        }

        //写入磁盘
        CSVUtil.StringListToCSVByStream(context,Path.of(resourcesPath+"/result.csv"));

    }

    /**
    * @Description: 从keySet中查询所有匹配storeName 的key
    * @Param: [keySet, storeName]
    * @return： java.util.List<java.lang.String> keySet中所有符合条件的key
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
    * @Description: 匹配storeName的标签， 满足优先返回符合关键字多的标签  例：关键字1,关键字2,关键字3 优先与 关键字1,关键字2 优先与 关键字1
    * @Param: [keys1：关键字1的列表, storeName]
    * @return： java.lang.String 符合条件的标签
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
                    return tagMap.get(keys[0]).get(keys[1]).get(keys[2]);//三个关键字都匹配到了，直接返回
                }
            }
        }
        if(tagMap.get(keys[0]).get(keys[1])!=null) return tagMap.get(keys[0]).get(keys[1]).get(keys[2]);//前两个关键字都匹配到了
        return "";//如果关键字2没有匹配上,并且在tagMap中不存在,说明没有符合条件的标签；
    }
}
