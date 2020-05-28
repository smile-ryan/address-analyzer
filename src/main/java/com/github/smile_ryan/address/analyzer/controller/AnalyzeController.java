package com.github.smile_ryan.address.analyzer.controller;

import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.User;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeAddressRequest;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeUserRequest;
import com.github.smile_ryan.address.analyzer.service.AnalyzeService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * 名称：AddressController
 * 描述：AddressController.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@RestController
@RequestMapping("/analyze")
public class AnalyzeController {

    @Autowired
    private AnalyzeService analyzeAddressService;

    @Autowired
    private AnalyzeService analyzeUserService;

    @PostMapping("/user")
    public User user(@RequestBody @Valid AnalyzeUserRequest user) {
        return analyzeUserService.analyzeUser(user);
    }

    @PostMapping("/address")
    public Address address(@RequestBody @Valid AnalyzeAddressRequest address) {
        return analyzeAddressService.analyzeAddress(address);
    }


}
