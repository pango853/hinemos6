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

package com.clustercontrol.platform.collect;

import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;

/**
 * 環境差分のあるHinemosPropertyのデフォルト値を定数として格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JdbcBatchUpsertUtil {

	private static final String SQL_BASE = "INSERT INTO %s"
			+ "(collector_id, time, avg, min, max, count) "
			+ "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT ON CONSTRAINT %s "
			+ "DO UPDATE SET avg = ?, min=?, max=?, count=?";

	public static final String SUMMARY_DAY_SQL;
	public static final String SUMMARY_HOUR_SQL;
	public static final String SUMMARY_MONTH_SQL;

	static {
		SUMMARY_DAY_SQL = String.format(SQL_BASE, "log.cc_collect_summary_day", "p_key_cc_collect_summary_day");
		SUMMARY_HOUR_SQL = String.format(SQL_BASE, "log.cc_collect_summary_hour", "p_key_cc_collect_summary_hour");
		SUMMARY_MONTH_SQL = String.format(SQL_BASE, "log.cc_collect_summary_month", "p_key_cc_collect_summary_month");
	}

	public static Object[] getParameters(CollectDataPK pk, SummaryDay entity) {
		Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getTime(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount()
		};
		return params;
	}

	public static Object[] getParameters(CollectDataPK pk, SummaryHour entity) {
		Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getTime(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount()
		};
		return params;
	}

	public static Object[] getParameters(CollectDataPK pk, SummaryMonth entity) {
		Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getTime(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount()
		};
		return params;
	}
}