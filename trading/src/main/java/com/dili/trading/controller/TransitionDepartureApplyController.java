package com.dili.trading.controller;

import com.alibaba.fastjson.JSONObject;
import com.dili.customer.sdk.domain.Customer;
import com.dili.customer.sdk.rpc.CustomerRpc;
import com.dili.orders.domain.TransitionDepartureApply;
import com.dili.orders.domain.TransitionDepartureSettlement;
import com.dili.orders.dto.UserAccountCardResponseDto;
import com.dili.orders.rpc.AccountRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.metadata.ValueProvider;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.rpc.TransitionDepartureApplyRpc;
import com.dili.trading.service.TransitionDepartureApplyService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TransitionDepartureApplyController
 */
@Controller
@RequestMapping("/transitionDepartureApply")
public class TransitionDepartureApplyController {


    @Autowired
    private TransitionDepartureApplyService transitionDepartureApplyService;

    @Autowired
    private TransitionDepartureApplyRpc transitionDepartureApplyRpc;

    @Autowired
    private AccountRpc accountRpc;

    @Autowired
    private CustomerRpc customerRpc;


    /**
     * 跳转到转离场申请单页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/list.html", method = RequestMethod.GET)
    public String index(ModelMap modelMap) {
        return "transitionDepartureApply/list";
    }


    /**
     * 跳转到转离场申请单新增页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/add.html", method = RequestMethod.GET)
    public String add(ModelMap modelMap) {
        return "transitionDepartureApply/add";
    }

    /**
     * 跳转到转离场申请单修改页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/update.html", method = RequestMethod.GET)
    public String update(ModelMap modelMap, TransitionDepartureApply transitionDepartureApply) throws Exception {
        if (transitionDepartureApply.getId() == null) {
            return "查询失败，申请单主键不能为空";
        }
        Map<Object, Object> map = new HashMap<>();
        //设置审批状态提供者
        map.put("approvalState", getProvider("applyProvider", "approvalState"));
        //设置业务类型提供者
        map.put("bizType", getProvider("bizTypeProvider", "bizType"));
        //设置商品提供者
        map.put("categoryId", getProvider("categoryProvider", "categoryId"));
        //设置交易类型提供者
        map.put("transTypeId", getProvider("dataDictionaryValueProvider", "transTypeId"));
        //设置车类型提供者
        map.put("carTypeId", getProvider("carTypeProvider", "carTypeId"));
        transitionDepartureApply.setMetadata(map);
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureApply.getId());
        if (oneByID.isSuccess()) {
            if (Objects.nonNull(oneByID.getData())) {
                modelMap.put("transitionDepartureApply", ValueProviderUtils.buildDataByProvider(transitionDepartureApply, Lists.newArrayList(oneByID.getData())).get(0));
            }
        }
        return "transitionDepartureApply/update";
    }

    /**
     * 跳转到转离场申请单详情页面
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/applyDetailPage.html", method = RequestMethod.GET)
    public String applyDetailPage(ModelMap modelMap) {
        return "transitionDepartureApply/applyDetailPage";
    }

    /**
     * 分页查询
     *
     * @param transitionDepartureApply
     * @return
     */
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listPage(TransitionDepartureApply transitionDepartureApply) {
        return transitionDepartureApplyRpc.listPage(transitionDepartureApply);
    }

    /**
     * 根据参数查询数据
     *
     * @param transitionDepartureApply
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listByQueryParams.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listByQueryParams(TransitionDepartureApply transitionDepartureApply) throws Exception {
        List<Map> ranges = SessionContext.getSessionContext().dataAuth(DataAuthType.DATA_RANGE.getCode());
        if (CollectionUtils.isNotEmpty(ranges)) {
            String value = (String) ranges.get(0).get("value");
            //如果value为0，则为个人
            if (value.equals("0")) {
                transitionDepartureApply.setUserId(SessionContext.getSessionContext().getUserTicket().getId());
            }
        }
        //数据权限，根据部门查询
//        List<Integer> arryList = null;
//        if (CollectionUtils.isNotEmpty(departments)) {
//            arryList = new ArrayList<>();
//            for (int i = 0; i < departments.size(); i++) {
//                Map map = departments.get(i);
//                arryList.add(Integer.valueOf((String) map.get("value")));
//            }
//            transitionDepartureApply.setDepartments(arryList);
//        }
        PageOutput<List<TransitionDepartureApply>> output = transitionDepartureApplyRpc.listByQueryParams(transitionDepartureApply);
        return new EasyuiPageOutput(output.getTotal(), ValueProviderUtils.buildDataByProvider(transitionDepartureApply, output.getData())).toString();
    }

    /**
     * 新增TransitionDepartureApply
     *
     * @param transitionDepartureApply
     * @return BaseOutput
     */
    @RequestMapping(value = "/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput insert(TransitionDepartureApply transitionDepartureApply) {
        return transitionDepartureApplyService.insert(transitionDepartureApply);
    }

    /**
     * 审批TransitionDepartureApply
     *
     * @param transitionDepartureApply
     * @return BaseOutput
     */
    @RequestMapping(value = "/approval.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput approval(TransitionDepartureApply transitionDepartureApply) {
        if (transitionDepartureApply.getId() == null) {
            return BaseOutput.failure("审批失败，申请单id不能为空");
        }
        return transitionDepartureApplyService.approval(transitionDepartureApply);
    }

