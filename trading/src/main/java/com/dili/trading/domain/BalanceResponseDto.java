package com.dili.trading.domain;


/**
 * 余额查询返回dto
 *
 * @author xuliang
 */
public class BalanceResponseDto {
    /**
     * 账户ID
     */
    private Long accountId;
    /**
     * 余额
     */
    private Long balance;
    /**
     * 冻结余额
     */
    private Long frozenAmount;
    /**
     * 可用余额
     */
    private Long availableAmount;

    /**
     * @return
     */
    public Long getAccountId() {
        return accountId;
    }

    /**
     * @param accountId
     */
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    /**
     * @return
     */
    public Long getBalance() {
        return balance;
    }

    /**
     * @param balance
     */
    public void setBalance(Long balance) {
        this.balance = balance;
    }

    /**
     * @return
     */
    public Long getFrozenAmount() {
        return frozenAmount;
    }

    /**
     * @param frozenAmount
     */
    public void setFrozenAmount(Long frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    /**
     * @return
     */
    public Long getAvailableAmount() {
        return availableAmount;
    }

    /**
     * @param availableAmount
     */
    public void setAvailableAmount(Long availableAmount) {
        this.availableAmount = availableAmount;
    }
}
