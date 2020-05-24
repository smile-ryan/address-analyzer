package com.github.smile_ryan.address.analyzer.common.searcher;

import com.github.smile_ryan.address.analyzer.common.model.Region;
import com.github.smile_ryan.address.analyzer.common.searcher.TreeNode.SearchNodeBuilder;
import com.github.smile_ryan.address.analyzer.common.util.AddressUtils;
import com.github.smile_ryan.address.analyzer.service.impl.AnalyzeAddressService;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * <pre>
 * 名称：Visitor
 * 描述：Visitor.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Slf4j
@Component
public class SearchVisitor {

    @Autowired
    private AnalyzeAddressService analyzeAddressService;

    public void visit(TreeNode parentNode) {
        String term = parentNode.nextTokenize();
        if (StringUtils.isEmpty(term)
            || (parentNode.getRegion() != null && parentNode.getRegion().getRegionLevel() >= 4)) {
            return;
        }
        List<Region> regions = Lists.newArrayList();

        if (parentNode.getRegion() == null || parentNode.getRegion().getRegionLevel() < 3) {
            regions = analyzeAddressService.searchRegion(term, parentNode.getRegion());
        }
        if (parentNode.getAnalyzeStreet() && (CollectionUtils.isEmpty(regions) || AddressUtils.inferenceRegionLevel(term) == 4)) {
            regions = analyzeAddressService.searchStreet(term, parentNode.getRegion());
        }
        log.debug("Keyword:{}, TotalHit:{}, ParentRegion:{}", term, regions.size(), parentNode.getRegion());
        for (Region region : regions) {
            TreeNode child = new SearchNodeBuilder()
                .tokenizeList(parentNode.getTokenizeList())
                .analyzeStreet(parentNode.getAnalyzeStreet())
                .tokenize(term).region(region).build();
            parentNode.getChildren().add(child);
            child.accept(this);
        }
    }


}
