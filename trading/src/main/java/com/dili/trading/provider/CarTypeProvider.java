package com.dili.trading.provider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.dili.orders.dto.MyBusinessType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dili.assets.sdk.dto.CarTypeForBusinessDTO;
import com.dili.orders.rpc.AssetsRpc;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.BatchProviderMeta;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.provider.BatchDisplayTextProviderSupport;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;

/**
 * 车类型模糊匹配
 */

@Component
@Scope("prototype")
public class CarTypeProvider extends BatchDisplayTextProviderSupport {

    @Autowired
    AssetsRpc assetsRpc;

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        CarTypeForBusinessDTO carTypeForJmsfDTO = new CarTypeForBusinessDTO();
        if (Objects.nonNull(obj)) {
            carTypeForJmsfDTO.setKeyword(obj.toString());
        }
//        carTypeForJmsfDTO.setBusinessCode("kcjm");
        carTypeForJmsfDTO.setBusinessCode(MyBusinessType.KCJM.getCode());
        carTypeForJmsfDTO.setMarketId(userTicket.getFirmId());
        List<CarTypeForBusinessDTO> list = assetsRpc.listCarType(carTypeForJmsfDTO).getData();
        List<ValuePair<?>> resultList = list.stream().map(f -> {
            return (ValuePair<?>) new ValuePairImpl(f.getCarTypeName(), f.getId());
        }).collect(Collectors.toList());
        return resultList;
    }


    @Override
    protected BatchProviderMeta getBatchProviderMeta(Map metaMap) {
        BatchProviderMeta batchProviderMeta = DTOUtils.newInstance(BatchProviderMeta.class);
        //设置主DTO和关联DTO需要转义的字段名
        batchProviderMeta.setEscapeFiled("carTypeName");
        //忽略大小写关联
        batchProviderMeta.setIgnoreCaseToRef(true);
        //关联(数据库)表的主键的字段名，默认取id
        batchProviderMeta.setRelationTablePkField("id");
        //当未匹配到数据时，返回的值
        batchProviderMeta.setMismatchHandler(t -> "-");
        return batchProviderMeta;
    }

    @Override
    protected List getFkList(List<String> relationIds, Map metaMap) {
        if (CollectionUtils.isEmpty(relationIds)) {
            return Collections.EMPTY_LIST;
        }
        List<Long> ids = relationIds.stream().distinct().map(f -> Long.valueOf(f)).collect(Collectors.toList());
        CarTypeForBusinessDTO carTypeForJmsfDTO = new CarTypeForBusinessDTO();
        carTypeForJmsfDTO.setIds(ids);
        return assetsRpc.listCarType(carTypeForJmsfDTO).getData();
    }
}
