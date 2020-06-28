package com.dili.trading.provider;

import com.dili.assets.sdk.dto.CarTypeDTO;
import com.dili.assets.sdk.dto.CarTypePublicDTO;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import com.dili.trading.rpc.TruckRpc;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 转离场申请单，结算单的车辆类型
 */

@Component
@Scope("prototype")
public class CarTypeProvider implements ValueProvider {
    private static final List<ValuePair<?>> BUFFER = new ArrayList<>();

    @Autowired
    TruckRpc assetsRpc;


    @Override
    public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object object, Map metaMap, FieldMeta fieldMeta) {
        if (object == null) {
            return null;
        }
        CarTypePublicDTO carTypePublicDTO = new CarTypePublicDTO();
        carTypePublicDTO.setMarketId(3l);
        //获取所有车型
        List<CarTypeDTO> list = assetsRpc.listCarType(carTypePublicDTO).getData();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        BUFFER.addAll(list.stream().map(e -> new ValuePairImpl<>(e.getName(), e.getId().toString())).collect(Collectors.toList()));
        ValuePair<?> valuePair = BUFFER.stream().filter(val -> object.toString().equals(val.getValue())).findFirst().orElseGet(null);
        if (null != valuePair) {
            return valuePair.getText();
        }
        return null;
    }
}
