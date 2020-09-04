package com.dili.trading.controller;

import com.dili.assets.sdk.dto.CategoryDTO;
import com.dili.assets.sdk.rpc.AssetsRpc;
import com.dili.orders.constants.TradingConstans;
import com.dili.orders.domain.GoodsReferencePriceSetting;

import com.dili.orders.rpc.CategoryRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.metadata.ValueProviderUtils;
import com.dili.trading.rpc.GoodsReferencePriceSettingRpc;
import com.dili.trading.service.GoodsReferencePriceSettingService;
import com.dili.uap.sdk.domain.Firm;
import com.dili.uap.sdk.rpc.FirmRpc;
import com.dili.uap.sdk.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import java.util.*;

/**
 * Description: 品类参考价功能Controller类
 *
 * @date:    2020/8/21
 * @author:   Seabert.Zhan
 */
@Controller
@RequestMapping("/goodsReferencePriceSetting")
public class GoodsReferencePriceSettingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsReferencePriceSettingController.class);

    @Autowired
    private GoodsReferencePriceSettingRpc goodsReferencePriceSettingRpc;

    @Autowired
    private GoodsReferencePriceSettingService goodsReferencePriceSettingService;

    @Autowired
    private AssetsRpc assetsRpc;

    @Autowired
    private FirmRpc firmRpc;

    @Autowired
    private CategoryRpc categoryRpc;

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
        metadata.put("referenceRule", "referenceRuleProvider");
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        categoryDTO.setParent(goodsReferencePriceSetting.getParentGoodsId());

        BaseOutput<List<CategoryDTO>> categoryDTOList = assetsRpc.list(categoryDTO);

        try {
            List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, categoryDTOList.getData());
            return   new Gson().toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            return this.index();
        }
    }

    /**
     * 获取节点下面所有商品
     *
     * @param goodsReferencePriceSetting
     * @return
     */
    @RequestMapping(value = "/getGoodsByParentId.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String getGoodsByParentId(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        Map<Object, Object> metadata = new HashMap<Object, Object>();
        metadata.put("referenceRule", "referenceRuleProvider");
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        categoryDTO.setParent(goodsReferencePriceSetting.getParentGoodsId());
        //获取当前节点本身的数据
        BaseOutput<CategoryDTO> categoryDTOOneSelf = assetsRpc.get(goodsReferencePriceSetting.getParentGoodsId());
        //获取节点下面子节点的数据
        BaseOutput<List<CategoryDTO>> categoryDTOList = assetsRpc.list(categoryDTO);
        goodsReferencePriceSetting.setGoodsName(null);
        goodsReferencePriceSetting.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        //获取节点子节点的数据（goods_reference_price_setting）
        List<GoodsReferencePriceSetting> goodsReferencePriceSettings =  goodsReferencePriceSettingRpc.getAllGoods(goodsReferencePriceSetting);
        try {
            ////获取节点本身的数据（goods_reference_price_setting）
            goodsReferencePriceSetting.setGoodsId(goodsReferencePriceSetting.getParentGoodsId());
            goodsReferencePriceSetting.setParentGoodsId(null);
            BaseOutput<GoodsReferencePriceSetting> output = goodsReferencePriceSettingRpc.findDetailDtoById(goodsReferencePriceSetting);
            if (!output.isSuccess()) {
                LOGGER.error(output.getMessage());
                return this.index();
            }
            //组装数据
            List<CategoryDTO> categoryList = new ArrayList<CategoryDTO>();
            CategoryDTO categoryOneSelf = categoryDTOOneSelf.getData();
            if(categoryOneSelf != null) {
                categoryList.add(categoryOneSelf);
                List<CategoryDTO> categoryListTemp = categoryDTOList.getData();
                if(categoryListTemp != null){
                    categoryList.addAll(categoryListTemp);
                }
            } else{
                throw new Exception("未获取到当前节点的商品数据");
            }

            GoodsReferencePriceSetting tempGoods = output.getData();
            if(tempGoods != null) {
                if(goodsReferencePriceSettings == null){
                    goodsReferencePriceSettings = new  ArrayList<GoodsReferencePriceSetting>();
                }
                goodsReferencePriceSettings.add(tempGoods);
            }
            List<GoodsReferencePriceSetting> finalSettings = new ArrayList<GoodsReferencePriceSetting>();
            if(categoryList != null) {
                if(goodsReferencePriceSettings != null){
                    for(CategoryDTO category : categoryList){
                        boolean flag = false;
                        for(GoodsReferencePriceSetting goodsSetting : goodsReferencePriceSettings)
                        {
                            if(category.getId().equals(goodsSetting.getGoodsId())){
                                finalSettings.add(goodsSetting);
                                flag = true;
                                break;
                            }
                        }

                        if(!flag){
                            GoodsReferencePriceSetting tempSetting = new GoodsReferencePriceSetting();
                            tempSetting.setGoodsId(category.getId());
                            tempSetting.setGoodsName(category.getName());
                            tempSetting.setParentGoodsId(category.getParent());
                            tempSetting.setReferenceRule(null);
                            finalSettings.add(tempSetting);
                        }
                    }
                }
            }

            List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, finalSettings);
            return   new Gson().toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            return this.index();
        }
    }

    /**
     * 新增价格预警页面
     *
     * @param goodsId
     * @param goodsName
     * @param parentGoodsId
     * @param referenceRule
     * @param modelMap
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
        metadata.put("referenceRule", "referenceRuleProvider");
        try {
            if(output.getData() == null)
            {
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
            e.printStackTrace();
            return this.index();
        }
    }

    /**
     * 新增/编辑品类参考价（如果数据库不存在数据，则新增，否则修改）
     *
     * @param goodsReferencePriceSetting
     * @return BaseOutput
     */
    @RequestMapping(value = "/insert.action", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseOutput insert(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        goodsReferencePriceSetting.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        BaseOutput<GoodsReferencePriceSetting> output = goodsReferencePriceSettingRpc.findDetailDtoById(goodsReferencePriceSetting);
        if (!output.isSuccess()) {
            LOGGER.error(output.getMessage());
            return output;
        }

        if(output.getData() == null){
            return goodsReferencePriceSettingService.insertGoodsReferencePriceSetting(goodsReferencePriceSetting);
        } else{
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
     * @param goodsReferencePriceSetting 快捷码
     * @return
     */
    @ResponseBody
    @RequestMapping("/getGoodsByKeyword.action")
    public String getGoodsByKeyword(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        try {
            BaseOutput<Firm> output = this.firmRpc.getByCode(TradingConstans.SHOUGUANG_FIRM_CODE);
            if (!output.isSuccess()) {
                LOGGER.error(output.getMessage());
                throw new Exception("查询市场信息失败");
            }
            if (output.getData() == null) {
                throw new Exception("市场信息不存在");
            }
            Map<Object, Object> metadata = new HashMap<Object, Object>();
            metadata.put("referenceRule", "referenceRuleProvider");

            CategoryDTO query = new CategoryDTO();
            query.setMarketId(output.getData().getId());
            query.setKeyword(goodsReferencePriceSetting.getGoodsName());
            BaseOutput<List<CategoryDTO>> categoryDTOList = this.categoryRpc.getTree(query);
            List<CategoryDTO> categoryList = categoryDTOList.getData();
            goodsReferencePriceSetting = new GoodsReferencePriceSetting();
            goodsReferencePriceSetting.setMarketId(output.getData().getId());
            List<GoodsReferencePriceSetting> goodsReferencePriceSettings =  goodsReferencePriceSettingRpc.getAllGoods(goodsReferencePriceSetting);

            List<GoodsReferencePriceSetting> finalSettings = new ArrayList<GoodsReferencePriceSetting>();
            if(categoryList != null) {
                if(goodsReferencePriceSettings != null){
                    for(CategoryDTO category : categoryList){
                        boolean flag = false;
                        for(GoodsReferencePriceSetting goodsSetting : goodsReferencePriceSettings)
                        {
                            if(category.getId().equals(goodsSetting.getGoodsId())){
                                finalSettings.add(goodsSetting);
                                flag = true;
                                break;
                            }
                        }

                        if(!flag){
                            GoodsReferencePriceSetting tempSetting = new GoodsReferencePriceSetting();
                            tempSetting.setGoodsId(category.getId());
                            tempSetting.setGoodsName(category.getName());
                            tempSetting.setParentGoodsId(category.getParent());
                            tempSetting.setReferenceRule(null);
                            finalSettings.add(tempSetting);
                        }
                    }
                }
            }

            List<Map> list = ValueProviderUtils.buildDataByProvider(metadata, finalSettings);
            return   new Gson().toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            return this.index();
        }
    }
}
