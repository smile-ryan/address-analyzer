
/**
	* MyIndexer.java
	* V1.0
	* 2015-1-28-下午8:53:37
	* Copyright (c) 宜昌***有限公司-版权所有
	*/
package com.github.smile_ryan.lucene.service;

import java.io.File;
import java.io.IOException;

import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * 此类描述的是：
 * @author yax 2015-1-28 下午8:53:37
 * @version v1.0
 */
public class MyIndexer {

	public static void createIndex(String indexPath) throws IOException{
		Directory directory = FSDirectory.open(Paths.get(indexPath));
		Analyzer analyzer = new IKAnalyzer(true);

//		IKAnalyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		Document document1 = new Document();
		document1.add(new TextField("title", "thinkpad超极本笔记本中的战斗机", Store.YES));
		indexWriter.addDocument(document1);

		Document document2 = new Document();
		document2.add(new TextField("title", "俺is好人", Store.YES));
		indexWriter.addDocument(document2);

		Document document3 = new Document();
		document3.add(new TextField("title", "超极本超极本", Store.YES));
		indexWriter.addDocument(document3);

		Document document4 = new Document();
		document4.add(new TextField("title", "第一台计算机是美国军方定制，专门为了计算弹道和射击特性表面而研制的，承担开发任务的“莫尔小组”由四位科学家和工程师埃克特、莫克利、戈尔斯坦、博克斯组成。1946年这台计算机主要元器件采用的是电子管。该机使用了1500" +
												"个继电器，18800个电子管，占地170m2，重量重达30多吨，耗电150KW，造价48万美元。这台计算机每秒能完成5000次加法运算，400次乘法运算，比当时最快的计算工具快300倍，是继电器计算机的1000倍、手工计算的20万倍。" +
												"用今天的标准看，它是那样的“笨拙”和“低级”，其功能远不如一只掌上可编程计算器，但它使科学家们从复杂的计算中解脱出来，它的诞生标志着人类进入了一个崭新的信息革命时代。", Store.YES));
		indexWriter.addDocument(document4);
		indexWriter.close();
	}

	public static void main(String[] args) throws IOException {
		createIndex("/Users/ryan/tmp/lucene-synonyms");
	}

}