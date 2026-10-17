/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.jeelink.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jeelink.internal.config.JeeLinkSensorConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract thing handler for sensors connected to a JeeLink.
 *
 * @author Volker Bier - Initial contribution
 */
public abstract class JeeLinkSensorHandler<R extends Reading> extends BaseThingHandler implements ReadingHandler<R> {
    private final Logger logger = LoggerFactory.getLogger(JeeLinkSensorHandler.class);
    protected @Nullable JeeLinkHandler registeredBridge;
    protected String id;
    protected final String sensorType;

    private ReadingPublisher<R> publisher;
    private long secsSinceLastReading;
    private ScheduledFuture<?> statusUpdateJob;

    public JeeLinkSensorHandler(Thing thing, String sensorType) {
        super(thing);
        this.sensorType = sensorType;
    }

    public abstract ReadingPublisher<R> createPublisher();

    @Override
    public String getSensorType() {
        return sensorType;
    }

    @Override
    public void handleReading(R r) {
        if (r != null && id.equals(r.getSensorId())) {
            secsSinceLastReading = 0;
            updateStatus(ThingStatus.ONLINE);

            if (publisher != null) {
                publisher.publish(r);
            }
        }
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUid, Command command) {
    }

    @Override
    public synchronized void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Bridge unavailable while initializing Thing: {}", thing.getUID());
            return;
        }
        if (!(bridge.getHandler() instanceof JeeLinkHandler jlh)) {
            logger.debug("Bridge handler unavailable while initializing Thing: {}", thing.getUID());
            return;
        }
        JeeLinkHandler previousBridge = registeredBridge;
        if (!jlh.equals(previousBridge)) {
            if (previousBridge != null) {
                previousBridge.removeReadingHandler(this);
            }
            jlh.addReadingHandler(this);
        }
        registeredBridge = jlh;

        JeeLinkSensorConfig cfg = getConfigAs(JeeLinkSensorConfig.class);
        id = cfg.sensorId;

        statusUpdateJob = createStatusUpdateJob(scheduler, cfg.sensorTimeout);

        publisher = createPublisher();

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public synchronized void dispose() {
        id = null;

        JeeLinkHandler jlh = registeredBridge;
        registeredBridge = null;
        if (jlh != null) {
            jlh.removeReadingHandler(this);
        }

        if (statusUpdateJob != null) {
            statusUpdateJob.cancel(true);
            statusUpdateJob = null;
        }

        if (publisher != null) {
            publisher.dispose();
            publisher = null;
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
