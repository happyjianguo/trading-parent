package com.dili.trading.rpc;

import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.orders.constants.OrdersConstant;
import com.dili.orders.domain.CollectionRecord;
import com.dili.orders.dto.WeighingCollectionStatementDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 回款记录相关rpc
 */
@FeignClient(name = "order-service", contextId = "collectionRecordRpc", url = "localhost:8185")
public interface CollectionRecordRpc {

    /**
     * 根据参数查询数据
     *
     * @param collectionRecord
     * @return String
     */
    @RequestMapping(value = "/api/collectionRecord/listByQueryParams", method = {RequestMethod.POST})
    PageOutput<List<CollectionRecord>> listByQueryParams(@RequestBody CollectionRecord collectionRecord);

    /**
     * 回款总和
     *
     * @param collectionRecord
     * @return
     */
    @PostMapping("/api/collectionRecord/groupListForDetail")
    BaseOutput<List<Map<String, String>>> groupListForDetail(@RequestBody CollectionRecord collectionRecord);

    /**
     * 回款下钻
     *
     * @param collectionRecord
     * @return
     */
    @RequestMapping(value = "/api/collectionRecord/weighingBills", method = {RequestMethod.POST})
    BaseOutput<List<WeighingCollectionStatementDto>> weighingBills(@RequestBody CollectionRecord collectionRecord);

    /**
     * 根据数据插入，并且支付
     *
     * @param collectionRecord
     * @return
     */
    @PostMapping("/api/collectionRecord/insertAndPay")
    BaseOutput insertAndPay(@RequestBody CollectionRecord collectionRecord, @RequestParam(value = "password") String password);
}
