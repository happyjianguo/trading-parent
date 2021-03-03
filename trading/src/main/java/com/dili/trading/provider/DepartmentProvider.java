package com.dili.trading.provider;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.metadata.BatchProviderMeta;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.provider.BatchDisplayTextProviderSupport;
import com.dili.uap.sdk.domain.Department;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.rpc.DepartmentRpc;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限部门
 */
@Component
@Scope("prototype")
public class DepartmentProvider extends BatchDisplayTextProviderSupport {

    @Autowired
    private DepartmentRpc departmentRpc;

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<List<Department>> listBaseOutput = departmentRpc.listUserAuthDepartmentByFirmId(userTicket.getId(), userTicket.getFirmId());
        if (!listBaseOutput.isSuccess() || CollectionUtils.isEmpty(listBaseOutput.getData())) {
            return null;
        }
        List<ValuePair<?>> resultList = listBaseOutput.getData().stream().map(f -> {
            return (ValuePair<?>) new ValuePairImpl(f.getName(), f.getId());
        }).collect(Collectors.toList());
        return resultList;
    }


    @Override
    protected BatchProviderMeta getBatchProviderMeta(Map metaMap) {
        return null;
    }

    @Override
    protected List getFkList(List<String> relationIds, Map metaMap) {
        return null;
    }


}
