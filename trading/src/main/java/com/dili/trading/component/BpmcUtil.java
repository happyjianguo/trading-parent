package com.dili.trading.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dili.bpmc.sdk.dto.GroupUserDto;
import com.dili.bpmc.sdk.dto.TaskIdentityDto;
import com.dili.bpmc.sdk.rpc.TaskRpc;
import com.dili.ss.domain.BaseOutput;
import com.dili.uap.sdk.domain.dto.RoleUserDto;
import com.dili.uap.sdk.rpc.RoleRpc;
import com.dili.uap.sdk.session.SessionContext;

@Component
public class BpmcUtil {

	@Autowired
	private TaskRpc taskRpc;
	@Autowired
	private RoleRpc roleRpc;

	/**
	 * 设置流程属性
	 * 
	 * @param dtos
	 */
	public void fitLoggedUserIsCanHandledProcess(List<? extends ProcessHandleInfoDto> dtos) {
		// 根据流程实例批量查询任务
		Set<String> processInstanceIds = new HashSet<>();
		dtos.forEach(d -> {
			if (StringUtils.isNotBlank(d.getProcessInstanceId())) {
				processInstanceIds.add(d.getProcessInstanceId());
			}
		});
		if (CollectionUtils.isEmpty(processInstanceIds)) {
			return;
		}
		BaseOutput<List<TaskIdentityDto>> tiOutput = this.taskRpc.listTaskIdentityByProcessInstanceIds(new ArrayList<String>(processInstanceIds));
		if (!tiOutput.isSuccess()) {
			return;
		}
		if (CollectionUtils.isEmpty(tiOutput.getData())) {
			return;
		}
		Set<Long> roleIds = new HashSet<>();
		tiOutput.getData().forEach(ti -> {
			ti.getGroupUsers().forEach(gu -> {
				if (StringUtils.isNotBlank(gu.getGroupId())) {
					roleIds.add(Long.valueOf(gu.getGroupId()));
				}
			});
		});
		final List<RoleUserDto> roleUsers = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(roleIds)) {
			BaseOutput<List<RoleUserDto>> ruOutput = this.roleRpc.listRoleUserByRoleIds(roleIds);
			if (!ruOutput.isSuccess()) {
				return;
			}
			roleUsers.addAll(ruOutput.getData());
		}
		Long userId = SessionContext.getSessionContext().getUserTicket().getId();
		dtos.forEach(d -> {
			for (TaskIdentityDto taskIdentity : tiOutput.getData()) {
				if (taskIdentity.getProcessInstanceId().equals(d.getProcessInstanceId())) {
					d.setFormKey(taskIdentity.getFormKey());
					d.setTaskId(taskIdentity.getTaskId());
					if (StringUtils.isNotBlank(taskIdentity.getAssignee()) && Long.valueOf(taskIdentity.getAssignee()).equals(userId)) {
						d.setIsHandleProcess(true);
						d.setIsNeedClaim(false);
					} else {
						GroupUserDto groupUser = taskIdentity.getGroupUsers().stream().filter(gu -> {
							if (userId.toString().equals(gu.getUserId())) {
								return true;
							}
							RoleUserDto ruTarget = roleUsers.stream().filter(ru -> ru.getId().toString().equals(gu.getGroupId())).findFirst().orElse(null);
							if (ruTarget != null) {
								return ruTarget.getUsers().stream().filter(u -> u.getId().equals(userId)).findFirst().orElse(null) != null;
							}
							return false;
						}).findFirst().orElse(null);
						d.setIsHandleProcess(groupUser != null);
						d.setIsNeedClaim(groupUser != null);
					}
				}

			}
		});
	}
}
