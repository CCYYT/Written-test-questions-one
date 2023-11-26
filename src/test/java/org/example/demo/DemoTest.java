package org.example.demo;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.util.*;

/**
 * @description: 测试类
 **/
public class DemoTest {
    static demo demo;

    public static void main(String[] args) throws IOException {
        demo = new demo();
        demo.processTwo();
        Map<String, Map<String, Map<String, String>>> tagMap = demo.getTagMap();

//        String tag = matchTagTest(storeName,tagMap);

        List<List<String>> l = new ArrayList<>();
        l.add(Arrays.asList("苏州张家港宝贝之家母婴用品（澳洋店）","苏州张家港宝贝之家母婴用品澳洋店"));
        l.add(Arrays.asList("aiqin爱亲母婴生活馆御景庄园店","aiqin爱亲母婴生活馆（御景庄园店)"));
        /*
        * 添加测试数据  输入值,期望值
        * l.add(Arrays.asList("...,..."));
        * */

        for (List<String> testCase : l) {
            String storeName = testCase.get(0);
            String ex = testCase.get(1);
            String tag = matchTagTest(storeName,tagMap);
            if(!Objects.equals(ex,tag)) System.err.println("storeName:"+storeName+"  ||| return:\""+tag+"\" ||| \"不符期望\" 期望->"+ex);
        }


    }

        /**
        * @Description: 测试匹配标签
        * @Param: [storeName : 店铺名, tagMap : 标签映射表]
        * @return： java.lang.String :匹配到的标签 未匹配成功返回""
        * @Author: CCYT
        * @Date: 2023/11/26
        */
    public static String matchTagTest(String storeName,Map<String, Map<String, Map<String, String>>> tagMap) {
        List<String> keys1 = demo.matchKey(tagMap.keySet(), storeName);//获取符合关键字1的 keys
        if (keys1.isEmpty())return "";//没有符合的关键字
        String tag = demo.matchTag(keys1, storeName);
        return tag.isEmpty()?"":tag;
    }

    }
