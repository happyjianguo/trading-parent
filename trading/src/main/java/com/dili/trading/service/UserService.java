package com.dili.trading.service;

import com.dili.ss.domain.BaseOutput;
import com.dili.uap.sdk.domain.User;

import java.util.List;

/**
 * 权限用户
 */
public interface UserService {
    /**
     * @param authCode
     * @return
     */

    BaseOutput<List<User>> getUsersByAuthCode(String authCode);

    /**
     * @param
     * @return
     */
    List<Long> getPassCheckUserIdsByApp(String authCode);
}
