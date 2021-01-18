package com.dili.trading.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dili.assets.sdk.dto.CusCategoryDTO;
import com.dili.assets.sdk.dto.CusCategoryQuery;
import com.dili.assets.sdk.rpc.AssetsRpc;
import com.dili.customer.sdk.domain.CharacterType;
import com.dili.customer.sdk.domain.dto.CharacterSubTypeDto;
import com.dili.customer.sdk.domain.dto.CharacterTypeGroupDto;
import com.dili.customer.sdk.domain.dto.CustomerExtendDto;
import com.dili.customer.sdk.domain.dto.CustomerQueryInput;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.domain.ComprehensiveFee;
import com.dili.orders.domain.ComprehensiveFeeType;
import com.dili.orders.domain.CustomerView;
import com.dili.orders.dto.AccountSimpleResponseDto;
import com.dili.orders.rpc.CardRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.dto.DTOUtils;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.ss.util.MoneyUtils;
import com.dili.trading.rpc.ComprehensiveFeeRpc;
import com.dili.trading.service.ComprehensiveFeeService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.domain.dto.UserQuery;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.rpc.UserRpc;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 检测收费controller
 *
 * @author Henry.Huang
 * @date   2020-08-18
 */
@Controller
@RequestMapping("/comprehensiveFee")
public class ComprehensiveFeeController {

    @Autowired
    private ComprehensiveFeeRpc comprehensiveFeeRpc;

    @Autowired
    private ComprehensiveFeeService comprehensiveFeeService;

    @Autowired
    private CardRpc cardRpc;

    @Autowired
    AssetsRpc assetsRpc;

    @Autowired
    private UserRpc useRpc;

    @Autowired
    private CustomerRpc customerRpc;


