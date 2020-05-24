package com.github.smile_ryan.address.analyzer.common.model;

import java.util.List;
import lombok.Data;

/**
 * <pre>
 * 名称：User
 * 描述：User.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Data
public class User {

    private String name;

    private String phoneNum;

    private String idNum;

    private String zipCode;

    private List<Address> addressList;

}
