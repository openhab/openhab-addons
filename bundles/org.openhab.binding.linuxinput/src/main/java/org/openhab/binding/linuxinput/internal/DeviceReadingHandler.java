/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linuxinput.internal;

import java.io.IOException;
import java.util.concurrent.CancellationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler, that encapsulates the lifecycle of an underlying device.
 *
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
public abstract class DeviceReadingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DeviceReadingHandler.class);

    private @Nullable Thread worker = null;

    public DeviceReadingHandler(Thing thing) {
        super(thing);
    }

    abstract boolean immediateSetup() throws IOException;

    abstract boolean delayedSetup() throws IOException;

    abstract void handleEventsInThread() throws IOException;

    abstract void closeDevice() throws IOException;

    abstract String getInstanceName();

    @Override
    public final void initialize() {
        boolean performDelayedSetup = performImmediateSetup();
        if (performDelayedSetup) {
            scheduler.execute(() -> {
                boolean handleEvents = performDelayedSetup();
                if (handleEvents) {
                    Thread thread = Utils.backgroundThread(() -> {
                        try {
                            handleEventsInThread();
                        } catch (IOException e) {
                            logger.warn("Could not read event", e);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    }, "events", thing);
                    thread.start();
                    worker = thread;
                }
            });
        }
    }

    private boolean performImmediateSetup() {
        try {
            return immediateSetup();
        } catch (IOException e) {
            handleSetupError(e);
            return false;
        }
    }

    private boolean performDelayedSetup() {
        try {
            return delayedSetup();
        } catch (IOException e) {
            handleSetupError(e);
            return false;
        }
    }

    private void handleSetupError(Exception e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
    }

    @Override
    public final void dispose() {
        try {
            stopWorker();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                closeDevice();
            } catch (IOException e) {
                logger.debug("Could not close device", e);
            }
            logger.trace("disposed");
        }
    }

    private void stopWorker() throws InterruptedException {
        @Nullable
        Thread activeWorker = this.worker;
        logger.debug("interrupting worker {}", activeWorker);
        worker = null;

        if (activeWorker == null) {
            return;
        }
        activeWorker.interrupt();
        try {
            activeWorker.join(100);
        } catch (CancellationException e) {
            /* expected */
        }
        logger.debug("worker interrupted");
        if (activeWorker.isAlive()) {
            logger.warn("Worker not stopped");
        }
    }
}
