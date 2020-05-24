package com.github.smile_ryan.address.analyzer.controller;

import com.github.smile_ryan.address.analyzer.common.model.Address;
import com.github.smile_ryan.address.analyzer.common.model.User;
import com.github.smile_ryan.address.analyzer.service.AddressService;
import com.github.smile_ryan.address.analyzer.service.UserService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
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
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @GetMapping("/address/init")
    public String init() throws IOException {
        addressService.deleteAllAddress();
        addressService.loadAddress();
        addressService.optimizeAddress();
        return "OK";
    }

    @GetMapping("/user")
    public User user(@RequestParam String text) {
        Preconditions.checkNotNull(text);
        User user = new User();
        user.setName(userService.extractName(text));
        user.setPhoneNum(userService.extractPhoneNum(text));
        user.setIdNum(userService.extractIDNum(text));
        user.setZipCode(userService.extractZIPCode(text));
        String address = userService.extractAddress(text, user);
        List<Address> addressList = addressService.analyze(address);
        user.setAddressList(addressList);
        return user;
    }

    @GetMapping("/address")
    public List<Address> address(@RequestParam String address) {
        return addressService.analyze(address);
    }


}
