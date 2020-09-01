package com.dili.trading.dto;

import javax.persistence.Column;

import com.dili.orders.domain.PriceApproveRecord;
import com.dili.trading.component.ProcessHandleInfoDto;

public class PriceApproveRecordProcessDto extends PriceApproveRecord implements ProcessHandleInfoDto {

	private Boolean isHandleProcess;
	private String formKey;
	private String taskId;
	private Boolean isNeedClaim;

	public Boolean getIsHandleProcess() {
		return isHandleProcess;
	}

	public void setIsHandleProcess(Boolean isHandleProcess) {
		this.isHandleProcess = isHandleProcess;
	}

	public String getFormKey() {
		return formKey;
	}

	public void setFormKey(String formKey) {
		this.formKey = formKey;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Boolean getIsNeedClaim() {
		return isNeedClaim;
	}

	public void setIsNeedClaim(Boolean isNeedClaim) {
		this.isNeedClaim = isNeedClaim;
	}

}
