package com.dili.trading.provider;

import com.dili.assets.sdk.dto.TradeTypeDto;
import com.dili.assets.sdk.dto.TradeTypeQuery;
import com.dili.assets.sdk.rpc.TradeTypeRpc;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.BatchProviderMeta;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.provider.BatchDisplayTextProviderSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 车类型模糊匹配
 */

@Component
@Scope("prototype")
public class TradeTypeProvider extends BatchDisplayTextProviderSupport {

    @Autowired
    private TradeTypeRpc tradeTypeRpc;

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        TradeTypeQuery tradeTypeQuery = new TradeTypeQuery();
        if (Objects.nonNull(obj)) {
            tradeTypeQuery.setKeyword(obj.toString());
        }
        List<TradeTypeDto> rows = this.tradeTypeRpc.query(tradeTypeQuery).getRows();
        return rows.stream().map(f -> {
            return (ValuePair<?>) new ValuePairImpl(f.getName(), f.getCode());
        }).collect(Collectors.toList());
    }

    @Override
    protected BatchProviderMeta getBatchProviderMeta(Map metaMap) {
        BatchProviderMeta batchProviderMeta = DTOUtils.newInstance(BatchProviderMeta.class);
        // 设置主DTO和关联DTO需要转义的字段名
        batchProviderMeta.setEscapeFiled("name");
        // 忽略大小写关联
        batchProviderMeta.setIgnoreCaseToRef(true);
        // 关联(数据库)表的主键的字段名，默认取id
        batchProviderMeta.setRelationTablePkField("code");
        // 当未匹配到数据时，返回的值
        batchProviderMeta.setMismatchHandler(t -> "-");
        return batchProviderMeta;
    }

    @Override
    protected List getFkList(List<String> relationIds, Map metaMap) {
        TradeTypeQuery tradeTypeQuery = new TradeTypeQuery();
        List<TradeTypeDto> rows = this.tradeTypeRpc.query(tradeTypeQuery).getRows();
        return rows;
    }
}
