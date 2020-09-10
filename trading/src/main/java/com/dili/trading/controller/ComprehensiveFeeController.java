package com.dili.trading.controller;

import com.alibaba.fastjson.JSONObject;
import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.assets.sdk.rpc.CategoryRpc;
import com.dili.orders.domain.ComprehensiveFee;
import com.dili.orders.rpc.CardRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.rpc.ComprehensiveFeeRpc;
import com.dili.trading.service.ComprehensiveFeeService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.session.SessionContext;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    CategoryRpc categoryRpc;

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
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
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
        //拿到数据权限，个人或全部
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
        if (CollectionUtils.isNotEmpty(ranges)) {
            String value = (String) ranges.get(0).get("value");
            //如果value为0，则为个人
            if ("0".equals(value)) {
                comprehensiveFee.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
            }
        }
        comprehensiveFee.setOrderType(1);
        PageOutput<List<ComprehensiveFee>> output = comprehensiveFeeRpc.listByQueryParams(comprehensiveFee);
        return new EasyuiPageOutput(output.getTotal(), ValueProviderUtils.buildDataByProvider(comprehensiveFee, output.getData())).toString();
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
        BaseOutput oneById = comprehensiveFeeRpc.getOneById(id);
        modelMap.put("comprehensiveFee", oneById.getData());
        Double chargeAmountView=((ComprehensiveFee)oneById.getData()).getChargeAmount().doubleValue()/100;
        modelMap.put("chargeAmountView", String.format("%.2f",chargeAmountView));
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
    public BaseOutput pay(Long id, String password) {
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
    public BaseOutput queryAccountBalance(String customerCardNo) {
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
    public BaseOutput insert(ComprehensiveFee comprehensiveFee) {
        String tips=checkUpDate(comprehensiveFee);
        if(StringUtils.isNotBlank(tips)){
            BaseOutput<ComprehensiveFee> result=new BaseOutput<ComprehensiveFee>();
            result.setCode("500");
            result.setResult(tips);
            return result;
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        comprehensiveFee.setMarketId(userTicket.getFirmId());
        comprehensiveFee.setOrderType(1);
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
        comprehensiveFee.setMetadata(map);
        BaseOutput<ComprehensiveFee> oneByID = comprehensiveFeeRpc.getOneById(comprehensiveFee.getId());
        if (oneByID.isSuccess()) {
            if (Objects.nonNull(oneByID.getData())) {
                //翻译商品id
                if(StringUtils.isNotBlank(oneByID.getData().getInspectionItem())){
                    List<String> ids= Arrays.asList(oneByID.getData().getInspectionItem().split(","));
                    CategoryDTO categoryDTO = new CategoryDTO();

                    categoryDTO.setIds(ids);
                    List<CategoryDTO> list = categoryRpc.getTree(categoryDTO).getData();
                    if(list!=null&&list.size()>0){
                        StringBuffer name=new StringBuffer("");
                        for (CategoryDTO cgdto:list) {
                            name.append(",");
                            name.append(cgdto.getName());
                        }
                        if(name.length()>0){
                            oneByID.getData().setInspectionItem(name.substring(1));
                        }
                    }
                }
                modelMap.put("comprehensiveFee", ValueProviderUtils.buildDataByProvider(comprehensiveFee, Lists.newArrayList(oneByID.getData())).get(0));
                //返回小数缴费金额
                Double chargeAmountView=((ComprehensiveFee)oneByID.getData()).getChargeAmount().doubleValue()/100;
                modelMap.put("chargeAmountView", String.format("%.2f",chargeAmountView));
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
    public BaseOutput getFee(Long customerId, String type) {
        if (Objects.isNull(customerId)) {
            return BaseOutput.failure("顾客编号不能为空");
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        BaseOutput baseOutput=comprehensiveFeeRpc.getFee(userTicket.getFirmId(), customerId, type);
        return baseOutput;
    }

    /**
     * 撤销密码页面
     *
     * @param id 检查收费单据ID
     * @param modelMap
     * @return
     */
    @RequestMapping("/revocatorPage.html")
    public String revocatorPage(Long id, ModelMap modelMap) {
        modelMap.addAttribute("comprehensiveFeeId", id).addAttribute("model", SessionContext.getSessionContext().getUserTicket()).addAttribute("submitHandler", "revocator");
        return "comprehensiveFee/revocatorPage";
    }

    /**
     * 撤销
     *
     * @param id                 检查收费ID
     * @param userName           用户真实名字
     * @param operatorPassword   操作人密码
     * @param modelMap
     * @param operatorName       操作员登录名
     * @return
     */
    @ResponseBody
    @PostMapping("/revocator.action")
    public BaseOutput<Object> revocator(Long id,@RequestParam(value = "userName")String userName,@RequestParam(value="password") String operatorPassword, ModelMap modelMap, String operatorName) {
        UserTicket user = SessionContext.getSessionContext().getUserTicket();
        BaseOutput<Object> output = this.comprehensiveFeeRpc.revocator(id, user.getRealName(),user.getId(), operatorPassword, user.getUserName());
        return output;

    }
    /**
     * 获取一个comprehensiveFee单
     *
     * @param comprehensiveFee 参数Obj
     * @return BaseOutput
     */
    @RequestMapping(value = "/printOneById.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput printOneById( ComprehensiveFee comprehensiveFee) throws Exception {
        Map<Object, Object> map = new HashMap<>();
        //设置单据状态提供者
        map.put("orderStatus", getProvider("payStatusProvider", "orderStatus"));
        comprehensiveFee.setMetadata(map);
        BaseOutput<ComprehensiveFee> oneByID = comprehensiveFeeRpc.getOneById(comprehensiveFee.getId());
        if (oneByID.isSuccess()) {
            if (Objects.nonNull(oneByID.getData())) {
                //翻译商品id
                if (StringUtils.isNotBlank(oneByID.getData().getInspectionItem())) {
                    List<String> ids = Arrays.asList(oneByID.getData().getInspectionItem().split(","));
                    CategoryDTO categoryDTO = new CategoryDTO();

                    categoryDTO.setIds(ids);
                    List<CategoryDTO> list = categoryRpc.getTree(categoryDTO).getData();
                    if (list != null && list.size() > 0) {
                        StringBuffer name = new StringBuffer("");
                        for (CategoryDTO cgdto : list) {
                            name.append(",");
                            name.append(cgdto.getName());
                        }
                        if (name.length() > 0) {
                            oneByID.getData().setInspectionItem(name.substring(1));
                        }
                    }
                }
            }
        }
        return oneByID;
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
        StringBuffer tips=new StringBuffer();
        if (StringUtils.isBlank(comprehensiveFee.getCustomerCardNo())){
            tips.append(",卡号不能为空");
        }else{
            if (comprehensiveFee.getCustomerId()==null){
                tips.append(",客户不存在或者卡号出错请联系管理员");
            }
        }
        if (tips.length()!=0){
            tips.append("!");
            return tips.substring(1);
        }
        return  "";
    }

}
