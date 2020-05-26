package com.github.smile_ryan.address.analyzer.common.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <pre>
 * 名称：AnalyzeUserRequest
 * 描述：AnalyzeUserRequest.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Data
public class AnalyzeUserRequest {

    @NotBlank
    private String text;

    private String countryCode = "CN";

    private boolean analyzeStreet;

    private String regionScheme = "OLD";

}
