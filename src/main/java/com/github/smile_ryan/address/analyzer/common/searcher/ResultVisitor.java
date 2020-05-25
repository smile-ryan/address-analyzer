package com.github.smile_ryan.address.analyzer.common.searcher;

import com.github.smile_ryan.address.analyzer.common.model.domain.Address;
import com.github.smile_ryan.address.analyzer.common.util.AddressUtils;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * <pre>
 * 名称：ResultVisitor
 * 描述：ResultVisitor.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Component
public class ResultVisitor {

    public List<Address> visit(TreeNode node) {
        if (CollectionUtils.isEmpty(node.getChildren())) {
            ArrayList<Address> addressList = Lists.newArrayList();
            if (node.getRegion() == null) {
                return addressList;
            }
            Address address = new Address();
            address.setRegion(node.getRegion());
            address.setDetail(AddressUtils.extractDetail(node));
            addressList.add(address);
            return addressList;
        }

        List<Address> addressList = Lists.newArrayList();
        for (TreeNode child : node.getChildren()) {
            addressList.addAll(child.accept(this));
        }
        return addressList;
    }

}
