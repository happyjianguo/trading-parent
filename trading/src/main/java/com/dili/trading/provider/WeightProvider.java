package com.dili.trading.provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValueProvider;

/**
 * 转离场申请单的审批状态 审批状态（1.待审核/2.通过/3.拒绝）默认为1
 */

@Component
@Scope("prototype")
public class WeightProvider implements ValueProvider {

    @Override
    public List<ValuePair<?>> getLookupList(Object o, Map map, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object object, Map map, FieldMeta fieldMeta) {
        if (null == object || Objects.equals(0, object)) {
            return null;
        }
        return new BigDecimal(object.toString()).divide(new BigDecimal(100).setScale(2)).setScale(2, RoundingMode.HALF_UP).toString();
    }
}
