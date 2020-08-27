package com.dili.trading.provider;

import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.orders.rpc.AssetsRpc;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.BatchProviderMeta;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.provider.BatchDisplayTextProviderSupport;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 品类模糊匹配
 */

@Component
@Scope("prototype")
public class CategoryProvider extends BatchDisplayTextProviderSupport {

    @Autowired
    AssetsRpc assetsRpc;

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setMarketId(userTicket.getFirmId());
        if (Objects.nonNull(obj)) {
            categoryDTO.setKeyword(obj.toString());
        }
        List<CategoryDTO> list = assetsRpc.list(categoryDTO).getData();
        List<ValuePair<?>> resultList = list.stream().map(f -> {
            return (ValuePair<?>) new ValuePairImpl(f.getName(), f.getId());
        }).collect(Collectors.toList());
        return resultList;
    }

    @Override
    protected BatchProviderMeta getBatchProviderMeta(Map metaMap) {
        BatchProviderMeta batchProviderMeta = DTOUtils.newInstance(BatchProviderMeta.class);
        //设置主DTO和关联DTO需要转义的字段名
        batchProviderMeta.setEscapeFiled("name");
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
        relationIds = relationIds.stream().distinct().collect(Collectors.toList());
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setIds(relationIds);
        categoryDTO.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        return assetsRpc.list(categoryDTO).getData();
    }
}
