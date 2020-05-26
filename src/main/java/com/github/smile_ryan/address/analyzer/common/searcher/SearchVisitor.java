package com.github.smile_ryan.address.analyzer.common.searcher;

import com.github.smile_ryan.address.analyzer.common.enums.RegionLevel;
import com.github.smile_ryan.address.analyzer.common.model.domain.Region;
import com.github.smile_ryan.address.analyzer.common.searcher.TreeNode.SearchNodeBuilder;
import com.github.smile_ryan.address.analyzer.common.util.AddressUtils;
import com.github.smile_ryan.address.analyzer.service.strategy.AnalyzeStrategy;
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

    public void visit(TreeNode parentNode, AnalyzeStrategy strategy) {
        String term = parentNode.getAnalyzeAddressRequest().nextTokenize(parentNode.getTokenize());
        if (StringUtils.isEmpty(term)
            || (parentNode.getRegion() != null && parentNode.getRegion().getRegionLevel() >= RegionLevel.street.getValue())) {
            return;
        }
        List<Region> regions = Lists.newArrayList();

        if (parentNode.getRegion() == null || parentNode.getRegion().getRegionLevel() < RegionLevel.district.getValue()) {
            regions = strategy.searchRegion(term, parentNode);
        }
        if (parentNode.getAnalyzeAddressRequest().isAnalyzeStreet() &&
            (CollectionUtils.isEmpty(regions) || AddressUtils.inferenceRegionLevel(term) == RegionLevel.street.getValue())) {
            regions = strategy.searchStreet(term, parentNode);
        }
        log.debug("Keyword:{}, TotalHit:{}, ParentRegion:{}", term, regions.size(), parentNode.getRegion());
        for (Region region : regions) {
            TreeNode child = new SearchNodeBuilder().tokenize(term)
                .analyzeAddressRequest(parentNode.getAnalyzeAddressRequest()).region(region).build();
            parentNode.getChildren().add(child);
            child.accept(this, strategy);
        }
    }


}
