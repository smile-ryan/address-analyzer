package com.github.smile_ryan.address.analyzer.service;

import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.User;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeAddressRequest;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeUserRequest;
import com.github.smile_ryan.address.analyzer.service.strategy.AnalyzeStrategy;
import com.github.smile_ryan.address.analyzer.service.strategy.AnalyzeStrategyFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * 名称：AnalyzeService
 * 描述：AnalyzeService.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Service
public class AnalyzeService {

    @Autowired
    private AnalyzeStrategyFactory analyzeStrategyFactory;

    public User analyzeUser(AnalyzeUserRequest userRequest) {
        AnalyzeStrategy analyzeStrategy = analyzeStrategyFactory.createStrategy(userRequest.getCountryCode());
        return analyzeStrategy.analyzeUser(userRequest);
    }


    public Address analyzeAddress(AnalyzeAddressRequest addressRequest) {
        AnalyzeStrategy analyzeStrategy = analyzeStrategyFactory.createStrategy(addressRequest.getCountryCode());
        return analyzeStrategy.analyzeAddress(addressRequest);
    }



}
