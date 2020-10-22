package com.dili.trading.service;

import java.util.List;

/**
 * 消息推送接口
 */

public interface MessageService {
    /**
     * 推送消息
     *
     * @param content
     * @param title
     * @param userIds
     */
    void pushAppMessage(String content, String title, List<Long> userIds);
}
