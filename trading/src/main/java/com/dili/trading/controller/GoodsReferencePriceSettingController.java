package com.dili.trading.controller;

import com.alibaba.fastjson.JSON;
import com.dili.assets.sdk.dto.CusCategoryDTO;
import com.dili.assets.sdk.dto.CusCategoryQuery;
import com.dili.assets.sdk.rpc.AssetsRpc;
import com.dili.commons.glossary.EnabledStateEnum;
import com.dili.orders.domain.GoodsReferencePriceSetting;
import com.dili.orders.domain.ReferenceRule;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.dto.GoodsReferencePriceQueryDto;
import com.dili.trading.rpc.GoodsReferencePriceSettingRpc;
import com.dili.trading.service.GoodsReferencePriceSettingService;
import com.dili.uap.sdk.session.SessionContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
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
    public String getGoodsByParentId(GoodsReferencePriceQueryDto params) throws Exception {
        Map<Object, Object> metadata = new HashMap<>();
        metadata.put(REFERENCE_RULE, REFERENCE_RULE_PROVIDER);

        params.setFirmId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        List<GoodsReferencePriceSetting> result = goodsReferencePriceSettingService.getListByParent(params);
        List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, result);
        return JSON.toJSON(list).toString();
    }

    /**
     * 新增价格预警页面
     *
     * @return
     */
    @RequestMapping(value = "/add.html", method = RequestMethod.GET)
    public String add(Long goodsId, String goodsName, Long parentGoodsId, Integer referenceRule, ModelMap modelMap) {
        GoodsReferencePriceSetting goodsReferencePriceSetting = new GoodsReferencePriceSetting();
        goodsReferencePriceSetting.setGoodsId(goodsId);
        goodsReferencePriceSetting.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        BaseOutput<GoodsReferencePriceSetting> output = goodsReferencePriceSettingRpc.findDetailDtoById(goodsReferencePriceSetting);
        if (!output.isSuccess()) {
            LOGGER.error(output.getMessage());
            return this.index();
        }
        Map<Object, Object> metadata = new HashMap<Object, Object>();
        metadata.put(REFERENCE_RULE, REFERENCE_RULE_PROVIDER);
        try {
            if (output.getData() == null) {
                goodsReferencePriceSetting = new GoodsReferencePriceSetting();
                goodsReferencePriceSetting.setGoodsId(goodsId);
                goodsReferencePriceSetting.setGoodsName(goodsName);
                goodsReferencePriceSetting.setParentGoodsId(parentGoodsId);
                goodsReferencePriceSetting.setReferenceRule(referenceRule);
                output.setData(goodsReferencePriceSetting);
            }
            List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, Arrays.asList(output.getData()));

            modelMap.addAttribute("model", list.get(0));
            return "goodsReferencePriceSetting/add";
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return this.index();
        }
    }

    /**
     * 新增/编辑品类参考价（如果数据库不存在数据，则新增，否则修改）
     *
     * @return BaseOutput
     */
    @RequestMapping(value = "/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput<GoodsReferencePriceSetting> insert(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        goodsReferencePriceSetting.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        BaseOutput<GoodsReferencePriceSetting> output = goodsReferencePriceSettingRpc.findDetailDtoById(goodsReferencePriceSetting);
        if (!output.isSuccess()) {
            LOGGER.error(output.getMessage());
            return output;
        }

        String tips = checkUpDate(goodsReferencePriceSetting);
        if (StringUtils.isNotBlank(tips)) {
            return BaseOutput.failure(tips);
        }

        if (output.getData() == null) {
            return goodsReferencePriceSettingService.insertGoodsReferencePriceSetting(goodsReferencePriceSetting);
        } else {
            goodsReferencePriceSetting.setId(output.getData().getId());
            goodsReferencePriceSetting.setVersion(output.getData().getVersion());
            goodsReferencePriceSetting.setCreatedTime(output.getData().getCreatedTime());
            goodsReferencePriceSetting.setCreatorId(output.getData().getCreatorId());
            return goodsReferencePriceSettingService.updateGoodsReferencePriceSetting(goodsReferencePriceSetting);
        }
    }


    /**
     * 根据快捷吗查询商品
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getGoodsByKeyword.action")
    public String getGoodsByKeyword(GoodsReferencePriceQueryDto params) throws Exception {
        Map<Object, Object> metadata = new HashMap<>();
        metadata.put(REFERENCE_RULE, REFERENCE_RULE_PROVIDER);

        params.setFirmId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        List<GoodsReferencePriceSetting> result = goodsReferencePriceSettingService.getList(params);
        List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, result);
        return JSON.toJSON(list).toString();
    }

    /**
     * 校验goodsReferencePriceSetting
     * @param goodsReferencePriceSetting
     * @return
     */
    public String checkUpDate(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        StringBuilder tips = new StringBuilder();
        if (ReferenceRule.RESCINDED.getCode().equals(goodsReferencePriceSetting.getReferenceRule())) {
            Long fixedPrice = goodsReferencePriceSetting.getFixedPrice();
            String regex = "^\\+?[1-9]\\d{0,4}(\\.\\d*)?$";
            if (fixedPrice == null || !Pattern.matches(regex, String.valueOf(fixedPrice))) {
                tips.append(",固定价格必须是0.01-999.99之间的数字且最多两位小数");
            }
        }
        if (tips.length() != 0) {
            tips.append("!");
            return tips.substring(1);
        }
        return "";
    }
}
