package com.github.smile_ryan.address.analyzer.controller;

import com.github.smile_ryan.address.analyzer.common.model.Address;
import com.github.smile_ryan.address.analyzer.common.model.User;
import com.github.smile_ryan.address.analyzer.service.AnalyzeService;
import com.google.common.base.Preconditions;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/user")
    public User user(@RequestParam String text, @RequestParam(required = false) boolean analyzeAddressStreet) {
        Preconditions.checkNotNull(text);
        return analyzeUserService.analyzeUser(text, analyzeAddressStreet);
    }

    @GetMapping("/address")
    public List<Address> address(@RequestParam String address, @RequestParam(required = false) boolean analyzeAddressStreet) {
        Preconditions.checkNotNull(address);
        return analyzeAddressService.analyzeAddress(address, analyzeAddressStreet);
    }


}
