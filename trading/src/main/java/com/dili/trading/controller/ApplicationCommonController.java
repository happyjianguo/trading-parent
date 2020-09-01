package com.dili.trading.controller;

import cn.hutool.core.util.IdUtil;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.redis.service.RedisUtil;
import com.dili.trading.common.CacheKey;
import com.dili.uap.sdk.domain.DataDictionaryValue;
import com.dili.uap.sdk.rpc.DataDictionaryRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: miaoguoxin
 * @Date: 2020/7/7 09:06
 * @Description:
 */
@Controller
@RequestMapping("/appCommon")
public class ApplicationCommonController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DataDictionaryRpc dataDictionaryRpc;

    /**
     * 获取防重复提交的token
     *
     * @author miaoguoxin
     * @date 2020/7/7
     */
    @GetMapping("/duplicateToken.action")
    @ResponseBody
    public BaseOutput<String> getForbidDuplicateToken() {
        String id = IdUtil.fastSimpleUUID();
        String key = CacheKey.FORBID_DUPLICATE_TOKEN_PREFIX + id;
        redisUtil.increment(key, 1L);
        redisUtil.expire(key, 30, TimeUnit.MINUTES);
        return BaseOutput.successData(id);
    }

    /**
     * 获取字典列表
     *
     * @author miaoguoxin
     * @date 2020/7/30
     */
    @GetMapping("/dictList.action")
    @ResponseBody
    public BaseOutput<List<DataDictionaryValue>> getDataDictList(String code) {
        return dataDictionaryRpc.listDataDictionaryValueByDdCode(code);
    }
}
