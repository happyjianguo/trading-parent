package com.dili.trading.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.dili.orders.config.FirmIdHolder;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;

public class PutMchIdHeaderInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String mchid = request.getHeader("mchid");
		if (StringUtils.isNotBlank(mchid)) {
			FirmIdHolder.setFirmId(mchid);
		} else {
			UserTicket user = SessionContext.getSessionContext().getUserTicket();
			if (user != null) {
				FirmIdHolder.setFirmId(user.getFirmId().toString());
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		FirmIdHolder.reset();
	}

}
