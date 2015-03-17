package com.abyss.wsdl.analyzer.support;

import com.abyss.wsdl.analyzer.core.BasicProcess;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by lblsloveryy on 15-2-10.
 */
public class TestWsdlAnalyzer {

    String base = "/Users/lblsloveryy/GitProject/WSDLAnalyzer/src/test/resources/wsdl2";
    String first = base + "/getJzgZpByZgh-root.xml";
    String second = base + "/ExtCommandService.xml";
    String third = base + "/regWebService-root.xml";
    String storeBase = "/Users/lblsloveryy/GitProject/WSDLAnalyzer/src/test/resources/temp/ServiceName";

    @Ignore("Temporary")
    @Test
    public void testStoreWsdlFiles() {

        long start=System.currentTimeMillis();

        BasicProcess bp = new BasicProcess(third, null);
        bp.Process();
        bp.writeProcessedWsdlToDisk(storeBase);

        long end=System.currentTimeMillis();
        System.out.println("总耗时为："+(end-start)+"毫秒");
        //总耗时为：209毫秒

    }

    @Ignore("Temporary")
    @Test
    public void testGetBaseTypeMap() {

        BasicProcess bp = new BasicProcess(first, null);
        bp.Process();
        Map<String,Integer> map =  bp.getBaseTypeMap();

        Set entrySet = map.entrySet();
        for(Object item: entrySet)
        {
            Map.Entry temp = (Map.Entry<String,Integer>)item;
            System.out.println("the key is " + temp.getKey() + ". the value is " + temp.getValue());
        }

    }

//    @Ignore("Temporary")
    @Test
    public void testAnalyseProcess() {

        long start=System.currentTimeMillis();

        BasicProcess bp = new BasicProcess(third, null);

        long init = System.currentTimeMillis();

        bp.Process();

        long analysis = System.currentTimeMillis();

//        bp.writeProcessedWsdlToDisk(storeBase);

        long end=System.currentTimeMillis();
        System.out.println("总耗时为："+(end-start)+"毫秒");
        System.out.println("初始化WSDL4J耗时为：" + (init-start) + "毫秒");
        System.out.println("处理耗时为：" + (analysis-init) + "毫秒");
        System.out.println("生成物理文件耗时为：" + (end-analysis) + "毫秒");

        //记录几次的处理时间。基本上最大头还是分析过程本身。
        /*
        1:
        总耗时为：205毫秒
        初始化WSDL4J耗时为：3毫秒
        处理耗时为：181毫秒
        生成物理文件耗时为：21毫秒

        2:
        总耗时为：208毫秒
        初始化WSDL4J耗时为：4毫秒
        处理耗时为：183毫秒
        生成物理文件耗时为：21毫秒

        3:
        总耗时为：216毫秒
        初始化WSDL4J耗时为：3毫秒
        处理耗时为：191毫秒
        生成物理文件耗时为：22毫秒
         */
    }
}
