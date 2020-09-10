package com.dili.trading.controller;

import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.domain.ComprehensiveFee;
import com.dili.orders.domain.ComprehensiveFeeType;
import com.dili.orders.dto.AccountSimpleResponseDto;
import com.dili.orders.rpc.CardRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.rpc.ComprehensiveFeeRpc;
import com.dili.trading.service.ComprehensiveFeeService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.rpc.FirmRpc;
import com.dili.uap.sdk.rpc.UserRpc;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Description: 查询收费功能Controller类
 *
 * @date:    2020/8/21
 * @author:   Seabert.Zhan
 */
@Controller
@RequestMapping("/queryFee")
public class QueryFeeController {

    @Autowired
    private ComprehensiveFeeRpc comprehensiveFeeRpc;

    @Autowired
    private ComprehensiveFeeService comprehensiveFeeService;

    @Autowired
    private CardRpc cardRpc;

    @Autowired
    private FirmRpc firmRpc;

    @Autowired
    private CustomerRpc customerRpc;
    @Autowired
    private UserRpc useRpc;

    /**
     * 跳转到列表页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/list.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        modelMap.put("operatorTimeStart", LocalDate.now() + " 00:00:00");
        modelMap.put("operatorTimeEnd", LocalDate.now() + " 23:59:59");
        return "queryFee/list";
    }



    /**
     * 分页查询
     *
     * @param comprehensiveFee
     * @return
     */
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listPage(ComprehensiveFee comprehensiveFee) {
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
        if (CollectionUtils.isNotEmpty(ranges)) {
            String value = (String) ranges.get(0).get("value");
            //如果value为0，则为个人
            if (value.equals("0")) {
                comprehensiveFee.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
            }
        }
        return comprehensiveFeeRpc.listPage(comprehensiveFee);
    }

    /**
     * 根据参数查询数据
     *
     * @param comprehensiveFee
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listByQueryParams.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listByQueryParams(ComprehensiveFee comprehensiveFee) throws Exception {
        //拿到数据权限，个人或全部
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
        if (CollectionUtils.isNotEmpty(ranges)) {
            String value = (String) ranges.get(0).get("value");
            //如果value为0，则为个人
            if (value.equals("0")) {
                comprehensiveFee.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
            }
        }
        comprehensiveFee.setOrderType(ComprehensiveFeeType.QUERY_CHARGE.getValue());
        PageOutput<List<ComprehensiveFee>> output = comprehensiveFeeRpc.listByQueryParams(comprehensiveFee);
        return new EasyuiPageOutput(output.getTotal(), ValueProviderUtils.buildDataByProvider(comprehensiveFee, output.getData())).toString();
    }

    /**
     * 检查询收费单新增页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/add.html", method = RequestMethod.GET)
    public String add(ModelMap modelMap) {
        return "queryFee/add";
    }

    /**
     * 跳转到查询收费输入密码页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/verificationUsernamePassword.action", method = RequestMethod.GET)
    public String verificationUsernamePassword(ModelMap modelMap, Long id) {
        BaseOutput oneById = comprehensiveFeeRpc.getOneById(id);
        modelMap.put("comprehensiveFee", oneById.getData());
        Double chargeAmountView = ((ComprehensiveFee)oneById.getData()).getChargeAmount().doubleValue()/100;
        modelMap.put("chargeAmountView", String.format("%.2f", chargeAmountView));
        return "queryFee/verificationUsernamePassword";
    }

    /**
     * 查询收费缴费
     *
     * @return BaseOutput
     */
    @RequestMapping(value = "/pay.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput pay(Long id, String password) {
        return comprehensiveFeeService.pay(id, password);
    }

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
        return oneAccountCard;
    }

    /**
     * 新增comprehensiveFee
     *
     * @param comprehensiveFee
     * @return BaseOutput
     */
    @RequestMapping(value = "/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput insert(ComprehensiveFee comprehensiveFee) {
        String tips = checkUpDate(comprehensiveFee);
        if(StringUtils.isNotBlank(tips)){
            BaseOutput<ComprehensiveFee> result = new BaseOutput<ComprehensiveFee>();
            result.setCode("500");
            result.setMessage(tips);
            return result;
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        comprehensiveFee.setMarketId(userTicket.getFirmId());
        comprehensiveFee.setOrderType(ComprehensiveFeeType.QUERY_CHARGE.getValue());
        return comprehensiveFeeService.insertComprehensiveFee(comprehensiveFee);
    }

    /**
     * 校验comprehensiveFee
     * @param comprehensiveFee
     * @return
     */
    public String  checkUpDate(ComprehensiveFee comprehensiveFee){
        StringBuffer tips = new StringBuffer();
        if (StringUtils.isBlank(comprehensiveFee.getCustomerCardNo())) {
            tips.append(",卡号不能为空");
        } else {
            if (comprehensiveFee.getCustomerId() == null) {
                tips.append(",客户不存在或者卡号出错请联系管理员");
            }
        }
        if (tips.length() != 0) {
            tips.append("!");
            return tips.substring(1);
        }
        return  "";
    }
}
