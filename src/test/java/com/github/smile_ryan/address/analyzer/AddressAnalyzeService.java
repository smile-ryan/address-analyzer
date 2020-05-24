//package com.github.smile_ryan.address.analyzer.service;
//
//import com.github.smile_ryan.address.analyzer.common.lucene.SynonymsAnalyzer;
//import com.github.smile_ryan.address.analyzer.common.model.Address;
//import com.github.smile_ryan.address.analyzer.common.model.Region;
//import com.github.smile_ryan.address.analyzer.common.util.AddressUtils;
//import com.google.common.base.Joiner;
//import com.google.common.base.Splitter;
//import com.google.common.collect.Lists;
//import com.google.gson.Gson;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Collectors;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.tuple.Pair;
//import org.apache.http.client.utils.CloneUtils;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field.Store;
//import org.apache.lucene.document.IntPoint;
//import org.apache.lucene.document.NumericDocValuesField;
//import org.apache.lucene.document.StoredField;
//import org.apache.lucene.document.StringField;
//import org.apache.lucene.document.TextField;
//import org.apache.lucene.index.Term;
//import org.apache.lucene.queryparser.classic.ParseException;
//import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.BooleanClause.Occur;
//import org.apache.lucene.search.BooleanQuery;
//import org.apache.lucene.search.FuzzyQuery;
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
//public class AddressAnalyzeService {
//
//
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
//            String regionPath = addressSpits.get(4);
//            int regionLevel = Splitter.on(",").splitToList(regionPath).size();
//            doc.add(new StringField("RegionCode", regionCode, Store.YES));
//            doc.add(new TextField("RegionName", regionName, Store.YES));
//            doc.add(new TextField("ShortName", AddressUtils.extractShortName(regionName), Store.YES));
//            doc.add(new StringField("ParentCode", parentCode, Store.YES));
//            doc.add(new IntPoint("RegionLevel", regionLevel));
//            doc.add(new StoredField("RegionLevel", regionLevel));
//            doc.add(new NumericDocValuesField("RegionLevel", regionLevel));
//            doc.add(new StringField("RegionPath", regionPath, Store.YES));
//            luceneService.addDocument(doc);
//        }
//    }
//
//
//
//    public List<Address> analyzeAddress(String address) {
//        List<Address> addressList = Lists.newLinkedList();
//        String cleanAddress = address.replaceAll(AddressUtils.SYMBOL_REGX, "");
//        if (StringUtils.isEmpty(cleanAddress)) {
//            return addressList;
//        }
//        List<String> tokenizeList = luceneService.tokenize(cleanAddress);
//        tokenizeList = tokenizeList.stream().distinct().collect(Collectors.toList());
//        System.out.println(new Gson().toJson(tokenizeList));
//        recursion(0, tokenizeList, new Address(), addressList);
//        addressList = processAddressList(addressList);
//        System.out.println(addressList);
//        System.out.println("--------------------------------------------------------------------------------------------------------------");
//        return addressList;
//    }
//
//    public Pair<ScoreDoc, Document> searchByRegionCode(String regionCode) {
//        Query query = new TermQuery(new Term("RegionCode", regionCode));
//        List<Pair<ScoreDoc, Document>> list = luceneService.search(query);
//        return CollectionUtils.isEmpty(list) ? null : list.get(0);
//    }
//
//    public List<Region> searchRegion(String regionName, String parentCode, boolean isSearchStreet) {
//        List<Region> regionList = Lists.newLinkedList();
//        try {
//            if (StringUtils.isEmpty(regionName)) {
//                return regionList;
//            }
//            BooleanQuery.Builder builder = new BooleanQuery.Builder();
//            Query nameQuery = new QueryParser("RegionName", new SynonymsAnalyzer()).parse(regionName);
//            builder.add(nameQuery, Occur.SHOULD);
//
//            Query nameQuery2 = new FuzzyQuery(new Term("RegionName", regionName), 1);
//            builder.add(nameQuery2, Occur.SHOULD);
//
//
//            Query levelQuery = isSearchStreet ? IntPoint.newExactQuery("RegionLevel", 4) : IntPoint.newRangeQuery("RegionLevel", 1, 3);
//            builder.add(levelQuery, Occur.FILTER);
//            if (StringUtils.isNotEmpty(parentCode)) {
//                WildcardQuery pathQuery = new WildcardQuery(new Term("RegionPath", "*" + parentCode + ",*"));
//                builder.add(pathQuery, Occur.FILTER);
//            }
//            regionList = luceneService.search(builder.build()).stream().map(pair -> AddressUtils.convertDocument(pair, true)).collect(Collectors.toList());
//        } catch (ParseException e) {
//            log.error(e.getMessage(), e);
//        }
//        return regionList;
//    }
//
//    private void recursion(int i, List<String> tokenizeList, Address address, List<Address> addressList) {
//        try {
//            if (i >= tokenizeList.size() || address.getStreet() != null) {
//                return;
//            }
//            String regionName = tokenizeList.get(i++);
//            Region parentRegion = address.getLowestLevelRegion();
//            String parentCode = parentRegion == null ? null : parentRegion.getRegionCode();
//            int parentLevel = parentRegion == null ? 0 : parentRegion.getRegionLevel();
//            String detail = Joiner.on("").join(tokenizeList.subList(i, tokenizeList.size()));
//            if (parentLevel == 3 && !regionName.contains("街道")) {
//                detail = Joiner.on("").join(tokenizeList.subList(i - 1, tokenizeList.size()));
//                regionName = detail;
//            }
//            boolean isSearchStreet = (parentRegion != null && parentRegion.getRegionLevel() != null) && parentRegion.getRegionLevel() >= 3;
//            List<Region> regionList = searchRegion(regionName, parentCode, isSearchStreet);
//            regionList = AddressUtils.filterRegionList(regionList);
//            for (Region region : regionList) {
//                Address clone = CloneUtils.cloneObject(address);
//                clone.setRegion(region);
//                addressList.add(clone);
//                addressList.remove(address);
//                if (region.getRegionName().contains(detail)) {
//                    clone.setDetail(null);
//                } else {
//                    clone.setDetail(detail.replace(region.getRegionName(), ""));
//                }
//                recursion(i, tokenizeList, clone, addressList);
//            }
//        } catch (CloneNotSupportedException e) {
//            log.error(e.getMessage(), e);
//        }
//    }
//
//    private void fillAddress(Address address) {
//        Region lowest = address.getLowestLevelRegion();
//        List<String> pathList = Splitter.on(",").omitEmptyStrings().splitToList(lowest.getRegionPath());
//        if (address.getProvince() == null && pathList.size() >= 1) {
//            String provinceCode = pathList.get(0);
//            Region region = AddressUtils.convertDocument(searchByRegionCode(provinceCode), false);
//            address.setRegion(region);
//        }
//        if (address.getCity() == null && pathList.size() >= 2) {
//            String cityCode = pathList.get(1);
//            Region region = AddressUtils.convertDocument(searchByRegionCode(cityCode), false);
//            address.setRegion(region);
//        }
//        if (address.getDistrict() == null && pathList.size() >= 3) {
//            String districtCode = pathList.get(2);
//            Region region = AddressUtils.convertDocument(searchByRegionCode(districtCode), false);
//            address.setRegion(region);
//        }
//    }
//
//
//    public List<Address> processAddressList(List<Address> addressList) {
//        addressList = addressList.stream().peek(Address::calculateRelevance).collect(Collectors.toList());
//        addressList.sort(Comparator.comparing(Address::getRelevance));
//        Collections.reverse(addressList);
//        addressList = AddressUtils.filterAddressList(addressList);
//        addressList = addressList.stream().peek(this::fillAddress).collect(Collectors.toList());
//        return addressList;
//    }
//
//
//}