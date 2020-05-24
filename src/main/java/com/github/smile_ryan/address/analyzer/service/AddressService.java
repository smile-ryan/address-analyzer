package com.github.smile_ryan.address.analyzer.service;

import com.github.smile_ryan.address.analyzer.common.lucene.SynonymsAnalyzer;
import com.github.smile_ryan.address.analyzer.common.model.Address;
import com.github.smile_ryan.address.analyzer.common.model.Region;
import com.github.smile_ryan.address.analyzer.common.searcher.ResultVisitor;
import com.github.smile_ryan.address.analyzer.common.searcher.TreeNode;
import com.github.smile_ryan.address.analyzer.common.searcher.TreeNode.SearchNodeBuilder;
import com.github.smile_ryan.address.analyzer.common.searcher.SearchVisitor;
import com.github.smile_ryan.address.analyzer.common.util.AddressUtils;
import com.github.smile_ryan.address.analyzer.common.util.JSONUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * <pre>
 * 名称：AddressService
 * 描述：AddressService.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Slf4j
@Service
public class AddressService {

    @Autowired
    private SearchVisitor searchVisitor;

    @Autowired
    private ResultVisitor resultVisitor;

    @Autowired
    private LuceneService luceneService;

    @Value("classpath:address.txt")
    private Resource addressResource;

    public void deleteAllAddress() {
        luceneService.deleteAll();
    }

    public void optimizeAddress() {
        luceneService.optimize();
    }

    public void loadAddress() throws IOException {
        File file = addressResource.getFile();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while (StringUtils.isNotBlank(line = reader.readLine())) {
            List<String> addressSpits = Splitter.on(" ").omitEmptyStrings().splitToList(line);
            Document doc = new Document();
            String regionCode = addressSpits.get(0);
            String regionName = addressSpits.get(1);
            String parentCode = addressSpits.get(2);
            String regionPath = addressSpits.get(4);
            int regionLevel = Splitter.on(",").splitToList(regionPath).size();
            doc.add(new StringField("RegionCode", regionCode, Store.YES));
            doc.add(new TextField("RegionName", regionName, Store.YES));
            doc.add(new TextField("ShortName", AddressUtils.extractShortName(regionName), Store.YES));
            doc.add(new StringField("ParentCode", parentCode, Store.YES));
            doc.add(new IntPoint("RegionLevel", regionLevel));
            doc.add(new StoredField("RegionLevel", regionLevel));
            doc.add(new NumericDocValuesField("RegionLevel", regionLevel));
            doc.add(new StringField("RegionPath", regionPath, Store.YES));
            luceneService.addDocument(doc);
        }
    }

    public List<Address> analyze(String address) {
        List<String> tokenizeList = tokenize(address);
        TreeNode treeNode = new SearchNodeBuilder().tokenizeList(tokenizeList).build().accept(searchVisitor);
        return AddressUtils.processAddressList(treeNode.accept(resultVisitor))
            .stream().peek(this::fillAddress).collect(Collectors.toList());
    }

    public List<String> tokenize(String address) {
        address = address.replaceAll(AddressUtils.REGX_SYMBOL, "");
        List<String> tokenizeList = Lists.newLinkedList();
        List<com.hankcs.hanlp.seg.common.Term> termList = HanLP.segment(address);
        for (int i = 0; i < termList.size(); i++) {
            com.hankcs.hanlp.seg.common.Term term = termList.get(i);
            if (term.nature.startsWith("ns")) {
                tokenizeList.addAll(termList.subList(i, termList.size()).stream().map(t -> t.word).collect(Collectors.toList()));
                break;
            }
        }
        return tokenizeList.stream().filter(s ->
            !CollectionUtils.contains(AddressUtils.ADDRESS_UNITS.iterator(), s)
                && !CollectionUtils.contains(AddressUtils.ADDRESS_NOISE_PHRASES.iterator(), s)
                && !CollectionUtils.contains(AddressUtils.MINORITIES.iterator(), s)
        ).distinct().collect(Collectors.toList());
    }


    public List<Region> searchRegion(String regionName, Region parentRegion) {
        List<Region> regionList = Lists.newLinkedList();
        try {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            Query nameQuery = new QueryParser("RegionName", new SynonymsAnalyzer()).parse("RegionName:" + regionName + "^2.0 +ShortName:" + AddressUtils.extractShortName(regionName));
            builder.add(nameQuery, Occur.MUST);
            builder.add(IntPoint.newRangeQuery("RegionLevel", 1, 3), Occur.FILTER);
            if (parentRegion != null && StringUtils.isNotEmpty(parentRegion.getRegionCode())) {
                builder.add(new WildcardQuery(new Term("RegionPath", "*" + parentRegion.getRegionCode() + ",*")), Occur.FILTER);
            }
            regionList = luceneService.search(builder.build()).stream().map(pair -> AddressUtils.convertDocument(pair, true)).collect(Collectors.toList());
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return regionList;
    }

    public List<Region> searchStreet(String regionName, Region parentRegion) {
        List<Region> regionList = Lists.newLinkedList();
        try {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            Query nameQuery = new QueryParser("RegionName", new KeywordAnalyzer()).parse("RegionName:" + regionName + "^2.0");
            builder.add(nameQuery, Occur.SHOULD);

            Query shortNameQuery = new QueryParser("ShortName", new KeywordAnalyzer()).parse(AddressUtils.extractShortName(regionName));
            builder.add(shortNameQuery, Occur.MUST);

            Query levelQuery = IntPoint.newExactQuery("RegionLevel", 4);
            builder.add(levelQuery, Occur.FILTER);
            if (parentRegion != null && StringUtils.isNotEmpty(parentRegion.getRegionCode())) {
                WildcardQuery pathQuery = new WildcardQuery(new Term("RegionPath", "*" + parentRegion.getRegionCode() + ",*"));
                builder.add(pathQuery, Occur.FILTER);
            }
            regionList = luceneService.search(builder.build()).stream().map(pair -> AddressUtils.convertDocument(pair, true)).collect(Collectors.toList());
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return regionList;
    }

    public Pair<ScoreDoc, Document> searchByRegionCode(String regionCode) {
        Query query = new TermQuery(new Term("RegionCode", regionCode));
        List<Pair<ScoreDoc, Document>> list = luceneService.search(query);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }


    private void fillAddress(Address address) {
        Region lowest = address.getLowestLevelRegion();
        List<String> pathList = Splitter.on(",").omitEmptyStrings().splitToList(lowest.getRegionPath());
        if (address.getProvince() == null && pathList.size() >= 1) {
            String provinceCode = pathList.get(0);
            Region region = AddressUtils.convertDocument(searchByRegionCode(provinceCode), false);
            address.setRegion(region);
        }
        if (address.getCity() == null && pathList.size() >= 2) {
            String cityCode = pathList.get(1);
            Region region = AddressUtils.convertDocument(searchByRegionCode(cityCode), false);
            address.setRegion(region);
        }
        if (address.getDistrict() == null && pathList.size() >= 3) {
            String districtCode = pathList.get(2);
            Region region = AddressUtils.convertDocument(searchByRegionCode(districtCode), false);
            address.setRegion(region);
        }
    }


}
