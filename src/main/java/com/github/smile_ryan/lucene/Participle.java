package com.github.smile_ryan.lucene;

import com.github.smile_ryan.lucene.service.IKSynonymsAnalyzer;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Participle {

    public static String getParticiple(String str) {
        String result = "";
        if (str == null) {
            return result;
        }
        try {
            Analyzer analyzer = new IKAnalyzer(true);
            StringReader reader = new StringReader(str);
            TokenStream ts = analyzer.tokenStream("", reader);
            CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
            StringBuffer sb = new StringBuffer();
            while (ts.incrementToken()) {
                sb.append(term.toString() + " ");
            }
            result = sb.toString();
            reader.close();
            // System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void displayTokenInfo(String text) {
        Analyzer analyzer = new IKSynonymsAnalyzer();
        //获取词汇流
        TokenStream stream = analyzer.tokenStream("", new StringReader(text));
        //查看词汇属性
        CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
        //位置偏移量属性
        PositionIncrementAttribute pia = stream.addAttribute(PositionIncrementAttribute.class);
        //偏移量属性
        OffsetAttribute oa = stream.addAttribute(OffsetAttribute.class);
        //查看分词类型属性
        TypeAttribute ta = stream.addAttribute(TypeAttribute.class);
        try {
            //重置下streamToken对象
            stream.reset();
            //判断是否还有下一个token
            while (stream.incrementToken()) {
                System.out.println("TypeAttribute:" + ta + " | PositionIncrementAttribute:" + pia.getPositionIncrement()
                    + " | OffsetAttribute:[" + oa.startOffset() + "-" + oa.endOffset() + "] | CharTermAttribute:" + cta
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------------");
    }

    public static void main(String[] args) {
//        Participle.displayTokenInfo("汽水不如果汁好喝");
//        Participle.displayTokenInfo("下雨天留客天天留我不留");
//        Participle.displayTokenInfo("小白痴痴地等着小黑");
//        Participle.displayTokenInfo("叔叔亲了我妈妈也亲了我");
//        Participle.displayTokenInfo("结合成分子");
//        Participle.displayTokenInfo("北京市海淀区上地七街");
//        Participle.displayTokenInfo("我的希望是希望张晚霞的背影被晚霞映红");
//        Participle.displayTokenInfo("梦幻诛仙");
        Participle.displayTokenInfo("thinkpad超极本笔记本中的战斗机");
    }
}