package com.dili.trading.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dili.orders.domain.WeighingBillState;
import com.dili.orders.domain.WeighingStatement;
import com.dili.orders.domain.WeighingStatementState;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.util.MoneyUtils;

/**
 * 转离场申请单的审批状态 审批状态（1.待审核/2.通过/3.拒绝）默认为1
 */

@Component
public class WeighingStatementStateProvider implements ValueProvider {

	@Override
	public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
		List<ValuePair<?>> list = new ArrayList<ValuePair<?>>();
		for (WeighingStatementState state : WeighingStatementState.values()) {
			list.add(new ValuePairImpl<Integer>(state.getName(), state.getValue()));
		}
		return list;
	}

	@Override
	public String getDisplayText(Object object, Map map, FieldMeta fieldMeta) {
		if (null == object) {
			return null;
		}
		if (object instanceof WeighingStatement) {
			for (Field field : WeighingStatement.class.getDeclaredFields()) {
				String[] strs = map.get(ValueProvider.FIELD_KEY).toString().split("\\.");
				if (field.getName().equals(strs[1])) {
					if (!field.canAccess(object)) {
						field.setAccessible(true);
					}
					try {
						Object obj = field.get(object);
						if (obj == null) {
							return null;
						}
						for (WeighingStatementState state : WeighingStatementState.values()) {
							if (state.getValue().equals(obj)) {
								return state.getName();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			}
			return null;
		} else {
			return MoneyUtils.centToYuan((Long) object);
		}
	}
}
