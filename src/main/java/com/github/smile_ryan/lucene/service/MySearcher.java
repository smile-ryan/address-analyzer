
/** 
	* MySearcher.java 
	* V1.0 
	* 2015-1-28-下午9:02:32 
	* Copyright (c) 宜昌**有限公司-版权所有 
	*/
package com.github.smile_ryan.lucene.service;
 
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/** 
 * 此类描述的是：
 * @author yax 2015-1-28 下午9:02:32 
 * @version v1.0 
 */
public class MySearcher {
	
	public static List<String> searchIndex(String keyword, String indexPath) throws IOException, ParseException{
		List<String> result = new ArrayList<>();
		IndexSearcher indexSearcher = null;
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		indexSearcher = new IndexSearcher(indexReader);
		Analyzer analyzer = new IKSynonymsAnalyzer();
		
		QueryParser queryParser = new QueryParser("title", analyzer);
		Query query = queryParser.parse(keyword);
		TopDocs td = indexSearcher.search(query, 10);
		for (int i = 0; i < td.totalHits.value; i++) {
			Document document = indexSearcher.doc(td.scoreDocs[i].doc);
			result.add(document.get("title"));
		}
		return result;
	}
 
}
