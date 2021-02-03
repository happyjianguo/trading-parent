package com.dili.trading.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dili.orders.domain.MeasureType;
import com.dili.orders.domain.TradingBillType;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;

/**
 * 转离场申请单的审批状态 审批状态（1.待审核/2.通过/3.拒绝）默认为1
 */

@Component
public class TradingBillTypeProvider implements ValueProvider {

	@Override
	public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
		List<ValuePair<?>> list = new ArrayList<ValuePair<?>>();
		for (TradingBillType type : TradingBillType.values()) {
			list.add(new ValuePairImpl<Integer>(type.getName(), type.getValue()));
		}
		return list;
	}

	@Override
	public String getDisplayText(Object object, Map map, FieldMeta fieldMeta) {
		if (null == object) {
			return null;
		}
		for (TradingBillType type : TradingBillType.values()) {
			if (type.getValue().equals(object)) {
				return type.getName();
			}
		}
		return null;
	}
}
