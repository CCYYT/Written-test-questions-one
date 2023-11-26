package org.example.demo;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.util.*;

/**
 * @description: ������
 **/
public class DemoTest {
    static demo demo;

    public static void main(String[] args) throws IOException {
        demo = new demo();
        demo.processTwo();
        Map<String, Map<String, Map<String, String>>> tagMap = demo.getTagMap();

//        String tag = matchTagTest(storeName,tagMap);

        List<List<String>> l = new ArrayList<>();
        l.add(Arrays.asList("�����żҸ۱���֮��ĸӤ��Ʒ������꣩","�����żҸ۱���֮��ĸӤ��Ʒ�����"));
        l.add(Arrays.asList("aiqin����ĸӤ���������ׯ԰��","aiqin����ĸӤ����ݣ�����ׯ԰��)"));
        /*
        * ��Ӳ�������  ����ֵ,����ֵ
        * l.add(Arrays.asList("...,..."));
        * */

        for (List<String> testCase : l) {
            String storeName = testCase.get(0);
            String ex = testCase.get(1);
            String tag = matchTagTest(storeName,tagMap);
            if(!Objects.equals(ex,tag)) System.err.println("storeName:"+storeName+"  ||| return:\""+tag+"\" ||| \"��������\" ����->"+ex);
        }


    }

        /**
        * @Description: ����ƥ���ǩ
        * @Param: [storeName : ������, tagMap : ��ǩӳ���]
        * @return�� java.lang.String :ƥ�䵽�ı�ǩ δƥ��ɹ�����""
        * @Author: CCYT
        * @Date: 2023/11/26
        */
    public static String matchTagTest(String storeName,Map<String, Map<String, Map<String, String>>> tagMap) {
        List<String> keys1 = demo.matchKey(tagMap.keySet(), storeName);//��ȡ���Ϲؼ���1�� keys
        if (keys1.isEmpty())return "";//û�з��ϵĹؼ���
        String tag = demo.matchTag(keys1, storeName);
        return tag.isEmpty()?"":tag;
    }

    }
