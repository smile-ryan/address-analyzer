package com.github.smile_ryan.address.analyzer.common.enums;

import lombok.Getter;

/**
 * <pre>
 * 名称：RegionLevel
 * 描述：RegionLevel.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
public enum  RegionLevel {

    country(1), province(2), city(3), district(4), street(5),other(0);

    @Getter
    private int value;

    RegionLevel(int value) {
        this.value = value;
    }

    public static RegionLevel getRegionLevel(int value) {
        for (RegionLevel regionLevel : RegionLevel.values()) {
            if (regionLevel.value == value) {
                return regionLevel;
            }
        }
        return other;
    }

}
