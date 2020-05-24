package com.github.smile_ryan.address.analyzer.service;

import com.google.common.collect.Lists;
import com.hankcs.lucene.HanLPAnalyzer;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.wltea.analyzer.lucene.IKAnalyzer;

@Slf4j
@Service
public class LuceneService {

    private IndexWriter indexWriter;
    private ReferenceManager<IndexSearcher> indexSearcherManager;
    private ControlledRealTimeReopenThread<IndexSearcher> indexReopenThread;
    private ReentrantLock lock = new ReentrantLock();
    private long lastGeneration;
    private AtomicLong commitCount;
    @Value("${indexDirectory:/Users/ryan/tmp/lucene/address}")
    private String indexDirectoryPath;
    @Value("${indexReopenMaxStaleSec:10}")
    private double maxStaleSec;
    @Value("${indexReopenMinStaleSec:0.025}")
    private double minStaleSec;
    @Value("${maxCommitCount: 100}")
    private long maxCommitCount;

    @PostConstruct
    public void init() throws Exception {
        commitCount = new AtomicLong(0);
        Directory directory = MMapDirectory.open(Paths.get(indexDirectoryPath));
        indexWriter = new IndexWriter(directory, new IndexWriterConfig(new HanLPAnalyzer()));
        indexSearcherManager = new SearcherManager(indexWriter, true, true, null);
        indexReopenThread = new ControlledRealTimeReopenThread<>(indexWriter, indexSearcherManager, maxStaleSec, minStaleSec);
        indexReopenThread.start();
    }

    @PreDestroy
    public void destroy() throws Exception {
        indexReopenThread.interrupt();
        indexReopenThread.close();

        indexWriter.commit();
        indexWriter.close();
    }


    public void commit() {
        long cc = commitCount.incrementAndGet();
        if (cc < maxCommitCount) {
            return;
        }
        if (lock.tryLock()) {
            try {
                indexWriter.commit();
                commitCount.set(commitCount.get() - cc >= 0 ? commitCount.get() - cc : 0);
                log.debug("Committed to Lucene index.");
            } catch (IOException e) {
                log.error("Error in Lucene index commit work. {}", e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }
    }

    public void addDocument(final Document document) {
        try {
            lastGeneration = indexWriter.addDocument(document);
            log.debug("Added document in Lucene index.");
        } catch (IOException e) {
            log.error("Error in addition work. {}", e.getMessage(), e);
        } finally {
            commit();
        }
    }

    public void updateDocument(final Term term, final Document document) {
        try {
            lastGeneration = indexWriter.updateDocument(term, document);
            log.debug("Updated document in Lucene index.");
        } catch (IOException e) {
            log.error("Error in update work. {}", e.getMessage(), e);
        } finally {
            commit();
        }
    }

    public void deleteDocuments(final Term... term) {
        try {
            lastGeneration = indexWriter.deleteDocuments(term);
            log.debug("Deleted document in Lucene index.");
        } catch (IOException e) {
            log.error("Error in deletion work. {}", e.getMessage(), e);
        } finally {
            commit();
        }
    }

    public void deleteDocuments(final Query... query) {
        try {
            lastGeneration = indexWriter.deleteDocuments(query);
            log.debug("Deleted document in Lucene index by query.");
        } catch (IOException e) {
            log.error("Error in deletion work. {}", e.getMessage(), e);
        } finally {
            commit();
        }
    }

    public void deleteAll() {
        try {
            lastGeneration = indexWriter.deleteAll();
            log.debug("Deleted document in Lucene index.");
        } catch (IOException e) {
            log.error("Error in deletion work. {}", e.getMessage(), e);
        } finally {
            commit();
        }
    }

    private IndexSearcher acquireSearcher() {
        IndexSearcher searcher = null;
        try {
            indexReopenThread.waitForGeneration(lastGeneration);
            searcher = indexSearcherManager.acquire();
        } catch (InterruptedException e) {
            log.error("Index Reopen Thread is interrupted.");
        } catch (IOException e) {
            log.error("Error in acquire index searcher.");
        }

        return searcher;
    }

    private void releaseSearcher(final IndexSearcher searcher) {
        try {
            indexSearcherManager.release(searcher);
        } catch (IOException e) {
            log.error("Error in release index searcher.");
        }
    }


    public List<Pair<ScoreDoc, Document>> search(final Query query) {
        return search(query, null, 20);
    }

    public List<Pair<ScoreDoc, Document>> search(final Query query, Set<SortField> sortFields) {
        return search(query, sortFields, 20);
    }

    public List<Pair<ScoreDoc, Document>> search(final Query query, Set<SortField> sortFields, int numberOfResults) {
        IndexSearcher searcher = acquireSearcher();
        List<Pair<ScoreDoc, Document>> results = Lists.newLinkedList();
        TopDocs scoredDocs;
        try {
            if (CollectionUtils.isEmpty(sortFields)) {
                scoredDocs = searcher.search(query, numberOfResults);
            } else {
                Sort sort = new Sort(sortFields.toArray(new SortField[0]));
                scoredDocs = searcher.search(query, numberOfResults, sort);
            }
            for (ScoreDoc scoreDoc : scoredDocs.scoreDocs) {
                Document document = searcher.doc(scoreDoc.doc);
                results.add(Pair.of(scoreDoc, document));
            }
        } catch (IOException e) {
            log.error("Error in Search Operation");
        }

        releaseSearcher(searcher);
        return results;
    }

    public List<String> tokenize(String text) {
        Analyzer analyzer = new IKAnalyzer(true);
        TokenStream stream = analyzer.tokenStream("", new StringReader(text));
        CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
        List<String> tokens = Lists.newLinkedList();
        try {
            stream.reset();
            while (stream.incrementToken()) {
                tokens.add(cta.toString());
            }
        } catch (IOException e) {
           log.error(e.getMessage(), e);
        }
        return tokens;
    }

    public void optimize() {
        try {
            indexWriter.forceMerge(1);
        } catch (IOException e) {
            log.error("Error while optimizing index.");
        }
    }

}