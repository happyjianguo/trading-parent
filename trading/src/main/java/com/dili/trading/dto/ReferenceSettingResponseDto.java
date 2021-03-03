package com.dili.trading.dto;


import com.dili.orders.dto.ReferencePriceSettingItemDto;

/**
 * @Auther: miaoguoxin
 * @Date: 2021/2/1 18:09
 * @Description: 参考价详情参数
 */
public class ReferenceSettingResponseDto {
    /**商品id*/
    private Long goodsId;
    /**品类名称*/
    private String goodsName;
    /**父级商品id*/
    private Long parentGoodsId;
    /**常规类型*/
    private ReferencePriceSettingItemDto genericItem;
    /**老农类型*/
    private ReferencePriceSettingItemDto traditionFarmerItem;
    /**自营类型*/
    private ReferencePriceSettingItemDto selfItem;

    public ReferenceSettingResponseDto() {
        //需要先初始化，减少前端判断
        this.genericItem = new ReferencePriceSettingItemDto();
        this.traditionFarmerItem = new ReferencePriceSettingItemDto();
        this.selfItem = new ReferencePriceSettingItemDto();
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Long getParentGoodsId() {
        return parentGoodsId;
    }

    public void setParentGoodsId(Long parentGoodsId) {
        this.parentGoodsId = parentGoodsId;
    }

    public ReferencePriceSettingItemDto getGenericItem() {
        return genericItem;
    }

    public void setGenericItem(ReferencePriceSettingItemDto genericItem) {
        this.genericItem = genericItem;
    }

    public ReferencePriceSettingItemDto getTraditionFarmerItem() {
        return traditionFarmerItem;
    }

    public void setTraditionFarmerItem(ReferencePriceSettingItemDto traditionFarmerItem) {
        this.traditionFarmerItem = traditionFarmerItem;
    }

    public ReferencePriceSettingItemDto getSelfItem() {
        return selfItem;
    }

    public void setSelfItem(ReferencePriceSettingItemDto selfItem) {
        this.selfItem = selfItem;
    }
}
