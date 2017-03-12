/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.job;

import org.openhab.binding.astro.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.AstroHandlerFactory;
import org.quartz.JobDataMap;

/**
 * Job to trigger a event.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class EventJob extends AbstractBaseJob {
    public static final String KEY_EVENT = "event";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeJob(String thingUid, JobDataMap jobDataMap) {
        AstroThingHandler astroHandler = AstroHandlerFactory.getHandler(thingUid);
        if (astroHandler != null) {
            String event = jobDataMap.getString(KEY_EVENT);
            String channelId = jobDataMap.getString(KEY_CHANNEL_ID);
            astroHandler.triggerEvent(channelId, event);
        }
    }

}
