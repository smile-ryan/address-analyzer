//package com.github.smile_ryan.lucene;
//
//import com.hankcs.hanlp.HanLP;
//import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
//import com.hankcs.hanlp.seg.common.Term;
//import com.hankcs.hanlp.tokenizer.NLPTokenizer;
//import com.hankcs.hanlp.tokenizer.StandardTokenizer;
//import java.util.List;
//import org.junit.jupiter.api.Test;
//
///**
// * <pre>
// * 名称：TestRunner
// * 描述：TestRunner.java
// * </pre>
// *
// * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
// * @since v1.0.0
// */
//public class TestRunner {
//
//    @Test
//    void test1() {
//        System.out.println(NLPTokenizer.analyze("商品和服务").translateLabels());
//        System.out.println(NLPTokenizer.analyze("汽水不如果汁好喝").translateLabels());
//        System.out.println(NLPTokenizer.analyze("小白痴痴地在门前等小黑回来").translateLabels());
//        System.out.println(NLPTokenizer.analyze("我的希望是希望张晚霞的背影被晚霞映红"));
//
//        System.out.println(HanLP.segment("北京海淀区上地七街"));
//
//
//    }
//
//    @Test
//    void test2() {
//        System.out.println(CoreSynonymDictionary.distance("北京市", "北京"));
//        System.out.println(CoreSynonymDictionary.similarity("北京市", "北京"));
//
//        System.out.println(CoreSynonymDictionary.distance("北京市", "上海市"));
//        System.out.println(CoreSynonymDictionary.similarity("北京市", "上海市"));
//
//    }
//
//}
