package com.dili.trading.glossary;

/**
 * 转离场申请单的审批状态  审批状态（1.待审核/2.通过/3.拒绝）默认为1
 */
public enum ApplyEnum {
    TOBEREVIEWED(1, "待审核"),
    ADOPT(2, "通过"),
    REFUSED(3, "拒绝");

    private String name;
    private Integer code;

    ApplyEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ApplyEnum getEnabledState(Integer code) {
        for (ApplyEnum anEnum : ApplyEnum.values()) {
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
