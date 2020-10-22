package com.dili.trading.service.impl;


import com.dili.ss.domain.BaseOutput;
import com.dili.trading.service.UserService;
import com.dili.uap.sdk.domain.User;
import com.dili.uap.sdk.rpc.UserRpc;
import com.dili.uap.sdk.session.SessionContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {
    @Resource
    UserRpc userRpc;

    @Override
    public BaseOutput<List<User>> getUsersByAuthCode(String authCode) {
        return userRpc.findUsersByResourceCode(authCode, SessionContext.getSessionContext().getUserTicket().getFirmId());
    }

    @Override
    public List<Long> getPassCheckUserIdsByApp(String authCode) {
        BaseOutput<List<User>> listBaseOutput = getUsersByAuthCode(authCode);
        if (listBaseOutput.isSuccess()) {
            List<User> users = listBaseOutput.getData();
            List<Long> ids = users.stream().map(User::getId).collect(Collectors.toList());
            return ids;
        }
        return null;
    }
}
