package com.github.smile_ryan.address.analyzer.service.impl;

import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.model.domain.User;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeAddressRequest;
import com.github.smile_ryan.address.analyzer.common.model.request.AnalyzeUserRequest;
import com.github.smile_ryan.address.analyzer.service.AnalyzeService;
import com.google.common.collect.Lists;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * 名称：UserService
 * 描述：UserService.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Service
public class AnalyzeUserService implements AnalyzeService {

    private final static List<String> DISTURB_WORDS = Lists.newArrayList("收件人", "收货人", "收货", "收件", "电话", "手机", "号码", "身份证", "身份证号", "姓名", "详细", "详情", "地址");

    private final static String REGX_ID_NUM = "(?<=\\D|^)\\d{17}[\\d|Xx](?=\\D|$)";

    private final static String REGX_ZIP_CODE = "(?<=\\D|^)\\d{6}(?=\\D|$)";

    private final static String REGX_PHONE_NUM = "(?<=\\D|^)(\\(?\\++\\d{1,3}\\)?[\\s|\\-])?\\d{3}[\\s|\\-]?\\d{4}[\\s|\\-]?\\d{4}(?=\\D|$)";


    @Autowired
    private AnalyzeService analyzeAddressService;

    @Override
    public User analyzeUser(AnalyzeUserRequest userRequest) {
        User user = new User();
        user.setName(extractName(userRequest.getText()));
        user.setPhoneNumber(extractPhoneNum(userRequest.getText()));
        user.setIdNumber(extractIDNum(userRequest.getText()));
        user.setZipCode(extractZIPCode(userRequest.getText()));
        String address = extractAddress(userRequest.getText(), user);
        userRequest.getAddress().setAddress(address);
        List<Address> addressList = analyzeAddress(userRequest.getAddress());
        user.setAddresses(addressList);
        return user;
    }

    @Override
    public List<Address> analyzeAddress(AnalyzeAddressRequest addressRequest) {
        return analyzeAddressService.analyzeAddress(addressRequest);
    }

    private String deleteDisturbWords(String text) {
        for (String word : DISTURB_WORDS) {
            text = text.replaceAll(word, "");
        }
        return text.replaceAll("[\n|\r]", " ");
    }

    private String extractName(String text) {
        List<Term> termList = HanLP.segment(text);
        Iterator<Term> iterator = termList.iterator();
        while (iterator.hasNext()) {
            Term term = iterator.next();
            if (term.nature.startsWith("nr")) {
                if (term.word.length() < 2) {
                    return term.word + iterator.next().word;
                }
                return term.word;
            }
        }
        return null;
    }

    private String extractPhoneNum(String text) {
        Pattern r = Pattern.compile(REGX_PHONE_NUM);
        Matcher m = r.matcher(text);
        return m.find() ? m.group() : null;
    }


    private String extractIDNum(String text) {
        Pattern r = Pattern.compile(REGX_ID_NUM);
        Matcher m = r.matcher(text);
        return m.find() ? StringUtils.trim(m.group()) : null;
    }

    private String extractZIPCode(String text) {
        Pattern r = Pattern.compile(REGX_ZIP_CODE);
        Matcher m = r.matcher(text);
        return m.find() ? m.group() : null;
    }


    private String extractAddress(String text, User user) {
        text = deleteDisturbWords(text);
        if (StringUtils.isNotEmpty(user.getName())) {
            text = text.replaceAll(user.getName(), "");
        }
        if (StringUtils.isNotEmpty(user.getIdNumber())) {
            text = text.replaceAll(user.getIdNumber(), "");
        }
        if (StringUtils.isNotEmpty(user.getPhoneNumber())) {
            text = text.replaceAll(user.getPhoneNumber(), "");
        }
        if (StringUtils.isNotEmpty(user.getZipCode())) {
            text = text.replaceAll(user.getZipCode(), "");
        }
        return text;
    }

}
