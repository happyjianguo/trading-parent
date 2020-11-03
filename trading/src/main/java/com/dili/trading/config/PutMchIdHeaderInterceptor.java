package com.dili.trading.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.dili.orders.config.ClientIpHolder;
import com.dili.orders.config.FirmIdHolder;
import com.dili.uap.sdk.domain.UserTicket;
import com.dili.uap.sdk.session.SessionContext;

public class PutMchIdHeaderInterceptor extends HandlerInterceptorAdapter {

	public static String getRemoteIP(HttpServletRequest request) {
		String ip = null;
		if ((request.getHeader("x-forwarded-for") != null) && (!"unknown".equalsIgnoreCase(request.getHeader("x-forwarded-for")))) {
			ip = request.getHeader("x-forwarded-for");
		} else {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String mchid = request.getHeader("mchid");
		String ip = getRemoteIP(request);
		if (StringUtils.isNotBlank(mchid)) {
			FirmIdHolder.setFirmId(mchid);
		} else {
			UserTicket user = SessionContext.getSessionContext().getUserTicket();
			if (user != null) {
				FirmIdHolder.setFirmId(user.getFirmId().toString());
			}
		}
		if (StringUtils.isNotBlank(ip)) {
			ClientIpHolder.setIp(ip);
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		FirmIdHolder.reset();
	}

}
