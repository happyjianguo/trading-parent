package com.dili.trading.rpc;

import com.dili.ss.domain.BaseOutput;
import com.dili.trading.dto.TraceTradeBillResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @Auther: miaoguoxin
 * @Date: 2020/11/6 14:16
 * @Description: 溯源rpc
 */
@FeignClient(name = "quality-trace", contextId = "qualityTraceRpc", url = "${qualityTrace.url}")
public interface QualityTraceRpc {

    /**
    *
    * @author miaoguoxin
    * @date 2020/11/6
    */
    @PostMapping(value = "/api/qualityTraceTradeBillApi/queryByOrderIdList.api")
    BaseOutput<List<TraceTradeBillResponseDto>> queryByOrderIdList(List<Long> orderIds);
}
