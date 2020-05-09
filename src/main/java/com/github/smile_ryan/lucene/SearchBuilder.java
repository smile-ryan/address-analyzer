package com.github.smile_ryan.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.lucene.IKTokenizer;

public class SearchBuilder {

    public static void doSearch(String indexDir, String field, String queryStr) throws IOException, ParseException, InvalidTokenOffsetsException {
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new IKAnalyzer();
        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(queryStr);

        long startTime = System.currentTimeMillis();
        TopDocs docs = searcher.search(query, 10);

        System.out.println("查找'" + queryStr + "'所用时间：" + (System.currentTimeMillis() - startTime));
        System.out.println("查询到" + docs.totalHits + "条记录");

        //加入高亮显示的
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>", "</font></b>");
        QueryScorer scorer = new QueryScorer(query);//计算查询结果最高的得分
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);//根据得分算出一个片段
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
        highlighter.setTextFragmenter(fragmenter);//设置显示高亮的片段

        //遍历查询结果
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            String id = doc.get("id");
            String title = doc.get("title");
            String tcontent = doc.get("tcontent");

            if (tcontent != null) {
                TokenStream tokenStream = analyzer.tokenStream("tcontent", new StringReader(tcontent));
                String summary = highlighter.getBestFragment(tokenStream, tcontent);
                System.out.println("id:" + id + ", title:" + title + ", tcontent:" + tcontent);
            }
        }
        reader.close();
    }

    public static void main(String[] args) {
        String indexDir = "/Users/ryan/tmp/lucene";
        String q = "北京市"; //查询这个字符串
        try {
            doSearch(indexDir, "tcontent", q);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
