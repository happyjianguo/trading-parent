package com.dili.trading.controller;


import com.dili.order.domain.TransitionDepartureApply;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.rpc.TransitionDepartureApplyRpc;
import com.dili.trading.service.TransitionDepartureApplyService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * TransitionDepartureApplyController
 */
@Controller
@RequestMapping("/transitionDepartureApplyController")
public class TransitionDepartureApplyController {

    @Autowired
    private TransitionDepartureApplyRpc transitionDepartureApplyRpc;

    @Autowired
    private TransitionDepartureApplyService transitionDepartureApplyService;

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
     * 分页查询
     *
     * @param transitionDepartureApply
     * @return
     */
    @RequestMapping(value = "/listPage.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listPage(@RequestBody TransitionDepartureApply transitionDepartureApply) {
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
    public String listByQueryParams(@RequestBody TransitionDepartureApply transitionDepartureApply) {
        return transitionDepartureApplyRpc.listByQueryParams(transitionDepartureApply);
    }


    /**
     * 新增TransitionDepartureApply
     *
     * @param transitionDepartureApply
     * @return BaseOutput
     */
    @RequestMapping(value = "/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput insert(@RequestBody TransitionDepartureApply transitionDepartureApply) {
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
    public BaseOutput approval(@RequestBody TransitionDepartureApply transitionDepartureApply) {
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
    @RequestMapping(value = "/getOneByCustomerID.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput getOneByCustomerID(@RequestBody TransitionDepartureApply transitionDepartureApply) {
        if (transitionDepartureApply.getCustomerId() == null) {
            return BaseOutput.failure("查询失败，客户主键不能为空");
        }
        try {
            BaseOutput<TransitionDepartureApply> oneByCustomerID = transitionDepartureApplyRpc.getOneByCustomerID(transitionDepartureApply);
            return BaseOutput.successData(ValueProviderUtils.buildDataByProvider(oneByCustomerID.getData(), Lists.newArrayList(oneByCustomerID)));
        } catch (Exception e) {
            e.printStackTrace();
            return BaseOutput.failure("查询失败" + e.getMessage());
        }
    }

    /**
     * 根据id获取申请单，并且使用provider
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getOneByID.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput getOneByID(Long id) {
        if (id == null) {
            return BaseOutput.failure("查询失败，申请单主键不能为空");
        }
        try {
            BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(id);
            return BaseOutput.successData(ValueProviderUtils.buildDataByProvider(oneByID.getData(), Lists.newArrayList(oneByID)));
        } catch (Exception e) {
            e.printStackTrace();
            return BaseOutput.failure("查询失败" + e.getMessage());
        }
    }
}