    private static final Logger LOGGER = LoggerFactory.getLogger(ComprehensiveFeeController.class);


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
        return "comprehensiveFee/list";
    }

    /**
     * 分页查询
     *
     * @param comprehensiveFee 参数obj
     * @return
     */
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listPage(ComprehensiveFee comprehensiveFee) {
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DEPARTMENT.getCode());
        if (CollectionUtils.isNotEmpty(ranges)) {
            String value = (String) ranges.get(0).get("value");
            //如果value为0，则为个人
            if ("0".equals(value)) {
                comprehensiveFee.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
            }
        }
        return comprehensiveFeeRpc.listPage(comprehensiveFee);
    }

    /**
     * 根据参数查询数据
     *
     * @param  comprehensiveFee 参数obj
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listByQueryParams.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listByQueryParams(ComprehensiveFee comprehensiveFee) throws Exception {
        //拿到部门数据权限
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        comprehensiveFee.setMarketId(userTicket.getFirmId());
        comprehensiveFee.setOrderType(ComprehensiveFeeType.TESTING_CHARGE.getValue());
        PageOutput<List<ComprehensiveFee>> output = comprehensiveFeeRpc.listByQueryParams(comprehensiveFee);
        return new EasyuiPageOutput(output.getTotal(), ValueProviderUtils.buildDataByProvider(comprehensiveFee, output.getData())).toString();
    }

    /**
     * 根据参数查询交易总额和交易总数
     *
     * @param comprehensiveFee 参数Obj
     * @return
     */
    @RequestMapping(value = "/selectCountAndTotal.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<ComprehensiveFee> selectCountAndTotal(@RequestBody ComprehensiveFee comprehensiveFee) throws Exception {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        comprehensiveFee.setMarketId(userTicket.getFirmId());
        comprehensiveFee.setOrderType(ComprehensiveFeeType.TESTING_CHARGE.getValue());
        BaseOutput<ComprehensiveFee> res = comprehensiveFeeRpc.selectCountAndTotal(comprehensiveFee);
        if(res.getData().getTransactionsTotal()==null){
            res.getData().setTransactionsTotal(0L);
        }
        return res;
    }



    /**
     * 检测收费单新增页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/add.html", method = RequestMethod.GET)
    public String add(ModelMap modelMap) {
        return "comprehensiveFee/add";
    }

    /**
     * 跳转到检测收费输入密码页面
     *
     * @param  modelMap
     * @param  id 检查收费单据ID
     * @return
     */
    @RequestMapping(value = "/verificationUsernamePassword.action", method = RequestMethod.GET)
    public String verificationUsernamePassword(ModelMap modelMap, Long id) {
        BaseOutput<ComprehensiveFee> oneById = comprehensiveFeeRpc.getOneById(id);
        modelMap.put("comprehensiveFee", oneById.getData());
        modelMap.put("chargeAmountView", MoneyUtils.centToYuan(oneById.getData().getChargeAmount()));
        return "comprehensiveFee/verificationUsernamePassword";
    }

    /**
     * 检测收费缴费
     *
     * @param  id 检查收费单据ID
     * @param  password 密码
     * @return BaseOutput
     */
    @RequestMapping(value = "/pay.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<ComprehensiveFee> pay(Long id, String password) throws Exception{
        return comprehensiveFeeService.pay(id, password);
    }

    /**
     * 根据卡号获取账户余额
     *
     * @param customerCardNo 客户卡号
     * @return
     */
    @RequestMapping(value = "/queryAccountBalance.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> queryAccountBalance(String customerCardNo) {
        return cardRpc.getOneAccountCard(customerCardNo);
    }

    /**
     * 新增comprehensiveFee
     *
     * @param comprehensiveFee 参数OBJ
     * @return BaseOutput
     */
    @RequestMapping(value = "/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<ComprehensiveFee> insert(ComprehensiveFee comprehensiveFee) throws Exception{
        String tips = checkUpDate(comprehensiveFee);
        if(StringUtils.isNotBlank(tips)){
            return BaseOutput.failure(tips);
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        if(comprehensiveFee.getPlate()!=null){
            comprehensiveFee.setPlate(comprehensiveFee.getPlate().trim());
        }
        comprehensiveFee.setMarketId(userTicket.getFirmId());
        comprehensiveFee.setOrderType(ComprehensiveFeeType.TESTING_CHARGE.getValue());
        //设置测试商品名称
        String itemName=getItemNameByItemId(comprehensiveFee.getInspectionItem(),userTicket.getFirmId());
        comprehensiveFee.setInspectionItemName(itemName);
        return comprehensiveFeeService.insertComprehensiveFee(comprehensiveFee);
    }

    /**
     * 跳到查看页面
     *
     * @param modelMap
     * @param comprehensiveFee 参数Obj
     * @return
     */
    @RequestMapping(value = "/view.html", method = {RequestMethod.GET, RequestMethod.POST})
    public String getOneByID(ModelMap modelMap, ComprehensiveFee comprehensiveFee) throws Exception {
        if (comprehensiveFee.getId() == null) {
            return "查询失败，检测收费单主键不能为空";
        }
        Map<Object, Object> map = new HashMap<>();
        //设置单据状态提供者
        map.put("orderStatus", getProvider("payStatusProvider", "orderStatus"));
        map.put("chargeAmount", getProvider("moneyProvider", "chargeAmount"));
        comprehensiveFee.setMetadata(map);
        BaseOutput<ComprehensiveFee> oneByID = comprehensiveFeeRpc.getOneById(comprehensiveFee.getId());
        if (oneByID.isSuccess()) {
            oneByID.getData().setCustomerType(getCustomerTypeAndName(oneByID.getData().getCustomerType()).getSubTypeTranslate());
            if (Objects.nonNull(oneByID.getData())) {
                modelMap.put("comprehensiveFee", ValueProviderUtils.buildDataByProvider(comprehensiveFee, Lists.newArrayList(oneByID.getData())).get(0));
            }
        }
        return "comprehensiveFee/view";
    }

    /**
     * 对接计费规则
     *
     * @param  customerId 客户ID
     * @param  type 客户类型
     * @return
     */
    @RequestMapping(value = "/fee.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<?> getFee(Long customerId, String type) {
        if (Objects.isNull(customerId)) {
            return BaseOutput.failure("顾客编号不能为空");
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        BaseOutput baseOutput = comprehensiveFeeRpc.getFee(userTicket.getFirmId(), customerId, type);
        return baseOutput;
    }

    /**
     * 综合收费将前一天未结算单据关闭定时任务执行接口
     *
     * @return
     */
    @RequestMapping(value = "/scheduleUpdate.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<String> scheduleUpdate() {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 3, 120L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1));
        // 任务1
        pool.execute(() -> {
            try {
                comprehensiveFeeRpc.scheduleUpdate();
            } catch (Exception e) {
                LOGGER.error("综合收费将前一天未结算单据关闭定时任务:" + e.getMessage());
            }
        });
        return BaseOutput.success("综合收费将前一天未结算单据关闭定时任务执行成功");
    }

    /**
     * 撤销密码页面
     *
     * @param id 检查收费单据ID
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/revocatorPage.html", method = RequestMethod.GET)
    public String revocatorPage(Long id, ModelMap modelMap) {
        modelMap.addAttribute("comprehensiveFeeId", id).addAttribute("model", SessionContext.getSessionContext().getUserTicket());
        return "comprehensiveFee/revocatorPage";
    }

    /**
     * 撤销
     *
     * @param id                 检测收费ID
     * @param operatorPassword   操作人密码
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/revocator.action", method = {RequestMethod.GET, RequestMethod.POST})
    public BaseOutput<ComprehensiveFee> revocator(Long id, @RequestParam(value="password") String operatorPassword) throws Exception{
        BaseOutput<ComprehensiveFee> oneById=comprehensiveFeeRpc.getOneById(id);
        if(!oneById.isSuccess()) {
            return BaseOutput.failure("检测单不存在");
        }
        return comprehensiveFeeService.revocator(oneById.getData(), operatorPassword);
    }

    /**
     * 获取一个comprehensiveFee单
     *
     * @param comprehensiveFee 参数Obj
     * @return BaseOutput
     */
    @RequestMapping(value = "/printOneById.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<ComprehensiveFee> printOneById( ComprehensiveFee comprehensiveFee) throws Exception {
        Map<Object, Object> map = new HashMap<>();
        //设置单据状态提供者
        map.put("orderStatus", getProvider("payStatusProvider", "orderStatus"));
        comprehensiveFee.setMetadata(map);
        BaseOutput<ComprehensiveFee> oneByID = comprehensiveFeeRpc.getOneById(comprehensiveFee.getId());
        if (!oneByID.isSuccess()) {
            return BaseOutput.failure("检测单不存在");
        }
        return oneByID;
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
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        UserQuery userQuery = DTOUtils.newInstance(UserQuery.class);
        userQuery.setKeyword(keyword);
        userQuery.setFirmCode(user.getFirmCode());
        return this.useRpc.listByExample(userQuery);
    }

    /**
     * 根据卡号获取客户
     *
     * @param cardNo
     * @return
     */
    @ResponseBody
    @RequestMapping("/listCustomerByCardNo.action")
    public BaseOutput<?> listCustomerByCardNo(String cardNo, ModelMap modelMap) throws Exception {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<AccountSimpleResponseDto> cardOutput = this.cardRpc.getOneAccountCard(cardNo);
        if (!cardOutput.isSuccess() || Objects.isNull(cardOutput.getData()) || Objects.isNull(cardOutput.getData().getAccountInfo())) {
            return cardOutput;
        }
        CustomerView customerView=new CustomerView();
        customerView.setHoldName(cardOutput.getData().getAccountInfo().getHoldName());
        CustomerQueryInput cq = new CustomerQueryInput();
        cq.setId(cardOutput.getData().getAccountInfo().getCustomerId());
        //获取当前登录人的市场，和客户市场进行对比
        //如果不相等，就直接返回
        if (!Objects.equals(userTicket.getFirmId(), cardOutput.getData().getAccountInfo().getFirmId())) {
            return BaseOutput.failure("未查询到相关客户信息");
        }
        cq.setMarketId(cardOutput.getData().getAccountInfo().getFirmId());
        BaseOutput<CustomerExtendDto> output = this.customerRpc.get(cardOutput.getData().getAccountInfo().getCustomerId(),cardOutput.getData().getAccountInfo().getFirmId());
        if(output ==null || output.getData()==null){
            return BaseOutput.failure("未查询到相关客户信息");
        }
        List<CharacterTypeGroupDto> characterTypeGroups=output.getData().getCharacterTypeGroupList();
        if(characterTypeGroups == null || characterTypeGroups.size() == 0){
            return BaseOutput.failure("顾客不存在身份类型！");
        }
        List<Map> jsonObjList=new ArrayList<>(characterTypeGroups.size());
        String subTypeTranslate="";
        String slpit=",";
        for (CharacterTypeGroupDto characterTypeGroupDto:characterTypeGroups) {
            if(characterTypeGroupDto.getSubTypeList()!=null && characterTypeGroupDto.getSubTypeList().size()>0){
                List<CharacterSubTypeDto> characterSubTypeDtos=characterTypeGroupDto.getSubTypeList();
                if(characterSubTypeDtos != null && characterSubTypeDtos.size()>0){
                    for (CharacterSubTypeDto characterSubTypeDto: characterSubTypeDtos) {
                        if(characterSubTypeDto.getSelected() && characterSubTypeDto.getCode() != null && !"".equals(characterSubTypeDto.getCode())){
                            Map<String,String> mapObj=new HashMap<>(4);
                            mapObj.put("subType",characterSubTypeDto.getCode());
                            mapObj.put("subTypeTranslate",characterSubTypeDto.getName());
                            subTypeTranslate+=characterSubTypeDto.getName()+slpit;
                            jsonObjList.add(mapObj);
                        }
                    }
                }
            }
        }
        if(jsonObjList.size() == 0){
            return BaseOutput.failure("顾客不存在身份类型！");
        }
        customerView.setId(output.getData().getId());
        customerView.setCode(output.getData().getCode());
        customerView.setName(output.getData().getName());
        customerView.setSubType(JSONObject.toJSONString(jsonObjList));
        if(subTypeTranslate.length()>0){
            customerView.setSubTypeTranslate(subTypeTranslate.substring(0,subTypeTranslate.length()-1));
        }
        return BaseOutput.successData(customerView);
    }

    /**
     * 获取provider 的JSONObject 对象
     *
     * @param providerName
     * @param field
     * @return
     */
    private JSONObject getProvider(String providerName, String field) {
        JSONObject provider = new JSONObject();
        //构建提供者名称
        provider.put("provider", providerName);
        //构建提供者的对应的字段，对哪个字段进行处理
        provider.put(ValueProvider.FIELD_KEY, field);
        //如果是数据字典提供者，需要传入参数，dd_code
        if ("dataDictionaryValueProvider".equals(providerName)) {
            provider.put("queryParams", "{dd_code:\"trade_type\"}");
        }
        return provider;
    }

    /**
     * 校验comprehensiveFee
     *
     * @param comprehensiveFee 参数Obj
     * @return
     */
    public String  checkUpDate(ComprehensiveFee comprehensiveFee){
        StringBuffer tips = new StringBuffer();
        if (StringUtils.isBlank(comprehensiveFee.getCustomerCardNo())){
            tips.append(",卡号不能为空");
        }else{
            if (comprehensiveFee.getCustomerId() == null){
                tips.append(",客户不存在或者卡号出错请联系管理员");
            }
        }
        if (tips.length() != 0){
            tips.append("!");
            return tips.substring(1);
        }
        return  "";
    }

    /**
     * 根据商品ID获取商品名称
     * @param inspectionItem
     * @return
     */
    public String getItemNameByItemId(String inspectionItem,Long marketId) {
        String returnName = "";
        if (StringUtils.isNotBlank(inspectionItem)) {
            List<String> ids = Arrays.asList(inspectionItem.split(","));
            CusCategoryQuery cusCategoryQuery=new CusCategoryQuery();
            cusCategoryQuery.setIds(ids);
            cusCategoryQuery.setMarketId(marketId);
            BaseOutput<List<CusCategoryDTO>> result=assetsRpc.listCusCategory(cusCategoryQuery);
            List<CusCategoryDTO> list = result.getData();
            if (list != null && list.size() > 0) {
                StringBuffer name=new StringBuffer("");
                for (CusCategoryDTO cgdto : list) {
                    name.append(",");
                    name.append(cgdto.getName());
                }

                if (name.length() > 0) {
                    returnName = name.substring(1);
                }
            }
        }
        return returnName;
    }

    /**
     * 将json数据转换成以逗号相隔
     * @param customerType
     * @return
     */
    private CustomerView getCustomerTypeAndName(String customerType) {
        CustomerView customerView= new CustomerView();
        if(customerType!=null&&!"".equals(customerType)){
            List<Map<String,String>> customerTypeJsonInfo= JSON.parseObject(customerType,List.class);
            StringBuffer subType=new StringBuffer();
            StringBuffer subTypeTranslate=new StringBuffer();
            String sliptStr=",";
            for (Map map:customerTypeJsonInfo) {
                subType.append(map.get("subType"));
                subType.append(sliptStr);
                subTypeTranslate.append(map.get("subTypeTranslate"));
                subTypeTranslate.append(sliptStr);
            }
            int length=subType.length();
            if(length>0){
                customerType=subType.substring(0,length-1);
            }
            if(subTypeTranslate.length()>0){
                customerView.setSubTypeTranslate(subTypeTranslate.substring(0,subTypeTranslate.length()-1));
            }
            customerView.setSubType(customerType);
        }
        return customerView;
    }
}
