package com.dili.trading.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dili.trading.rpc.TruckRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dili.assets.sdk.dto.CarTypeDTO;
import com.dili.assets.sdk.dto.CarTypePublicDTO;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.ValueProvider;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;

@Component
public class CarTypePublicProvider implements ValueProvider {

    private static final List<ValuePair<?>> BUFFER = new ArrayList<>();

    @Autowired
    TruckRpc truckRpc;

    @Override
    public List<ValuePair<?>> getLookupList(Object val, Map metaMap, FieldMeta fieldMeta) {
        return null;
    }

    @Override
    public String getDisplayText(Object object, Map metaMap, FieldMeta fieldMeta) {
        if (null == object) {
            return null;
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        CarTypePublicDTO carTypePublicDTO = new CarTypePublicDTO();
        carTypePublicDTO.setMarketId(userTicket.getFirmId());
        List<CarTypeDTO> list = truckRpc.listCarType(carTypePublicDTO).getData();
        BUFFER.addAll(Stream.of(list.toArray(new CarTypeDTO[list.size()]))
                .map(e -> new ValuePairImpl<>(e.getName(), e.getId().toString()))
                .collect(Collectors.toList()));
        ValuePair<?> valuePair = BUFFER.stream().filter(val -> object.toString().equals(val.getValue())).findFirst().orElseGet(null);
        if (null != valuePair) {
            return valuePair.getText();
        }
        return null;

    }

}
