package com.dili.trading.rpc;


import com.dili.ss.domain.BaseOutput;
import com.dili.trading.domain.AccountSimpleResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 根据卡号获取账户信息
 */
@FeignClient(name = "dili-card", contextId = "card", url = "10.28.1.185")
public interface CardRpc {
    /**
     * 根据卡号获取账户信息
     *
     * @param cardNo
     * @return
     */
    @GetMapping("/accountQuery/simpleInfo.action")
    BaseOutput<AccountSimpleResponseDto> getOneAccountCard(@RequestParam(value = "cardNo") String cardNo);
}
