package com.github.smile_ryan.address.analyzer.service.impl;

import com.github.smile_ryan.address.analyzer.common.lucene.SynonymsAnalyzer;
import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.Region;
import com.github.smile_ryan.address.analyzer.common.model.domain.User;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeAddressRequest;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeUserRequest;
import com.github.smile_ryan.address.analyzer.common.searcher.ResultVisitor;
import com.github.smile_ryan.address.analyzer.common.searcher.SearchVisitor;
import com.github.smile_ryan.address.analyzer.common.searcher.TreeNode;
import com.github.smile_ryan.address.analyzer.common.searcher.TreeNode.SearchNodeBuilder;
import com.github.smile_ryan.address.analyzer.common.util.AddressUtils;
import com.github.smile_ryan.address.analyzer.service.AnalyzeService;
import com.github.smile_ryan.address.analyzer.service.LuceneService;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
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
public class AnalyzeAddressService implements AnalyzeService {

    @Autowired
    private SearchVisitor searchVisitor;

    @Autowired
    private ResultVisitor resultVisitor;

    @Autowired
    private LuceneService luceneService;

    @Override
    public List<Address> analyzeAddress(AnalyzeAddressRequest addressRequest) {
        addressRequest.setTokenizeList(tokenize(addressRequest.getAddress()));
        TreeNode treeNode = new SearchNodeBuilder().analyzeAddressRequest(addressRequest).build().accept(searchVisitor);
        return AddressUtils.processAddressList(treeNode.accept(resultVisitor))
            .stream().peek(this::fillAddress).collect(Collectors.toList());
    }

    @Override
    public User analyzeUser(AnalyzeUserRequest userRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
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


    public List<Region> searchRegion(String regionName, TreeNode parentNode) {
        List<Region> regionList = Lists.newLinkedList();
        try {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            Query nameQuery = new QueryParser("RegionName", new SynonymsAnalyzer())
                .parse("RegionName:" + regionName + "^2.0 +ShortName:" + AddressUtils.extractShortName(regionName));
            builder.add(nameQuery, Occur.MUST);
            builder.add(IntPoint.newRangeQuery("RegionLevel", 1, 4), Occur.FILTER);

            builder.add(new TermQuery(new Term("RegionScheme", parentNode.getAnalyzeAddressRequest().getRegionScheme())), Occur.FILTER);

            if (parentNode.getRegion() != null && StringUtils.isNotEmpty(parentNode.getRegion().getRegionCode())) {
                builder.add(new WildcardQuery(new Term("RegionPath", "*" + parentNode.getRegion().getRegionCode() + ",*")), Occur.FILTER);
            }
            regionList = luceneService.search(builder.build()).stream().map(pair -> AddressUtils.convertDocument(pair, true)).collect(Collectors.toList());
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return regionList;
    }

    public List<Region> searchStreet(String regionName, TreeNode parentNode) {
        List<Region> regionList = Lists.newLinkedList();
        try {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            Query nameQuery = new QueryParser("RegionName", new KeywordAnalyzer()).parse("RegionName:" + regionName + "^2.0");
            builder.add(nameQuery, Occur.SHOULD);

            if ("CN".equalsIgnoreCase(parentNode.getAnalyzeAddressRequest().getCountryCode())) {
                Query shortNameQuery = new QueryParser("ShortName", new KeywordAnalyzer()).parse(AddressUtils.extractShortName(regionName));
                builder.add(shortNameQuery, Occur.MUST);
            }
            builder.add(new TermQuery(new Term("RegionScheme", parentNode.getAnalyzeAddressRequest().getRegionScheme())), Occur.FILTER);

            Query levelQuery = IntPoint.newExactQuery("RegionLevel", 5);
            builder.add(levelQuery, Occur.FILTER);
            if (parentNode.getRegion() != null && StringUtils.isNotEmpty(parentNode.getRegion().getRegionCode())) {
                WildcardQuery pathQuery = new WildcardQuery(new Term("RegionPath", "*" + parentNode.getRegion().getRegionCode() + ",*"));
                builder.add(pathQuery, Occur.FILTER);
            }
            regionList = luceneService.search(builder.build()).stream().map(pair -> AddressUtils.convertDocument(pair, true)).collect(Collectors.toList());
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return regionList;
    }

    public Pair<ScoreDoc, Document> searchByRegionCode(String regionCode, String regionScheme) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(new TermQuery(new Term("RegionCode", regionCode)), Occur.FILTER);
        builder.add(new TermQuery(new Term("RegionScheme", regionScheme)), Occur.FILTER);
        List<Pair<ScoreDoc, Document>> list = luceneService.search(builder.build());
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    private void fillAddress(Address address) {
        Region lowest = address.getLowestLevelRegion();
        List<String> pathList = Splitter.on(",").omitEmptyStrings().splitToList(lowest.getRegionPath());
        if (address.getCountry() == null && pathList.size() >= 1) {
            String countryCode = pathList.get(0);
            Region region = AddressUtils.convertDocument(searchByRegionCode(countryCode, lowest.getRegionScheme()), false);
            address.setRegion(region);
        }
        if (address.getProvince() == null && pathList.size() >= 2) {
            String provinceCode = pathList.get(1);
            Region region = AddressUtils.convertDocument(searchByRegionCode(provinceCode, lowest.getRegionScheme()), false);
            address.setRegion(region);
        }
        if (address.getCity() == null && pathList.size() >= 3) {
            String cityCode = pathList.get(2);
            Region region = AddressUtils.convertDocument(searchByRegionCode(cityCode, lowest.getRegionScheme()), false);
            address.setRegion(region);
        }
        if (address.getDistrict() == null && pathList.size() >= 4) {
            String districtCode = pathList.get(3);
            Region region = AddressUtils.convertDocument(searchByRegionCode(districtCode, lowest.getRegionScheme()), false);
            address.setRegion(region);
        }
    }


}
