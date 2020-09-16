package com.dili.trading.rpc;

import java.util.Map;

import com.dili.ss.domain.BaseOutput;
import com.dili.ss.retrofitful.annotation.POST;
import com.dili.ss.retrofitful.annotation.Restful;
import com.dili.ss.retrofitful.annotation.VOBody;

@Restful("${uap.contextPath}")
public interface AuthenticationRpc {

	@POST("/authenticationApi/loginout.api")
	BaseOutput<Object> loginout(@VOBody Map<String, String> sessionIdMap);

}
