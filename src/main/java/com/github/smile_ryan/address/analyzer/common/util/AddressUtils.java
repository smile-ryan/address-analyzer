package com.github.smile_ryan.address.analyzer.common.util;

import com.github.smile_ryan.address.analyzer.common.enums.RegionLevel;
import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.Region;
import com.github.smile_ryan.address.analyzer.common.searcher.TreeNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.springframework.util.CollectionUtils;

/**
 * <pre>
 * 名称：AddressUtils
 * 描述：AddressUtils.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
public class AddressUtils {

    public static final String REGX_SYMBOL = "[`~!@#$^&*()=|{}':;',\\\\[\\\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？\\s]";

    public static final List<String> ADDRESS_UNITS = Lists.newArrayList();

    public static final List<String> ADDRESS_UNITS_LEVEL_1 = Lists.newArrayList("特别行政区", "行政区", "自治区", "省");
    public static final List<String> ADDRESS_UNITS_LEVEL_2 = Lists.newArrayList("自治州", "地区", "市", "盟");
    public static final List<String> ADDRESS_UNITS_LEVEL_3 = Lists.newArrayList("自治县", "自治旗", "联合旗", "左旗", "右旗", "前旗", "后旗", "中旗", "市", "区", "县", "旗");
    public static final List<String> ADDRESS_UNITS_LEVEL_4 = Lists.newArrayList("乡", "镇", "街道", "林场", "农场", "牧场", "种畜场");

    public static final List<String> ADDRESS_NOISE_PHRASES = Lists.newArrayList("左翼", "右翼", "维吾尔");


    public static final List<String> SPECIAL_MUNICIPALITY = Lists.newArrayList("北京","天津","上海","重庆","香港","澳门");

    public static final List<String> SPECIAL_DISTRICT = Lists.newArrayList("香港岛","九龙","新界","澳门半岛","离岛");


    public static final List<String> MINORITIES = Lists
        .newArrayList("壮族", "满族", "回族", "苗族", "维吾尔族", "土家族", "彝族", "蒙古族", "藏族", "布依族", "侗族", "瑶族", "朝鲜族", "白族", "哈尼族", "哈萨克族", "黎族", "傣族", "畲族", "傈僳族", "仡佬族", "东乡族", "高山族", "拉祜族",
            "水族", "佤族", "纳西族", "羌族", "土族", "仫佬族", "锡伯族", "柯尔克孜族", "达斡尔族", "景颇族", "毛南族", "撒拉族", "布朗族", "塔吉克族", "阿昌族", "普米族", "鄂温克族", "怒族", "京族", "基诺族", "德昂族", "保安族", "俄罗斯族", "裕固族",
            "乌兹别克族", "门巴族", "鄂伦春族", "独龙族", "塔塔尔族", "赫哲族", "珞巴族");

    static {
        ADDRESS_UNITS.addAll(ADDRESS_UNITS_LEVEL_1);
        ADDRESS_UNITS.addAll(ADDRESS_UNITS_LEVEL_2);
        ADDRESS_UNITS.addAll(ADDRESS_UNITS_LEVEL_3);
        ADDRESS_UNITS.addAll(ADDRESS_UNITS_LEVEL_4);
    }

    public static List<Address> filterAddressList(List<Address> addressList) {
        if (addressList.size() > 1) {
            BigDecimal topRelevance = CollectionUtils.isEmpty(addressList) ? BigDecimal.ZERO : addressList.get(0).getRelevance();
            return addressList.stream().distinct()
                .filter(address -> address.getRelevance().compareTo(topRelevance.subtract(BigDecimal.valueOf(2))) >= 1)
                .limit(5)
                .collect(Collectors.toList());
        }
        return addressList;
    }

    public static Region convertDocument(Pair<ScoreDoc, Document> pair, boolean setRelevance) {
        if (pair.getKey() == null || pair.getValue() == null) {
            return null;
        }
        Document doc = pair.getValue();
        Region region = new Region();
        region.setRegionCode(doc.get("RegionCode"));
        region.setRegionName(doc.get("RegionName"));
        region.setRegionNameCN(doc.get("RegionNameCN"));
        region.setShortName(doc.get("ShortName"));
        region.setRegionLevel(doc.get("RegionLevel") != null ? Integer.parseInt(doc.get("RegionLevel")) : null);
        region.setParentCode(doc.get("ParentCode"));
        region.setRegionPath(doc.get("RegionPath"));
        region.setRegionScheme(doc.get("RegionScheme"));
        if (setRelevance) {
            region.setRelevance(BigDecimal.valueOf(pair.getKey().score));
        }
        return region;
    }

    public static String extractShortName(String regionName) {
        if (regionName.contains("族")) {
            for (String x : AddressUtils.MINORITIES) {
                regionName = regionName.replaceAll(x, "");
            }
        }
        if (regionName.length() > 2) {
            for (String y : AddressUtils.ADDRESS_UNITS) {
                if (regionName.endsWith(y)) {
                    regionName = regionName.replaceAll(y, "");
                    break;
                }
            }
        }
        if (regionName.length() > 2) {
            for (String m : AddressUtils.ADDRESS_NOISE_PHRASES) {
                regionName = regionName.replaceAll(m, "");
            }
        }
        return regionName;
    }


    public static List<Address> processAddressList(List<Address> addressList) {
        addressList = addressList.stream().peek(Address::calculateRelevance).collect(Collectors.toList());
        addressList.sort(Comparator.comparing(Address::getRelevance));
        Collections.reverse(addressList);
        addressList = AddressUtils.filterAddressList(addressList);
        return addressList;
    }


    public static int inferenceRegionLevel(String region) {
        for (String s : AddressUtils.ADDRESS_UNITS_LEVEL_1) {
            if (region.endsWith(s)) {
                return RegionLevel.province.getValue();
            }
        }
        for (String s : AddressUtils.ADDRESS_UNITS_LEVEL_2) {
            if (region.endsWith(s)) {
                return RegionLevel.city.getValue();
            }
        }
        for (String s : AddressUtils.ADDRESS_UNITS_LEVEL_3) {
            if (region.endsWith(s)) {
                return RegionLevel.district.getValue();
            }
        }
        for (String s : AddressUtils.ADDRESS_UNITS_LEVEL_4) {
            if (region.endsWith(s)) {
                return RegionLevel.street.getValue();
            }
        }
        for (String s : AddressUtils.SPECIAL_MUNICIPALITY) {
            if (region.endsWith(s)) {
                return RegionLevel.province.getValue();
            }
        }
        for (String s : AddressUtils.SPECIAL_DISTRICT) {
            if (region.endsWith(s)) {
                return RegionLevel.city.getValue();
            }
        }
        return 0;
    }


    public static String extractDetail(TreeNode node) {
        if (!StringUtils.isEmpty(node.getTokenize())) {
            List<String> tokenizeList = node.getAnalyzeAddressRequest().getTokenizeList();
            String detail = Joiner.on("").join(tokenizeList);
            detail = detail.substring(detail.indexOf(node.getTokenize()));
            if (node.getRegion().getRegionLevel() < RegionLevel.street.getValue()) {
                detail = detail.substring(detail.indexOf(node.getTokenize()) + node.getTokenize().length());
            } else {
                detail = detail.replace(node.getRegion().getRegionName(), "");
            }
            if (node.getRegion().getRegionName().contains(detail)) {
                return null;
            }
            return detail;
        }
        return null;
    }

}
