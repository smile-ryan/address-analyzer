package com.github.smile_ryan.address.analyzer.common.lucene;

import com.hankcs.hanlp.HanLP;
import com.hankcs.lucene.HanLPTokenizer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.apache.lucene.util.Version;

@Slf4j
public class SynonymsAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer token;
        SynonymFilterFactory factory;
        try {
            token = new HanLPTokenizer(HanLP.newSegment().enableOffset(true), null, false);
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("luceneMatchVersion", Version.LUCENE_8_5_1.toString());
            paramsMap.put("synonyms", "synonyms.txt");
            paramsMap.put("expand", "true");
            factory = new SynonymFilterFactory(paramsMap);
            ClasspathResourceLoader loader = new ClasspathResourceLoader();
            factory.inform(loader);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return new TokenStreamComponents(token, factory.create(token));
    }
}