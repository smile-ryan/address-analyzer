package com.github.smile_ryan.address.analyzer.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import java.math.BigDecimal;
import lombok.Data;

/**
 * <pre>
 * 名称：Address
 * 描述：Address.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Data
public class Address implements Cloneable {

    private Region province;

    private Region city;

    private Region district;

    private Region street;

    private String detail;

    @JsonIgnore
    private String tokenize;

    private BigDecimal relevance = BigDecimal.ZERO;

    @JsonIgnore
    public Region getLowestLevelRegion() {
        if (this.getStreet() != null) {
            return this.getStreet();
        }
        if (this.getDistrict() != null) {
            return this.getDistrict();
        }
        if (this.getCity() != null) {
            return this.getCity();
        }
        if (this.getProvince() != null) {
            return this.getProvince();
        }
        return null;
    }

    public void setRegion(Region region) {
        if (region == null) {
            return;
        }
        switch (region.getRegionLevel()) {
            case 1:
                if(province == null) setProvince(region);
                break;
            case 2:
                if(city == null) setCity(region);
                break;
            case 3:
                if(district == null) setDistrict(region);
                break;
            case 4:
                if(street == null) setStreet(region);
                break;
        }
    }

    public void calculateRelevance() {
        relevance = (province == null ? BigDecimal.ZERO : province.getRelevance())
            .add(city == null ? BigDecimal.ZERO : city.getRelevance())
            .add(district == null ? BigDecimal.ZERO : district.getRelevance())
            .add(street == null ? BigDecimal.ZERO : street.getRelevance());
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (street != null) {
            return 31 * result + street.hashCode();
        }
        if (district != null) {
            return 31 * result + district.hashCode();
        }
        if (city != null) {
            return 31 * result + city.hashCode();
        }
        if (province != null) {
            return 31 * result + province.hashCode();
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof Address) {
            return this.hashCode() == obj.hashCode();
        }

        return false;
    }


    @Override
    public String toString() {
        String join = Joiner.on(",").skipNulls().join(
            province == null ? null : province.toString(),
            city == null ? null : city.toString(),
            district == null ? null : district.toString(),
            street == null ? null : street.toString(),
            detail
        );
        return "[" + join + "]";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