    /**
     * 根据客户的主键查询最新一条申请单数据
     *
     * @param transitionDepartureApply
     * @return
     */
    @RequestMapping(value = "/getOneByCustomerCardNo.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput getOneByCustomerCardNo(TransitionDepartureApply transitionDepartureApply) {
        if (transitionDepartureApply.getCustomerCardNo() == null) {
            return BaseOutput.failure("查询失败，客户卡号不能为空");
        }
        try {
            Map<Object, Object> map = new HashMap<>();
            //设置审批状态提供者
            map.put("approvalState", getProvider("applyProvider", "approvalState"));
            //设置业务类型提供者
            map.put("bizType", getProvider("bizTypeProvider", "bizType"));
            //设置商品提供者
            map.put("categoryId", getProvider("categoryProvider", "categoryId"));
            //设置交易类型提供者
            map.put("transTypeId", getProvider("dataDictionaryValueProvider", "transTypeId"));
            //设置车类型提供者
            map.put("carTypeId", getProvider("carTypeProvider", "carTypeId"));
            transitionDepartureApply.setMetadata(map);
            BaseOutput<TransitionDepartureApply> oneByCustomerID = transitionDepartureApplyRpc.getOneByCustomerCardNo(transitionDepartureApply, SessionContext.getSessionContext().getUserTicket().getFirmId(), SessionContext.getSessionContext().getUserTicket().getDepartmentId());
            TransitionDepartureApply data = oneByCustomerID.getData();
            if (Objects.isNull(data)) {
                return BaseOutput.failure("该卡号暂时未查询到已审批的申请单");
            }
            if (Objects.isNull(data.getTransitionDepartureSettlement())) {
                data.setTransitionDepartureSettlement(new TransitionDepartureSettlement());
            }
            if (oneByCustomerID.isSuccess()) {
                if (Objects.nonNull(oneByCustomerID.getData())) {
                    return BaseOutput.successData(ValueProviderUtils.buildDataByProvider(transitionDepartureApply, Lists.newArrayList(data)).get(0));
                }
            }
            return oneByCustomerID;
        } catch (Exception e) {
            e.printStackTrace();
            return BaseOutput.failure("查询失败" + e.getMessage());
        }
    }

    /**
     * 根据id获取申请单，并且使用provider
     *
     * @param transitionDepartureApply
     * @return
     */
    @RequestMapping(value = "/getOneByID.action", method = {RequestMethod.GET, RequestMethod.POST})
    public String getOneByID(ModelMap modelMap, TransitionDepartureApply transitionDepartureApply) throws Exception {
        if (transitionDepartureApply.getId() == null) {
            return "查询失败，申请单主键不能为空";
        }
        Map<Object, Object> map = new HashMap<>();
        //设置审批状态提供者
        map.put("approvalState", getProvider("applyProvider", "approvalState"));
        //设置业务类型提供者
        map.put("bizType", getProvider("bizTypeProvider", "bizType"));
        //设置商品提供者
        map.put("categoryId", getProvider("categoryProvider", "categoryId"));
        //设置交易类型提供者
        map.put("transTypeId", getProvider("dataDictionaryValueProvider", "transTypeId"));
        //设置车类型提供者
        map.put("carTypeId", getProvider("carTypeProvider", "carTypeId"));
        transitionDepartureApply.setMetadata(map);
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureApply.getId());
        if (oneByID.isSuccess()) {
            if (Objects.nonNull(oneByID.getData())) {
                modelMap.put("transitionDepartureApply", ValueProviderUtils.buildDataByProvider(transitionDepartureApply, Lists.newArrayList(oneByID.getData())).get(0));
            }
        }
        return "transitionDepartureApply/view";
    }

    /**
     * 第一步：根据卡号获取客户信息
     *
     * @param cardNo
     * @return
     */
    @RequestMapping("/getCustomerByCardNo.action")
    @ResponseBody
    public BaseOutput getCustomerByCardNo(String cardNo) {
        BaseOutput<Customer> customerBaseOutput = null;
        if (StringUtils.isBlank(cardNo)) {
            return BaseOutput.failure("卡号不能为空");
        }
        //根据卡号id拿到对应的账号信息
        BaseOutput<UserAccountCardResponseDto> oneAccountCard = accountRpc.getOneAccountCard(cardNo);
        //判断请求是否成功
        if (oneAccountCard.isSuccess()) {
            //判断账户是否为空
            if (Objects.nonNull(oneAccountCard.getData())) {
                //如果账户信息不为空，则发起请求，根据用户id和市场id去拿到客户相关信息
                customerBaseOutput = customerRpc.get(oneAccountCard.getData().getCustomerId(), oneAccountCard.getData().getFirmId());
            } else {
                //没有查询到相关的账号信息
                return BaseOutput.failure("没有查询到相关账户信息");
            }
        } else {
            //不成功则直接返回根据卡号id查询对应账号的返回信息
            return oneAccountCard;
        }
        //判断客户rpc请求是否成功
        if (Objects.isNull(customerBaseOutput) || !customerBaseOutput.isSuccess() || Objects.isNull(customerBaseOutput.getData())) {
            return BaseOutput.failure("查询不到相关客户信息");
        }
        //如果存在相关客户信息,则返回客户信息
        return BaseOutput.successData(customerBaseOutput.getData());
    }

    /**
     * 第二步：根据卡号获取客户信息
     *
     * @param
     * @return
     */
    @RequestMapping("/getUserNameInSession.action")
    @ResponseBody
    public BaseOutput getUserNameInSession() {
        return BaseOutput.successData(SessionContext.getSessionContext().getUserTicket());
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
        if (providerName.equals("dataDictionaryValueProvider")) {
            provider.put("queryParams", "{dd_code:\"trade_type\"}");
        }
        return provider;
    }

}
