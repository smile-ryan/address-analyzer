package com.github.smile_ryan.address.analyzer.service.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <pre>
 * 名称：StrategyFactory
 * 描述：StrategyFactory.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Component
public class AnalyzeStrategyFactory {

    @Autowired
    private AnalyzeStrategy chinaAnalyzeStrategy;

    public AnalyzeStrategy createStrategy(String country) {
        switch (country) {
            case "CN":
                return chinaAnalyzeStrategy;
            case "TW":
                return chinaAnalyzeStrategy;
        }
        throw new IllegalArgumentException("CountryCode is invalid.");

    }

}
