package com.dili.trading.rpc;

import com.dili.ss.domain.BaseOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "dili-uid", contextId = "uidRpc")
public interface UidRpc {

    @RequestMapping(value = "/api/bizNumber/sid53", method = RequestMethod.GET)
    BaseOutput<String> getFirmCode();

}