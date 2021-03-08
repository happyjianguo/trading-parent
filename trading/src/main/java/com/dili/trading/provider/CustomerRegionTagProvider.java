package com.dili.trading.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dili.customer.sdk.enums.CustomerEnum.BusinessRegionTag;
import com.dili.orders.domain.PriceState;
import com.dili.orders.domain.WeighingBillState;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;

/**
 * 转离场申请单的审批状态 审批状态（1.待审核/2.通过/3.拒绝）默认为1
 */

@Component
public class CustomerRegionTagProvider implements ValueProvider {

	@Override
	public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
		List<ValuePair<?>> list = new ArrayList<ValuePair<?>>();
		for (BusinessRegionTag tag : BusinessRegionTag.values()) {
			list.add(new ValuePairImpl<Integer>(tag.getValue(), tag.getCode()));
		}
		return list;
	}

	@Override
	public String getDisplayText(Object object, Map map, FieldMeta fieldMeta) {
		if (null == object) {
			return null;
		}
		for (BusinessRegionTag tag : BusinessRegionTag.values()) {
			if (tag.getCode().equals(object)) {
				return tag.getValue();
			}
		}
		return null;
	}
}
