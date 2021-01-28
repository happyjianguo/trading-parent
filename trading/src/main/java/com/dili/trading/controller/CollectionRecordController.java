package com.dili.trading.controller;

import com.dili.orders.domain.CollectionRecord;
import com.dili.orders.dto.WeighingCollectionStatementDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.EasyuiPageOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.rpc.CollectionRecordRpc;
import com.dili.uap.sdk.glossary.DataAuthType;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 回款单接口
 */
@RequestMapping("/collectionRecord")
@Controller
public class CollectionRecordController {


    @Autowired
    private CollectionRecordRpc collectionRecordRpc;

    /**
     * 跳转页面
     */
    @RequestMapping(value = "/list.html", method = RequestMethod.GET)
    public String list(ModelMap modelMap) {
        return "collectionRecord/list";
    }


    /**
     * 跳转页面
     */
    @RequestMapping(value = "/groupList.html", method = RequestMethod.GET)
    public String groupList(ModelMap modelMap) {
        return "collectionRecord/groupList";
    }

    /**
     * 跳转页面
     */
    @RequestMapping(value = "/weighingBillList.html", method = RequestMethod.GET)
    public String weighingBillList() {
        return "collectionRecord/detail";
    }

    /**
     * 获取数据
     */
    @PostMapping(value = "/weighingBillListQuery.action")
    @ResponseBody
    public String weighingBillListQuery(@RequestBody CollectionRecord collectionRecord) throws Exception {
        if (CollectionUtils.isEmpty(collectionRecord.getCollectionRecordIds())) {
            return "id不能为空";
        }
        BaseOutput<List<WeighingCollectionStatementDto>> listBaseOutput = collectionRecordRpc.weighingBills(collectionRecord);
        if (!listBaseOutput.isSuccess()) {
            return "查询失败";
        }
        return new EasyuiPageOutput(Long.valueOf(listBaseOutput.getData().size()), ValueProviderUtils.buildDataByProvider(collectionRecord, listBaseOutput.getData())).toString();
    }

    /**
     * 根据参数查询数据
     *
     * @param collectionRecord
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listByQueryParams.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String listByQueryParams(@RequestBody CollectionRecord collectionRecord) throws Exception {
        //判断查询的部门权限id是否为空，如果为空则查询所有的，如果不为空则查询单独的
        if (CollectionUtils.isEmpty(collectionRecord.getDepartmentIds())) {
            List<Long> departments = SessionContext.getSessionContext().dataAuth(DataAuthType.DEPARTMENT.getCode()).stream().map(x -> Long.valueOf((String) x.get("value"))).collect(Collectors.toList());
            //数据权限，根据部门查询
            if (CollectionUtils.isNotEmpty(departments)) {
                collectionRecord.setDepartmentIds(departments);
            }
        }
        PageOutput<List<CollectionRecord>> output = collectionRecordRpc.listByQueryParams(collectionRecord);
        return new EasyuiPageOutput(output.getTotal(), ValueProviderUtils.buildDataByProvider(collectionRecord, output.getData())).toString();
    }

    /**
     * 根据参数查询数据
     *
     * @param collectionRecord
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/groupListByQueryParams.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String groupListByQueryParams(@RequestBody CollectionRecord collectionRecord) throws Exception {
        //判断买家id或买家卡账户是否为空，必须存在一个
        if (Objects.isNull(collectionRecord.getBuyerId()) && Objects.isNull(collectionRecord.getAccountBuyerId())) {
            return "买家id或买家卡账户不能为空";
        }
        //判断卖家id和卖家卡账户是否为空，必须存在一个
        if (Objects.isNull(collectionRecord.getSellerId()) && Objects.isNull(collectionRecord.getAccountSellerId())) {
            return "卖家id或卖家卡账户不能为空";
        }
        //判断查询的部门权限id是否为空，如果为空则查询所有的，如果不为空则查询单独的
        List<Long> departments = SessionContext.getSessionContext().dataAuth(DataAuthType.DEPARTMENT.getCode()).stream().map(x -> Long.valueOf((String) x.get("value"))).collect(Collectors.toList());
        //数据权限，根据部门查询
        if (CollectionUtils.isNotEmpty(departments)) {
            collectionRecord.setDepartmentIds(departments);
        }
        //设置市场id
        collectionRecord.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        BaseOutput<List<Map<String, String>>> baseOutput = collectionRecordRpc.groupListForDetail(collectionRecord);
        if (!baseOutput.isSuccess()) {
            return "查询失败";
        }
        return new EasyuiPageOutput(Long.valueOf(baseOutput.getData().size()), baseOutput.getData()).toString();
    }
}
