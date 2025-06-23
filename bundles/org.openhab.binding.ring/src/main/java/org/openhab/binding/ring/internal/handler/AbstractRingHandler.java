/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractRingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public abstract class AbstractRingHandler extends BaseThingHandler {

    // Current status
    protected OnOffType status = OnOffType.OFF;
    protected OnOffType enabled = OnOffType.ON;
    protected final Logger logger = LoggerFactory.getLogger(AbstractRingHandler.class);

    // Scheduler
    protected @Nullable ScheduledFuture<?> refreshJob;

    protected AbstractRingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing AbstractRingHandler");
    }

    /**
     * Refresh the state of channels that may have changed by (re-)initialization.
     */
    protected abstract void refreshState();

    /**
     * Called every minute
     */
    protected abstract void minuteTick();

    private void refresh() {
        try {
            minuteTick();
        } catch (final Exception e) {
            logger.debug("AbstractHandler - Exception occurred during execution of startAutomaticRefresh(): {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Check every 60 seconds if one of the alarm times is reached.
     */
    protected void startAutomaticRefresh(final int refreshInterval) {
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, refreshInterval, TimeUnit.SECONDS);
        refreshState();
    }

    protected void stopAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    /**
     * Dispose off the refreshJob nicely.
     */
    @Override
    public void dispose() {
        stopAutomaticRefresh();
    }
}
