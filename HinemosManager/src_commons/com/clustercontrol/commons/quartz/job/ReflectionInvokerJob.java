/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.quartz.job;

import java.io.Serializable;

import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.plugin.util.scheduler.Job;
import com.clustercontrol.plugin.util.scheduler.JobDataMap;
import com.clustercontrol.plugin.util.scheduler.JobDetail;
import com.clustercontrol.plugin.util.scheduler.JobExecutionException;

public class ReflectionInvokerJob implements Job {

	public static final String KEY_CLASS_NAME = "CLASS_NAME";
	public static final String KEY_METHOD_NAME = "METHOD_NAME";
	public static final String KEY_ARGS_TYPE = "ARGS_TYPE";
	public static final String KEY_ARGS = "ARGS";
	public static final String KEY_RESET_ON_RESTART = "RESET_ON_RESTART";

	@Override
	public void execute(JobDetail jd) throws JobExecutionException {
		JobDataMap dmap = jd.getJobDataMap();

		String className = dmap.getString(KEY_CLASS_NAME);
		String methodName = dmap.getString(KEY_METHOD_NAME);
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] argsType = (Class<? extends Serializable>[])dmap.get(KEY_ARGS_TYPE);
		Serializable[] args = (Serializable[])dmap.get(KEY_ARGS);

		MonitoredThreadPoolExecutor.beginTask(Thread.currentThread(), className);

		try {
			// ThreadLocalの初期化
			HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);

			IScheduledTaskExecutor exec = ScheduledTaskExecutorFactory.instance().create();
			exec.execute(className, methodName, argsType, args);
		} finally {
			MonitoredThreadPoolExecutor.finishTask(Thread.currentThread());
		}
	}

}