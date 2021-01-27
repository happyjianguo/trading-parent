package com.dili.trading.controller;

import com.dili.orders.domain.CollectionRecord;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
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
    public String index(ModelMap modelMap) {
        return "collectionRecord/list";
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
}
