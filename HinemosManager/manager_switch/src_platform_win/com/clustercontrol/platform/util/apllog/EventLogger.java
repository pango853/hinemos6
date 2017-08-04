/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform.util.apllog;

import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.util.HinemosMessage;

public class EventLogger {
	private static final Log LOG = LogFactory.getLog("WinEvent");
	
	private static enum Level {
		INFO,
		WARN,
		ERROR
	}
	
	public static void internal(Integer priority, OutputBasicInfo info) {
		/**	イベントログ出力用フォーマット「日付  プラグインID,アプリケーション,監視項目ID,メッセージID,ファシリティID,メッセージ,詳細メッセージ」 */
		MessageFormat logfmt = new MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}  {1},{2},{3},{4},{5},{6}");
		// Locale取得
		Locale locale = NotifyUtil.getNotifyLocale();
		//メッセージを編集
		Object[] args ={info.getGenerationDate(),info.getPluginId(), info.getApplication(),
				info.getMonitorId(), PriorityConstant.typeToMessageCode(info.getPriority()),
				HinemosMessage.replace(info.getMessage(), locale), info.getMessageOrg()};
		String logmsg = logfmt.format(args);
		
		switch (priority) {
		case PriorityConstant.TYPE_CRITICAL:
			error(logmsg);
			break;
		case PriorityConstant.TYPE_WARNING:
			warn(logmsg);
			break;
		case PriorityConstant.TYPE_INFO:
			info(logmsg);
			break;
		default:
			break;
		}
	}

	public static void info(Object o) {
		put(Level.INFO, o, null);
	}
	
	public static void info(Object o, Throwable t) {
		put(Level.INFO, o, t);
	}
	
	public static void warn(Object o) {
		put(Level.WARN, o, null);
	}
	
	public static void warn(Object o, Throwable t) {
		put(Level.WARN, o, t);
	}
	
	public static void error(Object o) {
		put(Level.ERROR, o, null);
	}
	
	public static void error(Object o, Throwable t) {
		put(Level.ERROR, o, t);
	}
	
	private static void put(Level level, Object o, Throwable t) {
		boolean isOutput = HinemosPropertyUtil.getHinemosPropertyBool("windows.eventlog", HinemosPropertyDefault.getBoolean(HinemosPropertyDefault.BooleanKey.WINDOWS_EVENTLOG));
		if (!isOutput) {
			return;
		}
		
		switch (level) {
			case INFO:
				if (t != null) {
					LOG.info(o, t);
				} else {
					LOG.info(o);
				}
				break;
				
			case WARN:
				if (t != null) {
					LOG.warn(o, t);
				} else {
					LOG.warn(o);
				}
				break;
				
			case ERROR:
				if (t != null) {
					LOG.error(o, t);
				} else {
					LOG.error(o);
				}
				break;
				
			default:
				break;
				
			}
		
	}
}
