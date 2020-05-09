package com.github.smile_ryan.lucene;


import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.lucene.IKAnalyzer;

@Component
public class LuceneIndexer {

    private final static String INDEX_DIR = "/Users/ryan/tmp/lucene";

    public static LuceneIndexer getInstance() {
        return SingletonHolder.instance;
    }

    public static void main(String[] args) {
        try {
            boolean r = LuceneIndexer.getInstance().createIndex(INDEX_DIR);
            if (r) {
                System.out.println("索引创建成功!");
            } else {
                System.out.println("索引创建失败!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean createIndex(String indexDir) throws IOException {
        //加点测试的静态数据
        String[] titles = {"标题1", "标题2", "标题3", "标题4", "标题5", "标题6"};
        String[] tcontents = {
            "汽水不如果汁好喝",
            "下雨天留客天天留我不留 ",
            "小白痴痴地等着小黑",
            "叔叔亲了我妈妈也亲了我 ",
            "结合成分子",
            "北京市海淀区上地七街"
        };

        long startTime = System.currentTimeMillis();//记录索引开始时间

        Analyzer analyzer = new IKAnalyzer(true);
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(directory, config);

        indexWriter.deleteAll();

        for (int i = 0; i < titles.length; i++) {
            Document doc = new Document();
            //添加字段
            doc.add(new StringField("id", String.valueOf(i + 1), Field.Store.YES)); //添加内容
            doc.add(new TextField("title", titles[i], Field.Store.YES)); //添加文件名，并把这个字段存到索引文件里
            doc.add(new TextField("tcontent", tcontents[i], Field.Store.YES)); //添加文件路径
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();
        System.out.println("共索引了" + indexWriter.getDocStats().numDocs + "个文件");
        indexWriter.close();
        System.out.println("创建索引所用时间：" + (System.currentTimeMillis() - startTime) + "毫秒");

        return true;
    }

    private static class SingletonHolder {

        private final static LuceneIndexer instance = new LuceneIndexer();
    }

}
