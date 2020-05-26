package com.github.smile_ryan.address.analyzer.controller;

import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.User;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeAddressRequest;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeUserRequest;
import com.github.smile_ryan.address.analyzer.service.AnalyzeService;
import com.google.common.base.Preconditions;
import com.hankcs.hanlp.HanLP;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

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
    public User user(@RequestBody @Valid AnalyzeUserRequest user) {
        Preconditions.checkNotNull(user.getCountryCode());
        return analyzeUserService.analyzeUser(user);
    }

    @GetMapping("/address")
    public List<Address> address(@RequestBody @Valid AnalyzeAddressRequest address) {
        Preconditions.checkNotNull(address.getCountryCode());
        return analyzeAddressService.analyzeAddress(address);
    }


}
