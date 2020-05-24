package com.github.smile_ryan.address.analyzer;//package com.github.smile_ryan.address.analyzer.service;
//
//import com.github.smile_ryan.address.analyzer.common.lucene.SynonymsAnalyzer;
//import com.github.smile_ryan.address.analyzer.common.model.Address;
//import com.github.smile_ryan.address.analyzer.common.model.Region;
//import com.github.smile_ryan.address.analyzer.common.util.BeanUtils;
//import com.google.common.base.Joiner;
//import com.google.common.base.Splitter;
//import com.google.common.collect.Lists;
//import com.google.gson.Gson;
//import com.hankcs.hanlp.HanLP;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Collectors;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.tuple.Pair;
//import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field.Store;
//import org.apache.lucene.document.IntPoint;
//import org.apache.lucene.document.NumericDocValuesField;
//import org.apache.lucene.document.StoredField;
//import org.apache.lucene.document.TextField;
//import org.apache.lucene.expressions.Expression;
//import org.apache.lucene.expressions.SimpleBindings;
//import org.apache.lucene.expressions.js.JavascriptCompiler;
//import org.apache.lucene.index.Term;
//import org.apache.lucene.queries.function.FunctionScoreQuery;
//import org.apache.lucene.queryparser.classic.ParseException;
//import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.BooleanClause.Occur;
//import org.apache.lucene.search.BooleanQuery;
//import org.apache.lucene.search.DoubleValuesSource;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.TermQuery;
//import org.apache.lucene.search.WildcardQuery;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//
///**
// * <pre>
// * 名称：AddressService
// * 描述：AddressService.java
// * </pre>
// *
// * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
// * @since v1.0.0
// */
//@Slf4j
//@Service
//public class AddressService {
//
//    private String SYMBOL_REGX = "[`~!@#$^&*()=|{}':;',\\\\[\\\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]";
//
//    @Value("classpath:address.txt")
//    private Resource addressResource;
//
//    @Autowired
//    private LuceneService luceneService;
//
//    public void deleteAllAddress() {
//        luceneService.deleteAll();
//    }
//
//    public void optimizeAddress() {
//        luceneService.optimize();
//    }
//
//    public void loadAddress() throws IOException {
//        File file = addressResource.getFile();
//        BufferedReader reader = new BufferedReader(new FileReader(file));
//        String line;
//        while (StringUtils.isNotBlank(line = reader.readLine())) {
//            List<String> addressSpits = Splitter.on(" ").omitEmptyStrings().splitToList(line);
//            Document doc = new Document();
//            String regionCode = addressSpits.get(0);
//            String regionName = addressSpits.get(1);
//            String parentCode = addressSpits.get(2);
//            int regionLevel = Integer.parseInt(addressSpits.get(3));
//            String regionPath = addressSpits.get(4);
//            doc.add(new TextField("RegionCode", regionCode, Store.YES));
//            doc.add(new TextField("RegionName", regionName, Store.YES));
//            doc.add(new TextField("ParentCode", parentCode, Store.YES));
//            doc.add(new IntPoint("RegionLevel", regionLevel));
//            doc.add(new StoredField("RegionLevel", regionLevel));
//            doc.add(new NumericDocValuesField("RegionLevel", regionLevel));
//            doc.add(new TextField("RegionPath", regionPath, Store.YES));
//            luceneService.addDocument(doc);
//        }
//    }
//
//    public Pair<ScoreDoc, Document> searchByRegionCode(String regionCode) {
//        Query query = new TermQuery(new Term("RegionCode", regionCode));
//        List<Pair<ScoreDoc, Document>> list = luceneService.search(query);
//        return CollectionUtils.isEmpty(list) ? null : list.get(0);
//    }
//
//    public List<Pair<ScoreDoc, Document>> searchByRegionName(String regionName) {
//        List<Pair<ScoreDoc, Document>> docs = Lists.newLinkedList();
//        try {
//            QueryParser queryParser = new QueryParser("RegionName", new SynonymsAnalyzer());
//            Query query = queryParser.parse(regionName);
//            SimpleBindings bindings = new SimpleBindings();
//            bindings.add("score", DoubleValuesSource.SCORES);
//            bindings.add("boost", DoubleValuesSource.fromIntField("RegionLevel"));
////            Expression expr = JavascriptCompiler.compile("score + 9 - 2 * boost");
//            Expression expr = JavascriptCompiler.compile("score");
//            FunctionScoreQuery funQuery = new FunctionScoreQuery(query, expr.getDoubleValuesSource(bindings));
//            docs = luceneService.search(funQuery);
//
//        } catch (ParseException | java.text.ParseException e) {
//            log.error(e.getMessage(), e);
//        }
//
//        return docs;
//    }
//
//    public List<Pair<ScoreDoc, Document>> searchByRegionName(String regionName, String parentCode, Integer parentLevel, boolean exactQuery) {
//        List<Pair<ScoreDoc, Document>> docs = Lists.newLinkedList();
//        if (StringUtils.isEmpty(parentCode) || parentLevel == null) {
//            return searchByRegionName(regionName);
//        }
//        try {
//            BooleanQuery.Builder builder = new BooleanQuery.Builder();
//
//            QueryParser parser1 = new QueryParser("RegionName", exactQuery ? new WhitespaceAnalyzer() : new SynonymsAnalyzer());
//            Query query1 = parser1.parse(regionName);
//            builder.add(query1, Occur.MUST);
//
//            WildcardQuery query2 = new WildcardQuery(new Term("RegionPath", "*" + parentCode + "*"));
//            builder.add(query2, Occur.MUST);
//
//            Query query3 = IntPoint.newRangeQuery("RegionLevel", Math.addExact(parentLevel, 1), 10);
//            SimpleBindings bindings = new SimpleBindings();
//            bindings.add("score", DoubleValuesSource.SCORES);
//            bindings.add("boost", DoubleValuesSource.fromIntField("RegionLevel"));
//            Expression expr = JavascriptCompiler.compile("score");
//            FunctionScoreQuery funQuery = new FunctionScoreQuery(query3, expr.getDoubleValuesSource(bindings));
//
//            builder.add(funQuery, Occur.MUST);
//
//            BooleanQuery query = builder.build();
//            docs = luceneService.search(query);
//        } catch (ParseException | java.text.ParseException e) {
//            log.error(e.getMessage(), e);
//        }
//        return docs;
//    }
//
//    public List<Address> analyzeAddress(String address) {
////        List<String> tokenizeList = luceneService.tokenize(address);
//        List<com.hankcs.hanlp.seg.common.Term> segment = HanLP.segment(address);
//        List<String> tokenizeList = segment.stream().map(input -> {
//            assert input != null;
//            return input.word;
//        }).filter(StringUtils::isNotBlank).collect(Collectors.toList());
//        System.out.println(new Gson().toJson(tokenizeList));
//        List<Address> addressList = Lists.newLinkedList();
//        recursion(0, tokenizeList, addressList, new Address(), new Region());
//        addressList = reduceAddresses(addressList);
//        System.out.println(addressList.get(0));
//        System.out.println("====================================================================================");
//        return addressList;
//    }
//
//    private List<Address> reduceAddresses(List<Address> addressList) {
//        addressList.sort(Comparator.comparing(Address::calculateRelevance));
//        Collections.reverse(addressList);
//
//        List<BigDecimal> relevanceList = addressList.stream().map(input -> {
//            assert input != null;
//            return input.getRelevance();
//        }).collect(Collectors.toList());
//
//        BigDecimal[] division = fourDivision(relevanceList);
//        if (division.length == 3) {
//            addressList = addressList.stream().filter(address -> address.getRelevance().compareTo(division[2]) >= 0).collect(Collectors.toList());
//        }
//        addressList.forEach(this::fillAddress);
//        return addressList.stream().distinct().collect(Collectors.toList());
//    }
//
//    private String recursion(int i, List<String> tokenizeList, List<Address> addressList, Address address, Region region) {
//        List<Pair<ScoreDoc, Document>> docs = null;
//        if (i < tokenizeList.size()) {
//            docs = searchByRegionName(tokenizeList.get(i), region.getRegionCode(), region.getRegionLevel(), region.getRegionLevel() != null && region.getRegionLevel() > 2);
//        }
//        if (CollectionUtils.isEmpty(docs)) {
//            return Joiner.on("").join(tokenizeList.subList(i, tokenizeList.size()));
//        }
//        i++;
//        for (Pair<ScoreDoc, Document> pair : docs) {
//            Region _region = convertDocument(pair, true);
//            Address _address = new Address();
//            BeanUtils.copyProperties(address, _address);
//            _address.setRegion(_region);
//            addressList.add(_address);
//            addressList.remove(address);
//            String detail = recursion(i, tokenizeList, addressList, _address, _region);
//            _address.setDetail(detail);
//        }
//        return null;
//    }
//
//    private void fillAddress(Address address) {
//        Region lowest;
//        if (address.getStreet() != null) {
//            lowest = address.getStreet();
//        } else if (address.getDistrict() != null) {
//            lowest = address.getDistrict();
//        } else if (address.getCity() != null) {
//            lowest = address.getCity();
//        } else {
//            lowest = address.getProvince();
//        }
//
//        List<String> pathList = Splitter.on(",").omitEmptyStrings().splitToList(lowest.getRegionPath());
//        if (address.getProvince() == null && pathList.size() >= 1) {
//            String provinceCode = pathList.get(0);
//            Region region = convertDocument(searchByRegionCode(provinceCode), false);
//            address.setRegion(region);
//        }
//        if (address.getCity() == null && pathList.size() >= 2) {
//            String cityCode = pathList.get(1);
//            Region region = convertDocument(searchByRegionCode(cityCode), false);
//            address.setRegion(region);
//        }
//        if (address.getDistrict() == null && pathList.size() >= 3) {
//            String districtCode = pathList.get(2);
//            Region region = convertDocument(searchByRegionCode(districtCode), false);
//            address.setRegion(region);
//        }
//    }
//
//    private Region convertDocument(Pair<ScoreDoc, Document> pair, boolean setRelevance) {
//        if (pair.getKey() == null || pair.getRegion() == null) {
//            return null;
//        }
//        Document doc = pair.getRegion();
//        Region region = new Region();
//        region.setRegionCode(doc.get("RegionCode"));
//        region.setRegionName(doc.get("RegionName"));
//        region.setRegionLevel(doc.get("RegionLevel") != null ? Integer.parseInt(doc.get("RegionLevel")) : null);
//        region.setParentCode(doc.get("ParentCode"));
//        region.setRegionPath(doc.get("RegionPath"));
//        if (setRelevance) {
//            region.setRelevance(BigDecimal.valueOf(pair.getKey().score));
//        }
//        return region;
//    }
//
//    private BigDecimal[] fourDivision(List<BigDecimal> param) {
//        if (param == null || param.size() < 4) {
//            return param == null ? new BigDecimal[]{} : param.toArray(new BigDecimal[0]);
//        }
//        BigDecimal[] bds = new BigDecimal[param.size()];
//        for (int i = 0; i < param.size(); i++) {
//            bds[i] = param.get(i);
//        }
//        int len = bds.length;
//        Arrays.sort(bds);
//        BigDecimal _q1;
//        BigDecimal _q2;
//        BigDecimal _q3;
//        int index;
//        if (len % 2 == 0) {
//            index = new BigDecimal(len).divide(new BigDecimal("4"), 2, BigDecimal.ROUND_HALF_UP).intValue();
//            _q1 = bds[index - 1].multiply(new BigDecimal("0.25")).add(bds[index].multiply(new BigDecimal("0.75")));
//            _q2 = bds[len / 2].add(bds[len / 2 - 1]).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
//            index = new BigDecimal(3 * (len + 1)).divide(new BigDecimal("4"), 2, BigDecimal.ROUND_HALF_UP).intValue();
//            _q3 = bds[index - 1].multiply(new BigDecimal("0.75")).add(bds[index].multiply(new BigDecimal("0.25")));
//        } else {
//            _q1 = bds[new BigDecimal(len).multiply(new BigDecimal("0.25")).intValue()];
//            _q2 = bds[new BigDecimal(len).multiply(new BigDecimal("0.5")).intValue()];
//            _q3 = bds[new BigDecimal(len).multiply(new BigDecimal("0.75")).intValue()];
//        }
//        BigDecimal q1 = _q1.setScale(2, BigDecimal.ROUND_FLOOR);
//        BigDecimal q2 = _q2.setScale(2, BigDecimal.ROUND_FLOOR);
//        BigDecimal q3 = _q3.setScale(2, BigDecimal.ROUND_FLOOR);
//        return new BigDecimal[]{q1, q2, q3};
//    }
//
//
//}