package com.dili.trading.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.BatchProviderMeta;
import com.dili.ss.metadata.FieldMeta;
import com.dili.ss.metadata.ValuePair;
import com.dili.ss.metadata.ValuePairImpl;
import com.dili.ss.metadata.provider.BatchDisplayTextProviderSupport;
import com.dili.trading.constants.TradingConstans;
import com.dili.uap.sdk.domain.DataDictionaryValue;
import com.dili.uap.sdk.rpc.DataDictionaryRpc;

/**
 * 车类型模糊匹配
 */

@Component
@Scope("prototype")
public class TradeTypeProvider extends BatchDisplayTextProviderSupport {

	@Autowired
	private DataDictionaryRpc ddRpc;

	@Override
	public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
		BaseOutput<List<DataDictionaryValue>> output = this.ddRpc.listDataDictionaryValueByDdCode(TradingConstans.DD_CODE_TRADE_TYPE);
		if (!output.isSuccess()) {
			return null;
		}
		List<ValuePair<?>> list = new ArrayList<>(output.getData().size());
		output.getData().forEach(v -> list.add(new ValuePairImpl<String>(v.getName(), v.getCode())));
		return list;
	}

	@Override
	protected BatchProviderMeta getBatchProviderMeta(Map metaMap) {
		BatchProviderMeta batchProviderMeta = DTOUtils.newInstance(BatchProviderMeta.class);
		// 设置主DTO和关联DTO需要转义的字段名
		batchProviderMeta.setEscapeFiled("tradeType");
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
		BaseOutput<List<DataDictionaryValue>> output = this.ddRpc.listDataDictionaryValueByDdCode(TradingConstans.DD_CODE_TRADE_TYPE);
		if (!output.isSuccess()) {
			return null;
		}
		return output.getData();
	}
}
