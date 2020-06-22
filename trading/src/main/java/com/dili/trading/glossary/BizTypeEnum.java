package com.dili.trading.glossary;

/**
 * <B>Description</B>
 * 本软件源代码版权归农丰时代及其团队所有,未经许可不得任意复制与传播
 * <B>农丰时代科技有限公司</B>
 *
 * @author yuehongbo
 * @createTime 2018/11/8 18:43
 */
public enum BizTypeEnum {

    TRANSITION(1, "转场"),
    DEPARTURE(2, "离场");

    private String name;
    private Integer code;

    BizTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static BizTypeEnum getEnabledState(Integer code) {
        for (BizTypeEnum anEnum : BizTypeEnum.values()) {
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
