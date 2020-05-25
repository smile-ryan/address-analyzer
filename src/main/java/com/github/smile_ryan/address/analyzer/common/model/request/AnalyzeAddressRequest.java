package com.github.smile_ryan.address.analyzer.common.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <pre>
 * 名称：AnalyzeAddressRequest
 * 描述：AnalyzeAddressRequest.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Data
public class AnalyzeAddressRequest {

    @NotBlank
    private String address;

    private boolean analyzeStreet;

    private String countryCode = "CN";

    private String regionScheme = "OLD";

    @JsonIgnore
    private List<String> tokenizeList;

    public String nextTokenize(String tokenize) {
        int i = tokenizeList.indexOf(tokenize);
        return ++i < tokenizeList.size() ? tokenizeList.get(i) : null;
    }

}
