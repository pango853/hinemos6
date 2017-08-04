/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.infra.model.FileTransferModuleInfo;
import com.clustercontrol.infra.model.FileTransferVariableInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.util.HinemosTime;


/**
 * 環境構築情報を更新する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ModifyInfraManagement {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyInfraManagement.class );

	public boolean add(InfraManagementInfo info, String user) throws HinemosUnknown, NotifyDuplicate, InvalidRole, InvalidSetting, EntityExistsException {
		m_log.debug("add() : start");

		// 環境情報を登録
		if(m_log.isDebugEnabled())
			m_log.debug("add() : " + info.toString());
		
		JpaTransactionManager jtm = new JpaTransactionManager();
		jtm.checkEntityExists(InfraManagementInfo.class, info.getManagementId());
		
		long now = HinemosTime.currentTimeMillis();
		info.setNotifyGroupId(NotifyGroupIdGenerator.generate(info));
		info.setRegDate(now);
		info.setRegUser(user);
		info.setUpdateDate(now);
		info.setUpdateUser(user);
		
		// コピーの場合に構築IDを更新する
		for (InfraModuleInfo<?> module : info.getModuleList()) {
			module.setManagementId(info.getManagementId());
			if (module instanceof FileTransferModuleInfo) {
				if (((FileTransferModuleInfo)module).getFileTransferVariableList() != null
						|| ((FileTransferModuleInfo)module).getFileTransferVariableList().size() > 0) {
					for (FileTransferVariableInfo variableInfo 
							: ((FileTransferModuleInfo)module).getFileTransferVariableList()) {
						variableInfo.setManagementId(info.getManagementId());
						variableInfo.setModuleId(module.getModuleId());
					}
				}
			}
		}
		
		info.persistSelf(jtm.getEntityManager());

		// 通知情報の登録
		if (info.getNotifyRelationList() != null
				&& info.getNotifyRelationList().size() > 0) {
			for (NotifyRelationInfo notifyRelationInfo : info.getNotifyRelationList()) {
				notifyRelationInfo.setNotifyGroupId(info.getNotifyGroupId());
			}
			// 通知情報を登録
			new ModifyNotifyRelation().add(info.getNotifyRelationList());
		}

		m_log.debug("add() : end");
		return true;
	}
	
	/**
	 * 環境構築情報を変更します。
	 */
	public boolean modify(InfraManagementInfo webEntity, String user) throws InfraManagementNotFound, NotifyDuplicate, NotifyNotFound, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("modify() : start");
		
		m_log.debug("modify() : " + webEntity.toString());
		
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		InfraManagementInfo entity = em.find(InfraManagementInfo.class, webEntity.getManagementId(), ObjectPrivilegeMode.MODIFY);
		if (entity == null) {
			InfraManagementNotFound e = new InfraManagementNotFound(
					webEntity.getManagementId(),
					"InfraManagementInfoEntity.findByPrimaryKey, " + "managementId = " + webEntity.getManagementId());
			m_log.info("InfraManagementInfoEntity.findByPrimaryKey : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		entity.setName(webEntity.getName());
		entity.setDescription(webEntity.getDescription());
		entity.setFacilityId(webEntity.getFacilityId());
		entity.setValidFlg(webEntity.getValidFlg());
		entity.setNotifyGroupId(NotifyGroupIdGenerator.generate(entity));
		entity.setStartPriority(webEntity.getStartPriority());
		entity.setNormalPriorityRun(webEntity.getNormalPriorityRun());
		entity.setAbnormalPriorityRun(webEntity.getAbnormalPriorityRun());
		entity.setNormalPriorityCheck(webEntity.getNormalPriorityCheck());
		entity.setAbnormalPriorityCheck(webEntity.getAbnormalPriorityRun());

		// 通知情報を更新
		if (webEntity.getNotifyRelationList() != null
				&& webEntity.getNotifyRelationList().size() > 0) {
			for (NotifyRelationInfo notifyRelationInfo : webEntity.getNotifyRelationList()) {
				notifyRelationInfo.setNotifyGroupId(entity.getNotifyGroupId());
			}
		}
		new NotifyControllerBean().modifyNotifyRelation(
				webEntity.getNotifyRelationList(), entity.getNotifyGroupId());

		List<InfraModuleInfo<?>> webModuleList = new ArrayList<InfraModuleInfo<?>>(webEntity.getModuleList());
		List<InfraModuleInfo<?>> moduleList = new ArrayList<InfraModuleInfo<?>>(entity.getModuleList());
		
		int orderNo = 0;
		Iterator<InfraModuleInfo<?>> webModuleIter = webModuleList.iterator();
		while (webModuleIter.hasNext()) {
			InfraModuleInfo<?> mi = webModuleIter.next();
			
			Iterator<InfraModuleInfo<?>> moduleIter = moduleList.iterator();
			while (moduleIter.hasNext()) {
				InfraModuleInfo<?> module = moduleIter.next();
				if (mi.getModuleId().equals(module.getId().getModuleId())) {
					mi.modifyCounterEntity(entity, module, orderNo);
					
					webModuleIter.remove();
					moduleIter.remove();
					break;
				}
			}
			orderNo++;
		}
		
		for (InfraModuleInfo<?> webModule: webModuleList) {
			webModule.addCounterEntity(entity);
		}
		
		for (InfraModuleInfo<?> module: moduleList) {
			entity.getModuleList().remove(module);
			module.removeSelf(em);
		}
		
		entity.setUpdateDate(HinemosTime.currentTimeMillis());
		entity.setUpdateUser(user);
		
		m_log.debug("modify() : end");
		return true;
	}
	
	/**
	 * 環境構築情報を削除します。
	 */
	public boolean delete(String managementId) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		m_log.debug("delete() : start");
		
		m_log.debug(String.format("delete() : managementId = %s", managementId));

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 監視情報を取得
		InfraManagementInfo entity = null;
		try {
			entity = em.find(InfraManagementInfo.class, managementId, ObjectPrivilegeMode.MODIFY);
			if (entity == null) {
				InfraManagementNotFound e = new InfraManagementNotFound("InfraManagementInfo.findByPrimaryKey" + ", managementId = " + managementId);
				m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("delete() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}

		// 監視グループ情報を削除
		new NotifyControllerBean().deleteNotifyRelation(entity.getNotifyGroupId());

		// 監視情報を削除
		entity.removeSelf(em);
		m_log.debug("delete() : end");
		
		return true;
	}
}