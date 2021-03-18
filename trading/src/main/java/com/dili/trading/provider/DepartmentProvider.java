package com.dili.trading.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.BatchProviderMeta;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.provider.BatchDisplayTextProviderSupport;
import com.dili.uap.sdk.domain.Department;
import com.dili.uap.sdk.domain.dto.DepartmentDto;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.rpc.DepartmentRpc;
import com.dili.uap.sdk.session.SessionContext;

/**
 * 权限部门
 */
@Component
public class DepartmentProvider extends BatchDisplayTextProviderSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentProvider.class);

	@Autowired
	private DepartmentRpc departmentRpc;

	@Override
	public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
		List<Map> deptDataAuths = SessionContext.getSessionContext().dataAuth(DataAuthType.DEPARTMENT.getCode());
		if (CollectionUtils.isEmpty(deptDataAuths)) {
			return null;
		}
		List<String> departmentIds = new ArrayList<String>(deptDataAuths.size());
		deptDataAuths.forEach(da -> departmentIds.add(da.get("value").toString()));
		DepartmentDto query = DTOUtils.newInstance(DepartmentDto.class);
		query.setIds(departmentIds);
		BaseOutput<List<Department>> listBaseOutput = this.departmentRpc.listByExample(query);
		if (!listBaseOutput.isSuccess()) {
			LOGGER.error(listBaseOutput.getMessage());
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
