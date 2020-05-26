package com.github.smile_ryan.address.analyzer.service.strategy;

import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.Region;
import com.github.smile_ryan.address.analyzer.common.model.domain.User;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeAddressRequest;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeUserRequest;
import com.github.smile_ryan.address.analyzer.common.searcher.TreeNode;
import java.util.List;

/**
 * <pre>
 * 名称：AnalyzeStrategy
 * 描述：AnalyzeStrategy.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
public interface AnalyzeStrategy {

    List<Address> analyzeAddress(AnalyzeAddressRequest addressRequest);

    User analyzeUser(AnalyzeUserRequest userRequest);

    List<Region> searchRegion(String regionName, TreeNode parentNode);

    List<Region> searchStreet(String regionName, TreeNode parentNode);
}
