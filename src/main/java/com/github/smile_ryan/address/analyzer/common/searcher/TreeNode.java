package com.github.smile_ryan.address.analyzer.common.searcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.smile_ryan.address.analyzer.common.model.Address;
import com.github.smile_ryan.address.analyzer.common.model.Region;
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

    private String tokenize;

    private Region region;

    private Boolean analyzeStreet;

    private List<TreeNode> children = Lists.newArrayList();

    @JsonIgnore
    private List<String> tokenizeList;

    public String nextTokenize() {
        int i = tokenizeList.indexOf(tokenize);
        return ++i < tokenizeList.size() ? tokenizeList.get(i) : null;
    }

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

        private String tokenize;

        private Region region;

        private List<TreeNode> children = Lists.newArrayList();

        private List<String> tokenizeList;

        private Boolean analyzeStreet;

        public TreeNode build() {
            TreeNode treeNode = new TreeNode();
            treeNode.setTokenize(this.tokenize);
            treeNode.setRegion(this.region);
            treeNode.setChildren(this.children);
            treeNode.setTokenizeList(this.tokenizeList);
            treeNode.setAnalyzeStreet(this.analyzeStreet);
            return treeNode;
        }

        public SearchNodeBuilder tokenize(String term) {
            this.tokenize = term;
            return this;
        }

        public SearchNodeBuilder region(Region region) {
            this.region = region;
            return this;
        }

        public SearchNodeBuilder analyzeStreet(Boolean analyzeStreet) {
            this.analyzeStreet = analyzeStreet;
            return this;
        }

        public SearchNodeBuilder children(List<TreeNode> children) {
            this.children = children;
            return this;
        }

        public SearchNodeBuilder tokenizeList(List<String> tokenizeList) {
            this.tokenizeList = tokenizeList;
            return this;
        }
    }


}
