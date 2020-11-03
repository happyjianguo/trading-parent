package com.dili.trading.service.impl;

import com.dili.trading.service.MessageService;
import com.dili.uap.sdk.session.SessionContext;
import com.diligrp.message.sdk.domain.input.AppPushInput;
import com.diligrp.message.sdk.enums.PushPlatformEnum;
import com.diligrp.message.sdk.rpc.AppPushRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    AppPushRpc appPushRpc;

    @Override
    public void pushAppMessage(String content, String title, List<Long> userIds, String code, String id) {
        if (userIds == null) {
            return;
        }
        AppPushInput appPushInput = new AppPushInput();
        appPushInput.setAlert(content);
        appPushInput.setTitle(title);
//        appPushInput.setPlatform(PushPlatformEnum.Android.getValue());
        Set<Long> users = new HashSet(userIds);
        appPushInput.setUserIds(users);
        appPushInput.setMarketId(SessionContext.getSessionContext().getUserTicket().getFirmId());
        appPushInput.setExtraMap(new HashMap<String, String>() {
            {
                put("moduleCode", code);
                put("id", id);
            }
        });
        appPushRpc.receiveMessage(appPushInput);
    }

}
