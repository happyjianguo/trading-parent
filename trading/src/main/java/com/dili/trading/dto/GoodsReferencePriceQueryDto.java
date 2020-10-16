package com.dili.trading.dto;

import com.dili.ss.domain.BaseDomain;


/**
 * @Auther: miaoguoxin
 * @Date: 2020/10/15 14:35
 * @Description:
 */
public class GoodsReferencePriceQueryDto extends BaseDomain {
    /**商品名*/
    private String goodsName;
    /**只查询有参考价的*/
    private Integer onlyExistReferencePrice;
    /**父节点id*/
    private Long parentGoodsId;
    /**市场id*/
    private Long firmId;

    public Long getFirmId() {
        return firmId;
    }

    public void setFirmId(Long firmId) {
        this.firmId = firmId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Integer getOnlyExistReferencePrice() {
        return onlyExistReferencePrice;
    }

    public void setOnlyExistReferencePrice(Integer onlyExistReferencePrice) {
        this.onlyExistReferencePrice = onlyExistReferencePrice;
    }

    public Long getParentGoodsId() {
        return parentGoodsId;
    }

    public void setParentGoodsId(Long parentGoodsId) {
        this.parentGoodsId = parentGoodsId;
    }
}
