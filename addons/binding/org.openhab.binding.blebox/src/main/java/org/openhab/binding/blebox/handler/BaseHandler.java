/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.blebox.internal.BleboxDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseHandler} is base class for device specified handlers.
 *
 * @author Szymon Tokarski - Initial contribution
 */
abstract class BaseHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(BaseHandler.class);

    abstract void initializeDevice(String ipAddress);

    abstract void updateDeviceStatus();

    public BaseHandler(Thing thing) {
        super(thing);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateDeviceStatus();
        }
    };
    private ScheduledFuture<?> pollingJob;

    @Override
    public void initialize() {
        logger.debug("Initializing handler for {}", getClass().getName());
        BleboxDeviceConfiguration config = getConfigAs(BleboxDeviceConfiguration.class);

        initializeDevice(config.ip);
        updateStatus(ThingStatus.ONLINE);

        int pollingInterval = (config.pollingInterval != null) ? config.pollingInterval.intValue()
                : BleboxDeviceConfiguration.DEFAULT_POLL_INTERVAL;

        pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, pollingInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
    }
}
