# Written-test-questions-one
线上笔试题 CSV解析

# 解析店铺名称&匹配店铺名称
1.解析CSV中带Json的数据   
2.使用流/并行流 加速解析文件    
3.使用Set对 店铺去重    
4.使用 使用Map<String,Map<String, Map<String,String>>>对词典数据进行映射，最快三次匹配成功    
5.先匹配3个关键词都有的 再匹配2个关键词都有的 最后匹配只有1个的   见代码

# 修复：
1.一次性将文件读取到磁盘中   ：  使用流解析完一行就写入文件中
2.店铺解析不全      ：    使用正则循环匹配每一个data

打标签 大致逻辑：
![Image text](https://github.com/CCYYT/images/blob/main/Snipaste_2023-11-25_23-37-39.png)
