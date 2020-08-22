package com.dili.trading.provider;

import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.provider.DatetimeProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 如果返回空串的话，就直接返回null
 */
@Component
@Scope("prototype")
public class DatetimeProviderPlus extends DatetimeProvider {
    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        String displayText = super.getDisplayText(obj, metaMap, fieldMeta);
        if (Objects.equals(displayText, "")) {
            return null;
        }
        return displayText;
    }
}
