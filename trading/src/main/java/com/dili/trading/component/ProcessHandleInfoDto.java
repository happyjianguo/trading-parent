package com.dili.trading.component;

public interface ProcessHandleInfoDto extends ProcessInstanceDto {

	/**
	 * 是否可以处理流程
	 * 
	 * @return
	 */
	Boolean getIsHandleProcess();

	void setIsHandleProcess(Boolean isHandleProcess);

	/**
	 * 表单key，用于获取流程处理页面
	 * 
	 * @return
	 */
	String getFormKey();

	void setFormKey(String formKey);

	/**
	 * 任务id，如果有多条任务默认返回第一条
	 * 
	 * @return
	 */
	String getTaskId();

	void setTaskId(String taskId);
	
	/**
	 * 是否需要签收任务
	 * @return
	 */
	Boolean getIsNeedClaim();
	
	void setIsNeedClaim(Boolean isNeedClaim);

}