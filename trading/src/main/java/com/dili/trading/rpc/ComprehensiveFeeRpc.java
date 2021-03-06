package com.dili.trading.rpc;

import com.dili.orders.domain.ComprehensiveFee;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * 检测收费RPC
 *
 * @author Henry.Huang
 * @date 2020/08/20
 */
@FeignClient(name = "order-service", contextId = "comprehensiveFeeRpc", url = "${orderService.url:}")
public interface ComprehensiveFeeRpc {


    /**
     * 分页查询
     *
     * @param comprehensiveFee
     * @return String
     * @throws Exception
     */
    @RequestMapping(value = "/api/comprehensiveFee/listPage", method = {RequestMethod.POST})
    String listPage(@RequestBody ComprehensiveFee comprehensiveFee);


    /**
     * 根据参数查询数据
     *
     * @param comprehensiveFee
     * @return String
     * @throws Exception
     */
    @RequestMapping(value = "/api/comprehensiveFee/listByQueryParams", method = {RequestMethod.POST})
    PageOutput<List<ComprehensiveFee>> listByQueryParams(ComprehensiveFee comprehensiveFee);

    /**
     * 根据参数查询交易总数和交易总额
     *
     * @param comprehensiveFee
     * @return String
     * @throws Exception
     */
    @RequestMapping(value = "/api/comprehensiveFee/selectCountAndTotal", method = {RequestMethod.POST})
    BaseOutput<ComprehensiveFee> selectCountAndTotal(ComprehensiveFee comprehensiveFee);

    /**
     * 新增comprehensiveFee
     *
     * @param comprehensiveFee
     * @return BaseOutput
     */
    @RequestMapping(value = "/api/comprehensiveFee/insert", method = {RequestMethod.POST})
    BaseOutput<ComprehensiveFee> insert(@RequestBody ComprehensiveFee comprehensiveFee);

    /**
     * 根据id查询单据
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/comprehensiveFee/getOneById/{id}", method = {RequestMethod.GET})
    BaseOutput<ComprehensiveFee> getOneById(@PathVariable(value = "id") Long id);

    /**
     * 检测收费单支付
     *
     * @param id
     * @param password
     * @param marketId
     * @param operatorId
     * @param operatorName
     * @param operatorUserName
     * @return
     */
    @RequestMapping(value = "/api/comprehensiveFee/pay", method = {RequestMethod.POST})
    BaseOutput<ComprehensiveFee> pay(@RequestParam(value = "id") Long id, @RequestParam(value = "password") String password, @RequestParam(value = "marketId") Long marketId, @RequestParam(value = "operatorId") Long operatorId, @RequestParam(value = "operatorName") String operatorName, @RequestParam(value = "operatorUserName") String operatorUserName);

    /**
     * 获取费用信息
     *
     * @param marketId
     * @param customerId
     * @param type
     * @return
     */
    @RequestMapping(value = "/api/comprehensiveFee/fee", method = {RequestMethod.POST})
    BaseOutput<?> getFee(@RequestParam(value = "marketId") Long marketId, @RequestParam(value = "customerId") Long customerId, @RequestParam(value = "type") String type);

    /**
     * 撤销调用
     *
     * @param comprehensiveFee
     * @param realName
     * @param operatorId
     * @param operatorPassword
     * @param userName
     * @return
     */
    @RequestMapping(value = "/api/comprehensiveFee/revocator", method = {RequestMethod.POST})
    BaseOutput<ComprehensiveFee> revocator(@RequestBody ComprehensiveFee comprehensiveFee, @RequestParam(value = "realName") String realName, @RequestParam(value = "operatorId") Long operatorId, @RequestParam(value = "operatorPassword") String operatorPassword, @RequestParam(value = "userName") String userName);

    /**
     * 获取费用信息
     *
     * @return
     */
    @RequestMapping(value = "/api/comprehensiveFee/scheduleUpdate", method = {RequestMethod.POST})
    BaseOutput<String> scheduleUpdate();
}
