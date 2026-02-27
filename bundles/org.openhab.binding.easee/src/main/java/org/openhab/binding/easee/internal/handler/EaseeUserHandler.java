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
package org.openhab.binding.easee.internal.handler;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easee.internal.command.account.GetUserTotalConsumption;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EaseeUserHandler} represents a user with access to an Easee site.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class EaseeUserHandler extends EaseeBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EaseeUserHandler.class);

    /**
     * Schedule for polling consumption data
     */
    private final AtomicReference<@Nullable Future<?>> dataPollingJobReference;

    public EaseeUserHandler(Thing thing) {
        super(thing, true);
        this.dataPollingJobReference = new AtomicReference<>(null);
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize User");
        logger.debug("Easee User initialized with id: {}", getId());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_BRIDGE);
        startPolling();
    }

    /**
     * Start the polling.
     */
    @Override
    protected void startPolling() {
        updateJobReference(dataPollingJobReference, scheduler.scheduleWithFixedDelay(this::pollingRun,
                POLLING_INITIAL_DELAY, getBridgeConfiguration().getSessionDataPollingInterval(), TimeUnit.SECONDS));
    }

    /**
     * Stops the polling.
     */
    @Override
    protected void stopPolling() {
        cancelJobReference(dataPollingJobReference);
    }

    /**
     * Poll the Easee Cloud API one time for user consumption data.
     */
    void pollingRun() {
        String userId = getId();
        logger.debug("polling consumption data for user {}", userId);

        if (getThing().getStatus() == ThingStatus.ONLINE) {
            enqueueCommand(new GetUserTotalConsumption(this, userId, this::updateOnlineStatus));
        }
    }
}
