/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.action;

import java.util.List;

import com.clustercontrol.monitor.action.DeleteInterface;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;

/**
 * カスタム監視（数値）情報を削除するクライアント側アクションクラス<br/>
 *
 * @since 4.0
 */
public class DeleteCustomNumeric implements DeleteInterface {

	/**
	 * カスタム監視（数値）情報をマネージャから削除します。<br/>
	 *
	 * @param monitorIdList 監視項目IDリスト
	 * @return 削除に成功した場合true, その他はfalse
	 */
	@Override
	public boolean delete(String managerName, List<String> monitorIdList) throws Exception{
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		return wrapper.deleteMonitor(monitorIdList);
	}
}
