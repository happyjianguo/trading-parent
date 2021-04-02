package com.dili.trading.rpc;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.retrofitful.annotation.POST;
import com.dili.ss.retrofitful.annotation.ReqParam;
import com.dili.ss.retrofitful.annotation.Restful;

@Restful("${uap.contextPath}")
public interface AuthenticationRpc {

	@POST("/authenticationApi/logout")
	BaseOutput<Object> logout(@ReqParam("refreshToken") String refreshToken);

}
