package com.dili.trading.glossary;

/**
 * 转离场结算单支付状态（1.未结算/2.已结算/3.已撤销/4.已关闭）
 */
public enum PayStatusEnum {
    UNSETTLED(1, "未结算"),
    SETTLED(2, "已结算"),
    RESCINDED(3, "已撤销"),
    CLOSED(4, "已撤销");

    private String name;
    private Integer code;

    PayStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PayStatusEnum getEnabledState(Integer code) {
        for (PayStatusEnum anEnum : PayStatusEnum.values()) {
            if (anEnum.getCode().equals(code)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
