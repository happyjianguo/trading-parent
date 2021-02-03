package com.dili.trading.controller;

import com.alibaba.fastjson.JSON;
import com.dili.assets.sdk.dto.CusCategoryDTO;
import com.dili.assets.sdk.dto.CusCategoryQuery;
import com.dili.assets.sdk.rpc.AssetsRpc;
import com.dili.commons.glossary.EnabledStateEnum;
import com.dili.orders.domain.GoodsReferencePriceSetting;
import com.dili.orders.domain.ReferenceRule;
import com.dili.orders.dto.ReferencePriceSettingRequestDto;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.dto.GoodsReferencePriceQueryDto;
import com.dili.trading.dto.ReferenceSettingResponseDto;
import com.dili.trading.rpc.GoodsReferencePriceSettingRpc;
import com.dili.trading.service.GoodsReferencePriceSettingService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Description: 品类参考价功能Controller类
 *
 * @date: 2020/8/21
 * @author: Seabert.Zhan
 */
@Controller
@RequestMapping("/goodsReferencePriceSetting")
public class GoodsReferencePriceSettingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsReferencePriceSettingController.class);

    /** 品类参考价 参考价规则  */
    private static final String REFERENCE_RULE = "referenceRule";

    private static final String REFERENCE_RULE_PROVIDER = "referenceRuleProvider";

    @Autowired
    private GoodsReferencePriceSettingRpc goodsReferencePriceSettingRpc;

    @Autowired
    private GoodsReferencePriceSettingService goodsReferencePriceSettingService;

    @Autowired
    private AssetsRpc assetsRpc;

    /**
     * 跳转到列表页面
     *
     * @return
     */
    @RequestMapping(value = "/index.html", method = RequestMethod.GET)
    public String index() {
        return "goodsReferencePriceSetting/index";
    }

    /**
     * 获取所有商品
     *
     * @param goodsReferencePriceSetting
     * @return
     */
    @RequestMapping(value = "/getAllGoods.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String getAllGoods(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        Map<Object, Object> metadata = new HashMap<Object, Object>();
        metadata.put(REFERENCE_RULE, REFERENCE_RULE_PROVIDER);
        CusCategoryQuery categoryDTO = new CusCategoryQuery();
        categoryDTO.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        categoryDTO.setParent(goodsReferencePriceSetting.getParentGoodsId());
        categoryDTO.setState(EnabledStateEnum.ENABLED.getCode());
        BaseOutput<List<CusCategoryDTO>> categoryDTOList = assetsRpc.listCusCategory(categoryDTO);

        try {
            List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, categoryDTOList.getData());
            return JSON.toJSON(list).toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return this.index();
        }
    }

    /**
     * 获取节点下面所有商品
     *
     * @return
     */
    @RequestMapping(value = "/getGoodsByParentId.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<List<ReferenceSettingResponseDto>> getGoodsByParentId(GoodsReferencePriceQueryDto params) throws Exception {
        params.setFirmId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        List<ReferenceSettingResponseDto> result = goodsReferencePriceSettingService.getListByParentV2(params);
        return BaseOutput.successData(result);
    }

    /**
     * 新增价格预警页面
     *
     * @return
     */
    @RequestMapping(value = "/add.html", method = RequestMethod.GET)
    public String add(Long goodsId, ModelMap modelMap) {
        modelMap.put("goodsId", goodsId);
        return "goodsReferencePriceSetting/add";
    }

    /**
    * 详情
    * @author miaoguoxin
    * @date 2021/2/3
    */
    @GetMapping("/detail.action")
    @ResponseBody
    public BaseOutput<ReferenceSettingResponseDto> getDetail(Long goodsId){
        ReferenceSettingResponseDto detail = goodsReferencePriceSettingService.getDetail(goodsId);
        return BaseOutput.successData(detail);
    }


    /**
    * 新增/编辑
    * @author miaoguoxin
    * @date 2021/2/1
    */
    @PostMapping("/saveOrEdit.action")
    @ResponseBody
    public BaseOutput<?> saveOrEdit(@RequestBody ReferencePriceSettingRequestDto requestDto){
        LOGGER.info("参考价编辑参数：{}",JSON.toJSONString(requestDto));
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        requestDto.setMarketId(userTicket.getFirmId());
        requestDto.getItems().forEach(itemDto -> {
            itemDto.setModifierId(userTicket.getId());
            itemDto.setCreatorId(userTicket.getId());
        });
        goodsReferencePriceSettingService.saveOrEdit(requestDto);
        return BaseOutput.success();
    }


    /**
     * 根据快捷吗查询商品
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getGoodsByKeyword.action")
    public BaseOutput<List<ReferenceSettingResponseDto>> getGoodsByKeyword(GoodsReferencePriceQueryDto params) throws Exception {
        params.setFirmId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        List<ReferenceSettingResponseDto> result = goodsReferencePriceSettingService.getListV2(params);

        return BaseOutput.successData(result);
    }
}
