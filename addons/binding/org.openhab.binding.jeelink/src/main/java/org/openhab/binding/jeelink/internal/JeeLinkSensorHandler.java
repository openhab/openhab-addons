/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.jeelink.internal.config.JeeLinkSensorConfig;

/**
 * Abstract thing handler for sensors connected to a JeeLink.
 *
 * @author Volker Bier - Initial contribution
 */
public abstract class JeeLinkSensorHandler<R extends Reading> extends BaseThingHandler implements ReadingHandler<R> {
    protected String id;

    private ReadingPublisher<R> publisher;
    private long secsSinceLastReading;
    private ScheduledFuture<?> statusUpdateJob;

    public JeeLinkSensorHandler(Thing thing) {
        super(thing);
    }

    public abstract ReadingPublisher<R> createPublisher();

    @Override
    public void handleReading(R r) {
        if (r != null && id.equals(r.getSensorId())) {
            secsSinceLastReading = 0;
            updateStatus(ThingStatus.ONLINE);

            publisher.publish(r);
        }
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUid, Command command) {
    }

    @Override
    public synchronized void initialize() {
        JeeLinkHandler jlh = (JeeLinkHandler) getBridge().getHandler();
        jlh.addReadingHandler(this);

        JeeLinkSensorConfig cfg = getConfigAs(JeeLinkSensorConfig.class);
        id = cfg.sensorId;

        statusUpdateJob = createStatusUpdateJob(scheduler, cfg.sensorTimeout);

        publisher = createPublisher();

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public synchronized void dispose() {
        id = null;

        JeeLinkHandler jlh = (JeeLinkHandler) getBridge().getHandler();
        jlh.removeReadingHandler(this);

        if (statusUpdateJob != null) {
            statusUpdateJob.cancel(true);
            statusUpdateJob = null;
        }

        super.dispose();
    }

    private ScheduledFuture<?> createStatusUpdateJob(ScheduledExecutorService execService, final int sensorTimeout) {
        return execService.scheduleWithFixedDelay(() -> {
            if (secsSinceLastReading++ > sensorTimeout) {
                updateStatus(ThingStatus.OFFLINE);
            }
        }, sensorTimeout, 1, TimeUnit.SECONDS);
    }
}
