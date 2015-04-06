/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Baseclass for all jobs with common methods.
 * 
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class AbstractBaseJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBaseJob.class);

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();

        String thingUid = jobDataMap.getString("thingUid");
        if (logger.isDebugEnabled()) {
            logger.debug("Starting astro {} for thing {}", this.getClass().getSimpleName(), thingUid);
        }

        executeJob(thingUid);
    }

    /**
     * Method to override by the different jobs to be executed.
     */
    protected abstract void executeJob(String thingUid);

}
