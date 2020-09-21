package com.dili.trading.controller;

import com.dili.orders.domain.TransitionDepartureSettlement;
import com.dili.orders.dto.AccountSimpleResponseDto;
import com.dili.orders.rpc.CardRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.ss.util.MoneyUtils;
import com.dili.trading.rpc.TransitionDepartureSettlementRpc;
import com.dili.trading.service.TransitionDepartureSettlementService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

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
        BaseOutput<AccountSimpleResponseDto> oneAccountCard = cardRpc.getOneAccountCard(customerCardNo);
        if (oneAccountCard.isSuccess()) {
            if (!Objects.equals(oneAccountCard.getData().getAccountInfo().getFirmId(), SessionContext.getSessionContext().getUserTicket().getFirmId())) {
                return BaseOutput.failure("未获取到相关客户信息");
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("balance", oneAccountCard.getData().getAccountFund().getAvailableAmount().doubleValue() / 100);
            map.put("customerCode", oneAccountCard.getData().getAccountInfo().getCustomerCode());
            map.put("customerId", oneAccountCard.getData().getAccountInfo().getCustomerId());
            map.put("customerName", oneAccountCard.getData().getAccountInfo().getCustomerName());
            return BaseOutput.successData(map);
        }
        return oneAccountCard;
    }


    /**
     * 跳转到转离场审批单页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/list.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        //默认选择当天的数据，页面上也需要展示
        modelMap.put("beginDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        modelMap.put("endDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
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
    @RequestMapping(value = "/verificationUsernamePassword.action", method = RequestMethod.GET)
    public String verificationUsernamePassword(ModelMap modelMap, Long id) {
        //返回页面用map封装，因为不好分转元
        BaseOutput<TransitionDepartureSettlement> oneById = transitionDepartureSettlementRpc.getOneById(id);
        modelMap.put("id", oneById.getData().getId());
        modelMap.put("chargeAmount", MoneyUtils.centToYuan(oneById.getData().getChargeAmount()));
        modelMap.put("customerName", oneById.getData().getCustomerName());
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
        if (CollectionUtils.isNotEmpty(ranges)) {
            String value = (String) ranges.get(0).get("value");
            //如果value为0，则为个人
            if (value.equals("0")) {
                transitionDepartureSettlement.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
            }
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
        transitionDepartureSettlement.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        if (CollectionUtils.isNotEmpty(ranges)) {
            String value = (String) ranges.get(0).get("value");
            //如果value为0，则为个人
            if (value.equals("0")) {
                transitionDepartureSettlement.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
            }
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
        //判断金额和净重是否都大于0
        if (transitionDepartureSettlement.getNetWeight() < 0) {
            return BaseOutput.failure("净重不能小于0");
        }
        if (transitionDepartureSettlement.getChargeAmount() < 0) {
            return BaseOutput.failure("缴费金额不能小于0");
        }
        //新增的时候设置市场id
        transitionDepartureSettlement.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
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
        //判断金额和净重是否都大于0
        if (transitionDepartureSettlement.getNetWeight() < 0) {
            return BaseOutput.failure("净重不能小于0");
        }
        if (transitionDepartureSettlement.getChargeAmount() < 0) {
            return BaseOutput.failure("缴费金额不能小于0");
        }
        return transitionDepartureSettlementRpc.update(transitionDepartureSettlement, SessionContext.getSessionContext().getUserTicket().getFirmId());
    }


    /**
     * 撤销TransitionDepartureSettlement
     *
     * @return BaseOutput
     */
    @RequestMapping(value = "/revocator.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput revocator(Long id, String password) {
        if (Objects.isNull(id)) {
            return BaseOutput.failure("该卡号不能进行操作");
        }
        //通过用户密码去uap验证，暂未对接
        return transitionDepartureSettlementService.revocator(transitionDepartureSettlementRpc.getOneById(id).getData(), password);
    }

    /**
     * 缴费TransitionDepartureSettlement
     *
     * @return BaseOutput
     */
    @RequestMapping(value = "/pay.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput pay(Long id, String password) {
        if (Objects.isNull(id)) {
            return BaseOutput.failure("结算单id不能为空");
        }
        if (StringUtils.isBlank(password)) {
            return BaseOutput.failure("密码不能为空");
        }
        return transitionDepartureSettlementService.pay(id, password);
    }

    /**
     * 获取一个TransitionDepartureSettlement单
     *
     * @return BaseOutput
     */
    @RequestMapping(value = "/getOneById.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput getOneById(Long id) {
        return transitionDepartureSettlementRpc.getOneById(id);
    }


    /**
     * 对接计费规则
     *
     * @return
     */
    @RequestMapping(value = "/fee.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput getFee(BigDecimal netWeight, Long id, Long carTypeId) {
        if (Objects.isNull(netWeight)) {
            return BaseOutput.failure("重量不能为空");
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        return transitionDepartureSettlementRpc.getFee(netWeight, userTicket.getFirmId(), userTicket.getDepartmentId(), id, carTypeId);
    }
}
