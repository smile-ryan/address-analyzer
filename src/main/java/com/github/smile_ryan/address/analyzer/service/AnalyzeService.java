package com.github.smile_ryan.address.analyzer.service;

import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.User;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeAddressRequest;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeUserRequest;
import java.util.List;

/**
 * <pre>
 * 名称：AnalyzeService
 * 描述：AnalyzeService.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
public interface AnalyzeService {

    User analyzeUser(AnalyzeUserRequest userRequest);

    List<Address> analyzeAddress(AnalyzeAddressRequest addressRequest);

}
