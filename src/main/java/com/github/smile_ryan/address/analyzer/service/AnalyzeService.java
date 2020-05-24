package com.github.smile_ryan.address.analyzer.service;

import com.github.smile_ryan.address.analyzer.common.model.Address;
import com.github.smile_ryan.address.analyzer.common.model.User;
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

    User analyzeUser(String text, Boolean analyzeAddressStreet);

    List<Address> analyzeAddress(String address, Boolean analyzeAddressStreet);

}
