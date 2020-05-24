package com.github.smile_ryan.address.analyzer.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 * 名称：Region
 * 描述：Region.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Data
public class Region {

    private String regionCode;

    private String regionName;

    @JsonIgnore
    private String shortName;

    private Integer regionLevel;

    private String regionPath;

    private String parentCode;

    private BigDecimal relevance = BigDecimal.ZERO;

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (regionCode == null ? 0 : regionCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof Region) {
            Region other = (Region) obj;
            return StringUtils.equals(this.toString(), other.toString());
        }

        return false;
    }


    @Override
    public String toString() {
        if (regionCode == null || StringUtils.isBlank(regionName)) {
            return "";
        }
        return this.regionCode + "|" + this.regionName;
    }

}
