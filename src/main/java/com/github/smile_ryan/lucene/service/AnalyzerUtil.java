/**
 * AnalyzerUtil.java V1.0 2015-1-28-下午8:42:24 Copyright (c) 宜昌**有限公司-版权所有
 */
package com.github.smile_ryan.lucene.service;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * 此类描述的是：
 * @author yax 2015-1-28 下午8:42:24 
 * @version v1.0
 */
public class AnalyzerUtil {

    public static void main(String[] args) {
        String indexPath = "/Users/ryan/tmp/lucene-synonyms";
        String input = "好心人";
        System.out.println("**********************");
        try {
//			MyIndexer.createIndex(indexPath);
            List<String> docs = MySearcher.searchIndex(input, indexPath);
            for (String string : docs) {
                System.out.println(string);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}