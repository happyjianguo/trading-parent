package com.dili.trading.controller;

import com.dili.order.domain.TransitionDepartureSettlement;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.TransitionDepartureSettlementRpc;
import com.dili.trading.service.TransitionDepartureSettlementService;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 结算单接口
 */
@Controller
@RequestMapping("/transitionDepartureSettlement")
public class TransitionDepartureSettlementController {

    @Autowired
    private TransitionDepartureSettlementRpc transitionDepartureSettlementRpc;

    @Autowired
    private TransitionDepartureSettlementService transitionDepartureSettlementService;

    /**
     * 跳转到转离场审批单页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/list.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "transitionDepartureSettlement/list";
    }

    /**
     * 分页查询
     *
     * @param transitionDepartureSettlement
     * @return
     */
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listPage(TransitionDepartureSettlement transitionDepartureSettlement) {
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
        String value = (String) ranges.get(0).get("value");
        //如果value为0，则为个人
        if (value.equals("0")) {
            transitionDepartureSettlement.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
        }
        return transitionDepartureSettlementRpc.listPage(transitionDepartureSettlement);
    }

    /**
     * 根据参数查询数据
     *
     * @param transitionDepartureSettlement
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listByQueryParams.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listByQueryParams(TransitionDepartureSettlement transitionDepartureSettlement) {
        //拿到数据权限，个人或全部
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
        String value = (String) ranges.get(0).get("value");
        //如果value为0，则为个人
        if (value.equals("0")) {
            transitionDepartureSettlement.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
        }
        return transitionDepartureSettlementRpc.listByQueryParams(transitionDepartureSettlement);
    }

    /**
     * 新增TransitionDepartureSettlement
     * 需要调用卡务，暂未接入
     *
     * @param transitionDepartureSettlement
     * @return BaseOutput
     */
    @RequestMapping(value = "/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput insert(TransitionDepartureSettlement transitionDepartureSettlement) {
        return transitionDepartureSettlementService.insert(transitionDepartureSettlement);
    }


    /**
     * 撤销TransitionDepartureSettlement
     * 需要调用卡务，暂未接入
     *
     * @param transitionDepartureSettlement
     * @return BaseOutput
     */
    @RequestMapping(value = "/revocator.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput revocator(TransitionDepartureSettlement transitionDepartureSettlement) {
        return transitionDepartureSettlementService.revocator(transitionDepartureSettlement);
    }

    /**
     * 缴费TransitionDepartureSettlement
     * 需要调用卡务，暂未接入
     *
     * @param transitionDepartureSettlement
     * @return BaseOutput
     */
    @RequestMapping(value = "/pay.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput pay(TransitionDepartureSettlement transitionDepartureSettlement) {
        return transitionDepartureSettlementService.pay(transitionDepartureSettlement);
    }

}
