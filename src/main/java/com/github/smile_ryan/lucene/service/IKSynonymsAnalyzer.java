package com.github.smile_ryan.lucene.service;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.apache.lucene.analysis.util.FilesystemResourceLoader;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKTokenizer;

public class IKSynonymsAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer token = new IKTokenizer(true);//开启智能切词
        Map paramsMap = new HashMap();
        paramsMap.put("luceneMatchVersion", Version.LUCENE_8_5_1.toString());
        paramsMap.put("synonyms", "synonyms.txt");
        paramsMap.put("expand", "true");
        SynonymFilterFactory factory = new SynonymFilterFactory(paramsMap);
        ClasspathResourceLoader loader = new ClasspathResourceLoader();
        try {
            factory.inform(loader);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new TokenStreamComponents(token, factory.create(token));
    }
}