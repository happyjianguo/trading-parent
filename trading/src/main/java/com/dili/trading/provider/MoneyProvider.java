package com.dili.trading.provider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dili.orders.domain.WeighingStatement;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.util.MoneyUtils;

/**
 * 转离场申请单的审批状态 审批状态（1.待审核/2.通过/3.拒绝）默认为1
 */

@Component
@Scope("prototype")
public class MoneyProvider implements ValueProvider {

	@Override
	public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
		return null;
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
						return MoneyUtils.centToYuan((Long) obj);
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
