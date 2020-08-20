package com.dili.trading.service.impl;

import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.glossary.LoggerConstant;
import com.dili.orders.domain.GoodsReferencePriceSetting;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.GoodsReferencePriceSettingRpc;
import com.dili.trading.service.GoodsReferencePriceSettingService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class GoodsReferencePriceSettingServiceImpl implements GoodsReferencePriceSettingService {

    @Autowired
    private GoodsReferencePriceSettingRpc goodsReferencePriceSettingRpc;

    /**
     * 新增
     *
     * @param goodsReferencePriceSetting
     * @return
     */
    @Override
    @BusinessLogger(businessType = "trading_orders", content = "品类参考价新增", operationType = "add", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput<GoodsReferencePriceSetting> insertGoodsReferencePriceSetting(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置创建人id
        goodsReferencePriceSetting.setCreatorId(userTicket.getId());
        //设置数据创建时间
        goodsReferencePriceSetting.setCreatedTime(LocalDateTime.now());

        BaseOutput<GoodsReferencePriceSetting> goodsReferencePriceSettingBaseOutput = goodsReferencePriceSettingRpc.insert(goodsReferencePriceSetting);
        if (goodsReferencePriceSettingBaseOutput.isSuccess()) {
            GoodsReferencePriceSetting data = goodsReferencePriceSettingBaseOutput.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return goodsReferencePriceSettingBaseOutput;
    }

    /**
     * 修改
     *
     * @param goodsReferencePriceSetting
     * @return
     */
    @Override
    @BusinessLogger(businessType = "trading_orders", content = "品类参考价修改", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput<GoodsReferencePriceSetting> updateGoodsReferencePriceSetting(GoodsReferencePriceSetting goodsReferencePriceSetting) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置修改人id
        goodsReferencePriceSetting.setModifierId(userTicket.getId());
        //设置数据修改时间
        goodsReferencePriceSetting.setModifiedTime(LocalDateTime.now());

        BaseOutput<GoodsReferencePriceSetting> goodsReferencePriceSettingBaseOutput = goodsReferencePriceSettingRpc.update(goodsReferencePriceSetting);
        if (goodsReferencePriceSettingBaseOutput.isSuccess()) {
            GoodsReferencePriceSetting data = goodsReferencePriceSettingBaseOutput.getData();
            LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
            LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
            LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        }
        return goodsReferencePriceSettingBaseOutput;
    }
}
