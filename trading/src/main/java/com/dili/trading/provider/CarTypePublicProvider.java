package com.dili.trading.provider;

import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValueProvider;
import com.dili.trading.rpc.AssetsRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CarTypePublicProvider implements ValueProvider {

    private static final List<ValuePair<?>> BUFFER = new ArrayList<>();

    @Autowired
    AssetsRpc assetsRpc;

    @Override
    public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object object, Map metaMap, FieldMeta fieldMeta) {
//        if (null == object) {
//            return null;
//        }
//        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
//        CarTypePublicDTO carTypePublicDTO = new CarTypePublicDTO();
//        carTypePublicDTO.setMarketId(userTicket.getFirmId());
//        carTypePublicDTO.setKeyword(object.toString());
//        List<CarTypeDTO> list = assetsRpc.listCarType(carTypePublicDTO).getData();
//        BUFFER.addAll(Stream.of(list.toArray(new CarTypeDTO[list.size()]))
//                .map(e -> new ValuePairImpl<>(e.getName(), e.getId().toString()))
//                .collect(Collectors.toList()));
//        ValuePair<?> valuePair = BUFFER.stream().filter(val -> object.toString().equals(val.getValue())).findFirst().orElseGet(null);
//        if (null != valuePair) {
//            return valuePair.getText();
//        }
        return null;

    }

}
