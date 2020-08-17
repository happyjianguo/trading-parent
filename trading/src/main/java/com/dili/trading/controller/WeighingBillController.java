package com.dili.trading.controller;

import java.util.*;

import com.dili.orders.dto.*;
import com.dili.orders.rpc.AccountRpc;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.customer.sdk.domain.Customer;
import com.dili.customer.sdk.domain.dto.CustomerQueryInput;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.constants.OrdersConstant;
import com.dili.orders.constants.TradingConstans;
import com.dili.orders.rpc.CardRpc;
import com.dili.orders.rpc.CategoryRpc;
import com.dili.orders.rpc.PayRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.dto.WeighingBillSaveAndSettleDto;
import com.dili.trading.rpc.WeighingBillRpc;
import com.dili.uap.sdk.domain.DataDictionaryValue;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.domain.dto.UserQuery;
import com.dili.uap.sdk.rpc.DataDictionaryRpc;
import com.dili.uap.sdk.rpc.FirmRpc;
import com.dili.uap.sdk.rpc.UserRpc;
import com.dili.uap.sdk.session.SessionContext;

/**
 * WeighingBillController
 */
@Controller
@RequestMapping("/weighingBill")
public class WeighingBillController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeighingBillController.class);
    @Autowired
    private WeighingBillRpc weighingBillRpc;
    @Autowired
    private CategoryRpc categoryRpc;
    @Autowired
    private FirmRpc firmRpc;
    @Autowired
    private CardRpc cardRpc;
    @Autowired
    private CustomerRpc customerRpc;
    @Autowired
    private UserRpc useRpc;
    @Autowired
    private PayRpc payRpc;
    @Autowired
    private DataDictionaryRpc dataDictionaryRpc;
    @Autowired
    private AccountRpc accountRpc;

    /**
     * 列表页
     *
     * @return
     */
    @GetMapping("/index.html")
    public String index() {
        return "weighingBill/index";
    }

    /**
     * 结算
     *
     * @param weighingBill 过磅单和卖家密码数据
     * @return
     */
    @ResponseBody
    @PostMapping("/saveAndSettle.action")
    public BaseOutput<?> saveAndSettle(@RequestBody WeighingBillSaveAndSettleDto weighingBill) {
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        if (user == null) {
            return BaseOutput.failure("用户未登录");
        }
        AccountPasswordValidateDto dto = new AccountPasswordValidateDto();
        dto.setAccountId(weighingBill.getBuyerAccount());
        dto.setPassword(weighingBill.getBuyerPassword());
        BaseOutput<?> output = this.payRpc.validateAccountPassword(dto);
        if (!output.isSuccess()) {
            return output;
        }
        if (StringUtils.isBlank(weighingBill.getSerialNo())) {
            weighingBill.setCreatorId(user.getId());
            output = this.weighingBillRpc.add(weighingBill);
            if (!output.isSuccess()) {
                return output;
            }
            output = this.weighingBillRpc.settle(output.getData().toString(), weighingBill.getBuyerPassword(), user.getId());
        } else {
            weighingBill.setModifierId(user.getId());
            output = this.weighingBillRpc.update(weighingBill);
            if (!output.isSuccess()) {
                return output;
            }
            output = this.weighingBillRpc.settle(weighingBill.getSerialNo(), weighingBill.getBuyerPassword(), user.getId());
        }
        return output;
    }

    /**
     * 撤销
     *
     * @param serialNo
     * @param buyerPassword
     * @param sellerPassword
     * @return
     */
    @ResponseBody
    @PostMapping("/withdraw.action")
    public BaseOutput<Object> withdraw(String serialNo, String buyerPassword, String sellerPassword) {
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        if (user == null) {
            return BaseOutput.failure("用户未登录");
        }
        return this.weighingBillRpc.withdraw(serialNo, buyerPassword, sellerPassword, user.getId());
    }

    /**
     * 作废
     *
     * @param serialNo
     * @param buyerPassword
     * @param sellerPassword
     * @return
     */
    @ResponseBody
    @PostMapping("/invalidate.action")
    public BaseOutput<Object> invalidate(String serialNo, String buyerPassword, String sellerPassword) {
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        if (user == null) {
            return BaseOutput.failure("用户未登录");
        }
        return this.weighingBillRpc.invalidate(serialNo, buyerPassword, sellerPassword, user.getId());
    }

    /**
     * 查询列表
     *
     * @param dto
     * @return
     */
    @ResponseBody
    @PostMapping("/listByExample.action")
    public BaseOutput<Object> listByExample(@RequestBody WeighingBillQueryDto dto) {
        return this.weighingBillRpc.listByExample(dto);
    }

    /**
     * 根据快捷吗查询商品
     *
     * @param keyword 快捷码
     * @return
     */
    @ResponseBody
    @RequestMapping("/getGoodsByKeyword.action")
    public BaseOutput<List<CategoryDTO>> getGoodsByKeyword(String keyword) {
        BaseOutput<Firm> output = this.firmRpc.getByCode(TradingConstans.SHOUGUANG_FIRM_CODE);
        if (!output.isSuccess()) {
            LOGGER.error(output.getMessage());
            return BaseOutput.failure("查询市场信息失败");
        }
        if (output.getData() == null) {
            return BaseOutput.failure("市场信息不存在");
        }
        CategoryDTO query = new CategoryDTO();
        query.setMarketId(output.getData().getId());
        query.setKeyword(keyword);
        return this.categoryRpc.getTree(query);
    }

    /**
     * 根据卡号查询客户信息
     *
     * @param cardNo 卡号
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getCustomerInfoByCardNo.action")
    public BaseOutput<AccountSimpleResponseDto> getCustomerInfoByCardNo(String cardNo) {
        BaseOutput<AccountSimpleResponseDto> output = this.cardRpc.getOneAccountCard(cardNo);
        return output;
    }

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    @ResponseBody
    @PostMapping("/listPage.action")
    public String listPage(WeighingBillQueryDto query) {
        PageOutput<List<WeighingBillListPageDto>> output = this.weighingBillRpc.listPage(query);

        Map<Object, Object> metadata = new HashMap<Object, Object>();
        metadata.put("roughWeight", "weightProvider");
        metadata.put("netWeight", "weightProvider");
        metadata.put("unitWeight", "weightProvider");
        metadata.put("roughWeight", "weightProvider");

        metadata.put("unitPrice", "moneyProvider");
        metadata.put("statement.tradeAmount", "moneyProvider");
        metadata.put("statement.buyerPoundage", "moneyProvider");
        metadata.put("statement.buyerActualAmount", "moneyProvider");
        metadata.put("statement.sellerPoundage", "moneyProvider");
        metadata.put("statement.sellerActualAmount", "moneyProvider");

        metadata.put("operationRecord.operationTime", "datetimeProvider");
        metadata.put("state", "weighingBillStateProvider");

        JSONObject ddProvider = new JSONObject();
        ddProvider.put(ValueProvider.PROVIDER_KEY, "dataDictionaryValueProvider");
        ddProvider.put(ValueProvider.QUERY_PARAMS_KEY, "{\"dd_code\":\"trade_type\"}");
        metadata.put("tradeType", ddProvider);

        query.setMetadata(metadata);
        try {
            List<Map> list = ValueProviderUtils.buildDataByProvider(query, output.getData());

            return new EasyuiPageOutput(output.getTotal(), list).toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * 查询客户信息
     *
     * @param name
     * @param keyword
     * @return
     */
    @ResponseBody
    @RequestMapping("/listCustomerByKeyword.action")
    public BaseOutput<?> listCustomerByKeyword(String name, String keyword) {
        CustomerQueryInput cq = new CustomerQueryInput();
        cq.setKeyword(name);
        BaseOutput<Firm> firmOutput = this.firmRpc.getByCode(TradingConstans.SHOUGUANG_FIRM_CODE);
        if (!firmOutput.isSuccess()) {
            return firmOutput;
        }
        cq.setMarketId(firmOutput.getData().getId());
        //json 修改开始
        //customer那边没有卡号的冗余，直接走卡余额查询，应该可以满足
        //通过卡号获取账户
//        BaseOutput<UserAccountCardResponseDto> oneAccountCard = accountRpc.getOneAccountCard(keyword);
//        List<UserAccountCardResponseDto> userAccountCardResponseDtos = new ArrayList<>();
//        userAccountCardResponseDtos.add(oneAccountCard.getData());
        BaseOutput<List<Customer>> output = this.customerRpc.list(cq);
        //json 修改结束
        return output;
    }

    /**
     * 查询客户信息
     *
     * @param name
     * @param keyword
     * @return
     */
    @ResponseBody
    @RequestMapping("/listCustomerByCardNo.action")
    public BaseOutput<?> listCustomerByCardNo(String cardNo) {
//        BaseOutput<AccountSimpleResponseDto> cardOutput = this.cardRpc.getOneAccountCard(cardNo);
//        if (!cardOutput.isSuccess()) {
//            return cardOutput;
//        }
//        CustomerQueryInput cq = new CustomerQueryInput();
//        cq.setId(cardOutput.getData().getAccountInfo().getCustomerId());
//        BaseOutput<Firm> firmOutput = this.firmRpc.getByCode(TradingConstans.SHOUGUANG_FIRM_CODE);
//        if (!firmOutput.isSuccess()) {
//            return firmOutput;
//        }
//        cq.setMarketId(firmOutput.getData().getId());
//        BaseOutput<List<Customer>> output = this.customerRpc.list(cq);
//        return output;
        //json修改开始
        BaseOutput<UserAccountCardResponseDto> oneAccountCard = this.accountRpc.getOneAccountCard(cardNo);
        if (!oneAccountCard.isSuccess()) {
            return oneAccountCard;
        }
        List<UserAccountCardResponseDto> userAccountCardResponseDtos = new ArrayList<>();
        userAccountCardResponseDtos.add(oneAccountCard.getData());
        return BaseOutput.successData(userAccountCardResponseDtos);
        //json修改结束

    }

    /**
     * 查询结算员信息
     *
     * @param name
     * @param keyword
     * @return
     */
    @ResponseBody
    @RequestMapping("/listOperatorByKeyword.action")
    public BaseOutput<?> listOperatorByKeyword(String name, String keyword) {
        UserQuery userQuery = DTOUtils.newInstance(UserQuery.class);
        userQuery.setKeyword(keyword);
        return this.useRpc.listByExample(userQuery);
    }

    /**
     * 详情页
     *
     * @param id
     * @param modelMap
     * @return
     */
    @GetMapping("/detail.html")
    public String detail(Long id, ModelMap modelMap) {
        BaseOutput<WeighingBillDetailDto> output = this.weighingBillRpc.findDetailDtoById(id);
        if (!output.isSuccess()) {
            LOGGER.error(output.getMessage());
            return this.index();
        }

        Map<Object, Object> metadata = new HashMap<Object, Object>();
        metadata.put("netWeight", "weightProvider");
        metadata.put("unitWeight", "weightProvider");
        metadata.put("roughWeight", "weightProvider");
        metadata.put("tareWeight", "weightProvider");
        metadata.put("createdTime", "datetimeProvider");
        metadata.put("measureType", "measureTypeProvider");

        metadata.put("unitPrice", "moneyProvider");
        metadata.put("statement.tradeAmount", "moneyProvider");
        metadata.put("statement.buyerPoundage", "moneyProvider");
        metadata.put("statement.buyerActualAmount", "moneyProvider");
        metadata.put("statement.sellerPoundage", "moneyProvider");
        metadata.put("statement.sellerActualAmount", "moneyProvider");

        JSONObject ddProvider = new JSONObject();
        ddProvider.put(ValueProvider.PROVIDER_KEY, "dataDictionaryValueProvider");
        ddProvider.put(ValueProvider.QUERY_PARAMS_KEY, "{\"dd_code\":\"trade_type\"}");
        metadata.put("tradeType", ddProvider);

        try {
            List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, Arrays.asList(output.getData()));
            metadata = new HashMap<Object, Object>();
            metadata.put("operationTime", "datetimeProvider");
            List<Map> recordsListMap = ValueProviderUtils.buildDataByProvider(metadata, output.getData().getRecords());
            list.get(0).put("records", recordsListMap);
            modelMap.addAttribute("model", list.get(0));
            return "weighingBill/detail";
        } catch (Exception e) {
            e.printStackTrace();
            return this.index();
        }

    }

    /**
     * 操作员撤销验证密码页面
     *
     * @param id
     * @param modelMap
     * @return
     */
    @GetMapping("/operatorInvalidate.html")
    public String validatePassword(Long id, ModelMap modelMap) {
        modelMap.addAttribute("weighingBillId", id).addAttribute("model", SessionContext.getSessionContext().getUserTicket()).addAttribute("submitHandler", "invalidateHandler");
        return "weighingBill/validatePassword";
    }

    /**
     * 操作员撤销
     *
     * @param id
     * @param operatorPassword
     * @param modelMap
     * @return
     */
    @ResponseBody
    @PostMapping("/operatorInvalidate.action")
    public BaseOutput<Object> operatorInvalidate(Long id, String operatorPassword, ModelMap modelMap) {
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<Object> output = this.weighingBillRpc.operatorInvalidate(id, user.getId(), operatorPassword);
        return output;
    }

    /**
     * 操作员作废密码验证页面
     *
     * @param id
     * @param modelMap
     * @return
     */
    @GetMapping("/operatorWithdraw.html")
    public String operatorWithdraw(Long id, ModelMap modelMap) {
        modelMap.addAttribute("weighingBillId", id).addAttribute("model", SessionContext.getSessionContext().getUserTicket()).addAttribute("submitHandler", "withdrawHandler");
        return "weighingBill/validatePassword";
    }

    /**
     * 操作员作废
     *
     * @param id
     * @param operatorPassword
     * @param modelMap
     * @return
     */
    @ResponseBody
    @PostMapping("/operatorWithdraw.action")
    public BaseOutput<Object> operatorWithdraw(Long id, String operatorPassword, ModelMap modelMap) {
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<Object> output = this.weighingBillRpc.operatorWithdraw(id, user.getId(), operatorPassword);
        return output;
    }

    /**
     * 获取取重按钮是否可用
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getFetchWeightSwitch.action")
    public BaseOutput<Object> getFetchWeightSwitch() {
        DataDictionaryValue ddValueQuery = DTOUtils.newInstance(DataDictionaryValue.class);
        ddValueQuery.setFirmCode(OrdersConstant.SHOUGUANG_FIRM_CODE);
        ddValueQuery.setDdCode(OrdersConstant.FETCH_WEIGHT_SWITCH_DD_CODE);
        BaseOutput<List<DataDictionaryValue>> output = this.dataDictionaryRpc.listDataDictionaryValueWithFirm(ddValueQuery);
        if (!output.isSuccess()) {
            return BaseOutput.failure(output.getMessage());
        }
        return BaseOutput.successData(Boolean.valueOf(output.getData().get(0).getName()));
    }
}
