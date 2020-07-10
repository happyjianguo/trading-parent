package com.dili.trading.controller;

import com.dili.orders.domain.TransitionDepartureSettlement;
import com.dili.orders.rpc.AccountRpc;
import com.dili.orders.rpc.CardRpc;
import com.dili.orders.rpc.PayRpc;
import com.dili.rule.sdk.domain.input.QueryFeeInput;
import com.dili.rule.sdk.rpc.ChargeRuleRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.rpc.TransitionDepartureSettlementRpc;
import com.dili.trading.service.TransitionDepartureSettlementService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
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

    @Autowired
    private ChargeRuleRpc chargeRuleRpc;

    @Autowired
    private AccountRpc accountRpc;

    @Autowired
    private PayRpc payRpc;

    @Autowired
    private CardRpc cardRpc;

    /**
     * 根据卡号获取账户余额
     *
     * @param customerCardNo
     * @return
     */
    @RequestMapping(value = "/queryAccountBalance.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput queryAccountBalance(String customerCardNo) {
        return cardRpc.getOneAccountCard(customerCardNo);
    }


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
     * 跳转到转离场申请单新增页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/add.html", method = RequestMethod.GET)
    public String add(ModelMap modelMap) {
        return "transitionDepartureSettlement/add";
    }


    /**
     * 跳转到转离场输入密码页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/verificationUsernamePassword.html", method = RequestMethod.GET)
    public String verificationUsernamePassword(ModelMap modelMap, Long id) {
        BaseOutput oneById = transitionDepartureSettlementRpc.getOneById(id);
        modelMap.put("transitionDepartureSettlement", oneById.getData());
        return "transitionDepartureSettlement/verificationUsernamePassword";
    }

    /**
     * 跳转到转离场输入密码页面，撤销操作
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/revocatorPage.html", method = RequestMethod.GET)
    public String revocatorPage(ModelMap modelMap, Long id) {
        BaseOutput oneById = transitionDepartureSettlementRpc.getOneById(id);
        modelMap.put("transitionDepartureSettlement", oneById.getData());
        modelMap.put("userTicket", SessionContext.getSessionContext().getUserTicket());
        return "transitionDepartureSettlement/revocatorPage";
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
    public String listByQueryParams(TransitionDepartureSettlement transitionDepartureSettlement) throws Exception {
        //拿到数据权限，个人或全部
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
        String value = (String) ranges.get(0).get("value");
        //如果value为0，则为个人
        if (value.equals("0")) {
            transitionDepartureSettlement.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
        }

        PageOutput<List<TransitionDepartureSettlement>> output = transitionDepartureSettlementRpc.listByQueryParams(transitionDepartureSettlement);
        return new EasyuiPageOutput(output.getTotal(), ValueProviderUtils.buildDataByProvider(transitionDepartureSettlement, output.getData())).toString();
    }

    /**
     * 新增TransitionDepartureSettlement
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
     * 更新TransitionDepartureSettlement
     *
     * @param transitionDepartureSettlement
     * @return BaseOutput
     */
    @RequestMapping(value = "/update.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput update(TransitionDepartureSettlement transitionDepartureSettlement) {
        return transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
    }


    /**
     * 撤销TransitionDepartureSettlement
     *
     * @return BaseOutput
     */
    @RequestMapping(value = "/revocator.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput revocator(Long id, String password) {
        //通过用户密码去uap验证，暂未对接
        return transitionDepartureSettlementService.revocator(transitionDepartureSettlementRpc.getOneById(id).getData());
    }

    /**
     * 缴费TransitionDepartureSettlement
     *
     * @return BaseOutput
     */
    @RequestMapping(value = "/pay.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput pay(Long id, String password) {
        return transitionDepartureSettlementService.pay(id, password);
    }

    /**
     * 对接计费规则
     *
     * @return
     */
    @RequestMapping(value = "/fee.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput getFee(BigDecimal netWeight) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        return transitionDepartureSettlementRpc.getFee(netWeight, userTicket.getFirmId(), userTicket.getDepartmentId());
    }
}
