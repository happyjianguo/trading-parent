package com.dili.trading.service.impl;

import com.dili.jmsf.microservice.sdk.dto.VehicleAccessDTO;
import com.dili.logger.sdk.annotation.BusinessLogger;
import com.dili.logger.sdk.base.LoggerContext;
import com.dili.logger.sdk.glossary.LoggerConstant;
import com.dili.orders.domain.TransitionDepartureApply;
import com.dili.orders.domain.TransitionDepartureSettlement;
import com.dili.orders.dto.*;
import com.dili.orders.rpc.AccountRpc;
import com.dili.orders.rpc.PayRpc;
import com.dili.orders.rpc.UidRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.trading.rpc.JmsfRpc;
import com.dili.trading.rpc.TransitionDepartureApplyRpc;
import com.dili.trading.rpc.TransitionDepartureSettlementRpc;
import com.dili.trading.service.TransitionDepartureSettlementService;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class TransitionDepartureSettlementServiceImpl implements TransitionDepartureSettlementService {

    @Autowired
    private TransitionDepartureApplyRpc transitionDepartureApplyRpc;

    @Autowired
    private TransitionDepartureSettlementRpc transitionDepartureSettlementRpc;

    @Autowired
    private UidRpc uidRpc;

    @Autowired
    private JmsfRpc jmsfRpc;

    @Autowired
    private AccountRpc accountRpc;

    @Autowired
    private PayRpc payRpc;

    /**
     * 根据申请单信息，新增一条结算单信息
     *
     * @param transitionDepartureSettlement
     * @return
     */
    @Override
    @BusinessLogger(businessType = "trading_orders", content = "转离场结算单新增", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput insert(TransitionDepartureSettlement transitionDepartureSettlement) {
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置创建时间
        transitionDepartureSettlement.setCreateTime(LocalDateTime.now());
        //设置操作员id
        transitionDepartureSettlement.setOperatorId(userTicket.getId());
        //设置操作员姓名
        transitionDepartureSettlement.setOperatorName(userTicket.getRealName());
        //设置操作员登录用户名
        transitionDepartureSettlement.setOperatorCode(userTicket.getUserName());
        //设置支付状态为未结算
        transitionDepartureSettlement.setPayStatus(1);
        //根据申请单id拿到申请单，修改申请单的支付状态为1（未结算）
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureSettlement.getApplyId());
        //盘点是否请求成功
        if (oneByID.isSuccess()) {
            //判断是否存在申请单
            if (Objects.nonNull(oneByID.getData())) {
                TransitionDepartureApply data = oneByID.getData();
                data.setPayStatus(1);
                BaseOutput update = transitionDepartureApplyRpc.update(data);
                //判断是否更新失败，如果更新失败，那就抛出异常
                if (!update.isSuccess()) {
                    throw new RuntimeException();
                }
            } else {
                return BaseOutput.failure("操作失败，申请单不存在");
            }
        } else {
            return BaseOutput.failure("操作失败");
        }
        //更新完成之后，插入缴费单信息，必须在这之前发起请求，到支付系统，拿到支付单号
        PaymentTradePrepareDto paymentTradePrepareDto = new PaymentTradePrepareDto();
        BaseOutput<UserAccountCardResponseDto> oneAccountCard = accountRpc.getOneAccountCard(transitionDepartureSettlement.getCustomerCardNo());
        if (!oneAccountCard.isSuccess()) {
            throw new RuntimeException("转离场结算单新增-->根据卡号获取账户信息失败");
        }
        //请求与支付，两边的账户id对应关系如下
        paymentTradePrepareDto.setAccountId(oneAccountCard.getData().getFundAccountId());
        paymentTradePrepareDto.setType(12);
        paymentTradePrepareDto.setBusinessId(oneAccountCard.getData().getAccountId());
        paymentTradePrepareDto.setAmount(transitionDepartureSettlement.getChargeAmount());
        BaseOutput<CreateTradeResponseDto> prepare = payRpc.prepare(paymentTradePrepareDto);
        if (!prepare.isSuccess()) {
            throw new RuntimeException("转离场结算单新增-->创建交易失败");
        }
        //设置交易单号
        transitionDepartureSettlement.setPaymentNo(prepare.getData().getTradeId());
        //根据uid设置结算单的code
        transitionDepartureSettlement.setCode(uidRpc.getCode().getData());
        BaseOutput<TransitionDepartureSettlement> update = transitionDepartureSettlementRpc.insert(transitionDepartureSettlement);
        //更新结算单不成功的时候
        if (!update.isSuccess()) {
            throw new RuntimeException("转离场结算单新增-->结算单更新失败");
        }
        TransitionDepartureSettlement data = update.getData();
        LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, data.getId());
        LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, data.getCode());
        LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
        LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        return BaseOutput.success();
    }

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "转离场结算单撤销", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput revocator(TransitionDepartureSettlement transitionDepartureSettlement) {
        //判断结算单的支付状态是否为2（已结算）,不是则直接返回
        if (transitionDepartureSettlement.getPayStatus() != 1) {
            return BaseOutput.failure("只有已结算的结算单可以撤销");
        }
        //设置为已撤销的支付状态
        transitionDepartureSettlement.setPayStatus(3);
        //根据结算单的apply_id拿到申请单信息
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureSettlement.getApplyId());
        if (!oneByID.isSuccess()) {
            return BaseOutput.failure(oneByID.getMessage());
        }
        if (Objects.isNull(oneByID.getData())) {
            return BaseOutput.failure("申请单不存在");
        }
        TransitionDepartureApply data = oneByID.getData();
        //设置申请单支付状态为已撤销
        data.setPayStatus(3);
        //先更新申请单，判断是否更新成功，没有更新成功则抛出异常
        BaseOutput update = transitionDepartureApplyRpc.update(data);
        if (!update.isSuccess()) {
            throw new RuntimeException("转离场结算单撤销-->申请单更新失败");
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //修改结算单的支付状态
        //设置撤销人员相关信息
        transitionDepartureSettlement.setRevocatorId(userTicket.getId());
        transitionDepartureSettlement.setRevocatorName(userTicket.getRealName());
        transitionDepartureSettlement.setRevocatorTime(LocalDateTime.now());
        BaseOutput update1 = transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
        //判断结算单修改是否成功，不成功则抛出异常
        if (!update1.isSuccess()) {
            throw new RuntimeException("转离场结算单撤销-->结算单更新失败");
        }
        //再掉卡务撤销交易
        BaseOutput<UserAccountCardResponseDto> oneAccountCard = accountRpc.getOneAccountCard(transitionDepartureSettlement.getCustomerCardNo());
        //判断调用卡号拿到账户信息是否成功
        if (!oneAccountCard.isSuccess()) {
            throw new RuntimeException("转离场结算单撤销-->调用卡号拿到账户失败");
        }
        //设置撤销交易的dto
        PaymentTradeCommitDto paymentTradeCommitDto = new PaymentTradeCommitDto();
        //设置资金账户
        paymentTradeCommitDto.setAccountId(oneAccountCard.getData().getFundAccountId());
        //设置密码
//        paymentTradeCommitDto.setPassword(password);
        //设置交易号
        paymentTradeCommitDto.setTradeId(transitionDepartureSettlement.getPaymentNo());
        BaseOutput<PaymentTradeCommitResponseDto> paymentTradeCommitResponseDtoBaseOutput = payRpc.cancel2(paymentTradeCommitDto);
        if (!paymentTradeCommitResponseDtoBaseOutput.isSuccess()) {
            throw new RuntimeException("转离场结算单撤销-->调用撤销交易rpc失败");
        }
        LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, transitionDepartureSettlement.getId());
        LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, transitionDepartureSettlement.getCode());
        LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
        LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        return BaseOutput.success();
    }

    @Override
    @BusinessLogger(businessType = "trading_orders", content = "转离场结算单支付", operationType = "update", systemCode = "ORDERS")
    @Transactional(propagation = Propagation.REQUIRED)
    public BaseOutput pay(TransitionDepartureSettlement transitionDepartureSettlement, String password) {
        //判断结算单的支付状态是否为1（未结算）,不是则直接返回
        if (transitionDepartureSettlement.getPayStatus() != 1) {
            return BaseOutput.failure("只有未结算的结算单可以结算");
        }
        //设置为已支付状态
        transitionDepartureSettlement.setPayStatus(2);
        //根据结算单apply_id获取到对应申请单
        BaseOutput<TransitionDepartureApply> oneByID = transitionDepartureApplyRpc.getOneByID(transitionDepartureSettlement.getApplyId());
        //判断请求是否成功
        if (!oneByID.isSuccess()) {
            return BaseOutput.failure(oneByID.getMessage());
        }
        //判断申请单是否存在
        if (Objects.isNull(oneByID.getData())) {
            return BaseOutput.failure("申请单不存在");
        }
        //获取到申请单
        TransitionDepartureApply data = oneByID.getData();
        //设置申请单支付状态为已支付
        data.setPayStatus(2);
        BaseOutput update = transitionDepartureApplyRpc.update(data);
        //如果申请单更新失败，则抛出异常
        if (!update.isSuccess()) {
            throw new RuntimeException("转离场结算单支付-->申请单更新失败");
        }
        transitionDepartureSettlement.setPayTime(LocalDateTime.now());
        //修改结算单的支付状态
        BaseOutput update1 = transitionDepartureSettlementRpc.update(transitionDepartureSettlement);
        //如果结算单修改失败，则抛出异常
        if (!update1.isSuccess()) {
            throw new RuntimeException("转离场结算单支付-->结算单更新失败");
        }
        //再调用支付
        //首先根据卡号拿倒账户信息
        BaseOutput<UserAccountCardResponseDto> oneAccountCard = accountRpc.getOneAccountCard(transitionDepartureSettlement.getCustomerCardNo());
        if (!oneAccountCard.isSuccess()) {
            throw new RuntimeException("转离场结算单支付-->查询账户失败");
        }
        //构建支付对象
        UserAccountCardResponseDto userAccountCardResponseDto = oneAccountCard.getData();
        PaymentTradeCommitDto paymentTradeCommitDto = new PaymentTradeCommitDto();
        //设置自己账户id
        paymentTradeCommitDto.setAccountId(userAccountCardResponseDto.getFundAccountId());
        //设置账户id
        paymentTradeCommitDto.setBusinessId(userAccountCardResponseDto.getAccountId());
        //设置密码
        paymentTradeCommitDto.setPassword(password);
        //设置交易单号
        paymentTradeCommitDto.setTradeId(transitionDepartureSettlement.getPaymentNo());
        //设置费用
        List<FeeDto> feeDtos = new ArrayList();
        FeeDto feeDto = new FeeDto();
        feeDto.setAmount(transitionDepartureSettlement.getChargeAmount());
        feeDto.setType(31);
        feeDto.setTypeName("转离场收费");
        feeDtos.add(feeDto);
        paymentTradeCommitDto.setFees(feeDtos);
        //调用rpc支付
        BaseOutput<PaymentTradeCommitResponseDto> pay = payRpc.commit4(paymentTradeCommitDto);
        if (!pay.isSuccess()) {
            throw new RuntimeException("转离场结算单支付-->支付rpc请求失败");
        }
        UserTicket userTicket = SessionContext.getSessionContext().getUserTicket();
        //设置进门收费相关信息，并调用新增
        VehicleAccessDTO vehicleAccessDTO = new VehicleAccessDTO();
        vehicleAccessDTO.setMarketId(userTicket.getFirmId());
        vehicleAccessDTO.setPlateNumber(data.getPlate());
        vehicleAccessDTO.setVehicleTypeId(data.getCarTypeId());
        vehicleAccessDTO.setBarrierType(3);
        vehicleAccessDTO.setEntryTime(new Date());
        vehicleAccessDTO.setAmount(transitionDepartureSettlement.getChargeAmount());
        vehicleAccessDTO.setPayType(3);
        vehicleAccessDTO.setCasherId(userTicket.getId());
        vehicleAccessDTO.setCasherName(userTicket.getRealName());
        vehicleAccessDTO.setCasherDepartmentId(userTicket.getDepartmentId());
        vehicleAccessDTO.setPayTime(new Date());
        vehicleAccessDTO.setOperatorId(userTicket.getId());
        vehicleAccessDTO.setOperatorName(userTicket.getRealName());
        vehicleAccessDTO.setRemark(data.getTransitionDepartureReason());
        vehicleAccessDTO.setCreated(new Date());
        BaseOutput<VehicleAccessDTO> vehicleAccessDTOBaseOutput = jmsfRpc.add(vehicleAccessDTO);
        if (!vehicleAccessDTOBaseOutput.isSuccess()) {
            throw new RuntimeException("进门收费单-->新增失败");
        }
        LoggerContext.put(LoggerConstant.LOG_BUSINESS_ID_KEY, transitionDepartureSettlement.getId());
        LoggerContext.put(LoggerConstant.LOG_BUSINESS_CODE_KEY, transitionDepartureSettlement.getCode());
        LoggerContext.put(LoggerConstant.LOG_OPERATOR_ID_KEY, userTicket.getId());
        LoggerContext.put(LoggerConstant.LOG_MARKET_ID_KEY, userTicket.getFirmId());
        return BaseOutput.success();
    }

}
