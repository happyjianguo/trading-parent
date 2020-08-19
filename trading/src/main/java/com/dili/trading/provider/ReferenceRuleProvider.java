package com.dili.trading.provider;

import com.dili.orders.domain.ReferenceRule;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 品类参考价设置 参考价规则(1.规则1 2.规则2 3.规则3 4.无)默认为1
 * @author Seabert.Zhan
 */

@Component
@Scope("prototype")
public class ReferenceRuleProvider implements ValueProvider {

	@Override
	public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
		List<ValuePair<?>> list = new ArrayList<ValuePair<?>>();
		for (ReferenceRule state : ReferenceRule.values()) {
			list.add(new ValuePairImpl<Integer>(state.getName(), state.getCode()));
		}
		return list;
	}

	@Override
	public String getDisplayText(Object object, Map map, FieldMeta fieldMeta) {
		if (null == object) {
			return null;
		}
		for (ReferenceRule state : ReferenceRule.values()) {
			if (state.getCode().equals(object)) {
				return state.getName();
			}
		}
		return null;
	}
}
