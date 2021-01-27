package com.dili.trading.rpc;

import com.dili.orders.domain.CollectionRecord;
import com.dili.ss.domain.PageOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * 回款记录相关rpc
 */
@FeignClient(name = "order-service", contextId = "collectionRecordRpc", url = "${orderService.url:}")
public interface CollectionRecordRpc {

    /**
     * 根据参数查询数据
     *
     * @param collectionRecord
     * @return String
     */
    @RequestMapping(value = "/api/collectionRecord/listByQueryParams", method = {RequestMethod.POST})
    PageOutput<List<CollectionRecord>> listByQueryParams(@RequestBody CollectionRecord collectionRecord);
}
