package com.dili.trading.rpc;

import com.alibaba.fastjson.JSONObject;
import com.dili.ss.constant.ResultCode;
import com.dili.ss.domain.BaseOutput;
import com.dili.ss.domain.PageOutput;
import com.dili.ss.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description： 通用RPC结果解析
 *
 * @author ：WangBo
 * @time ：2020年6月30日下午2:28:22
 */
public class GenericRpcResolver {

	static Logger log = LoggerFactory.getLogger(GenericRpcResolver.class);

	/**
	 * 判断baseOutput.isSuccess()为false则抛出异常 <br>
	 * <b>(单看异常信息可能无法知道是哪个服务出现的错误。)
	 *
	 * @param <T>
	 * @param baseOutput
	 * @return
	 */
	@Deprecated()
	public static <T> T resolver(BaseOutput<T> baseOutput) {
		if (!baseOutput.isSuccess()) {
			log.error("远程服务返回了一个错误![{}]", JSONObject.toJSONString(baseOutput));
			throw new BusinessException(ResultCode.DATA_ERROR, baseOutput.getMessage());
		}
		return baseOutput.getData();
	}

	/**
	 * 判断baseOutput.isSuccess()为false则抛出异常
	 * @param <T>
	 * @param baseOutput
	 * @param serviceName 服务名或功能用于日志记录
	 * @return
	 */
	public static <T> T resolver(BaseOutput<T> baseOutput, String serviceName) {
		if (!baseOutput.isSuccess()) {
			log.error("{}远程服务返回了一个错误![{}]", serviceName, JSONObject.toJSONString(baseOutput));
			throw new BusinessException(baseOutput.getCode(), baseOutput.getMessage());
		}
		return baseOutput.getData();
	}


	/**
	 * 判断baseOutput.isSuccess()为false则抛出异常，并返回远程服务原始错误码及提示信息,目前错误码都是200为成功，非200则失败
	 * @param <T>
	 * @param baseOutput
	 * @param serviceName 服务名或功能用于日志记录
	 * @return
	 */
	public static <T> PageOutput<T> resolver(PageOutput<T> baseOutput, String serviceName) {
		if (!baseOutput.isSuccess()) {
			log.error("{}远程服务返回了一个错误![{}]", serviceName, JSONObject.toJSONString(baseOutput));
			throw new BusinessException(baseOutput.getCode(), baseOutput.getMessage());
		}
		return baseOutput;
	}

}
