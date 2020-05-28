package com.github.smile_ryan.address.analyzer;

import com.github.smile_ryan.address.analyzer.common.lucene.SynonymsAnalyzer;
import com.hankcs.lucene.HanLPAnalyzer;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * 名称：TestRunner
 * 描述：TestRunner.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
public class TestRunner {


    @Test
    public void index() throws IOException {
        Directory directory = FSDirectory.open(Paths.get("/Users/ryan/tmp/lucene/test"));
        IndexWriterConfig iwc = new IndexWriterConfig(new HanLPAnalyzer());
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(directory, iwc);
        Document doc = new Document();
        doc.add(new TextField("Content", "北京", Store.YES));
        doc.add(new TextField("Content", "北京", Store.YES));
        writer.addDocument(doc);
        Document doc1 = new Document();
        doc1.add(new TextField("Content", "北京市", Store.YES));
        writer.addDocument(doc1);

        Document doc2 = new Document();
        doc2.add(new TextField("Content", "北京街道", Store.YES));
        writer.addDocument(doc2);

        Document doc3 = new Document();
        doc3.add(new TextField("Content", "北京欢迎你", Store.YES));
        writer.addDocument(doc3);
        writer.close();
    }

    @Test
    void search() throws Exception {
        String field = "Content";
        String queryStr = "北京市";
        Directory directory = FSDirectory.open(Paths.get("/Users/ryan/tmp/lucene/test"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser(field, new SynonymsAnalyzer());
        Query query = parser.parse(queryStr);
        TopDocs docs = searcher.search(query, 10);
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(doc.get(field) + "\t" + scoreDoc.score);
        }

        System.out.println(reader.totalTermFreq(new Term(field, queryStr)));

        reader.close();
    }

}
