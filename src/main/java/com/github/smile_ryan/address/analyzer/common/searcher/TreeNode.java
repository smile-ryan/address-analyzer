package com.github.smile_ryan.address.analyzer.common.searcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.Region;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeAddressRequest;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;

/**
 * <pre>
 * 名称：TreeNode
 * 描述：TreeNode.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Data
public class TreeNode {

    private Region region;

    private List<TreeNode> children = Lists.newArrayList();

    private AnalyzeAddressRequest analyzeAddressRequest;

    @JsonIgnore
    private String tokenize;

    public TreeNode accept(SearchVisitor searchVisitor) {
        searchVisitor.visit(this);
        return this;
    }

    public List<Address> accept(ResultVisitor resultVisitor) {
        List<Address> arrayList = resultVisitor.visit(this);
        for (Address address : arrayList) {
            if (this.getRegion() != null && address.getLowestLevelRegion().getRegionPath().contains(this.getRegion().getRegionCode())) {
                address.setRegion(this.getRegion());
            }
        }
        return arrayList;
    }

    @Data
    public static class SearchNodeBuilder {

        private Region region;

        private List<TreeNode> children = Lists.newArrayList();

        private AnalyzeAddressRequest analyzeAddressRequest;

        private String tokenize;

        public TreeNode build() {
            TreeNode treeNode = new TreeNode();
            treeNode.setRegion(this.region);
            treeNode.setChildren(this.children);
            treeNode.setTokenize(this.tokenize);
            treeNode.setAnalyzeAddressRequest(this.analyzeAddressRequest);
            return treeNode;
        }

        public SearchNodeBuilder analyzeAddressRequest(AnalyzeAddressRequest analyzeAddressRequest) {
            this.analyzeAddressRequest = analyzeAddressRequest;
            return this;
        }

        public SearchNodeBuilder region(Region region) {
            this.region = region;
            return this;
        }

        public SearchNodeBuilder tokenize(String tokenize) {
            this.tokenize = tokenize;
            return this;
        }


        public SearchNodeBuilder children(List<TreeNode> children) {
            this.children = children;
            return this;
        }

    }


}
