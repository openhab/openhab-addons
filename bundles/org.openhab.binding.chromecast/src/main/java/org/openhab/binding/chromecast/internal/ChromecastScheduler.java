/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.chromecast.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles scheduling of connect and refresh events.
 *
 * @author Jason Holmes - Initial contribution
 * @author Wouter Born - Make sure only at most one refresh job is scheduled and running
 */
@NonNullByDefault
public class ChromecastScheduler {
    private final Logger logger = LoggerFactory.getLogger(ChromecastScheduler.class);

    private final ScheduledExecutorService scheduler;
    private final long connectDelay;
    private final long refreshRate;
    private final Runnable connectRunnable;
    private final Runnable refreshRunnable;

    private @Nullable ScheduledFuture<?> connectFuture;
    private @Nullable ScheduledFuture<?> refreshFuture;

    public ChromecastScheduler(ScheduledExecutorService scheduler, long connectDelay, Runnable connectRunnable,
            long refreshRate, Runnable refreshRunnable) {
        this.scheduler = scheduler;
        this.connectDelay = connectDelay;
        this.connectRunnable = connectRunnable;
        this.refreshRate = refreshRate;
        this.refreshRunnable = refreshRunnable;
    }

    public synchronized void destroy() {
        cancelConnect();
        cancelRefresh();
    }

    public synchronized void scheduleConnect() {
        cancelConnect();
        logger.debug("Scheduling connection");
        connectFuture = scheduler.schedule(connectRunnable, connectDelay, TimeUnit.SECONDS);
    }

    private synchronized void cancelConnect() {
        logger.debug("Canceling connection");
        ScheduledFuture<?> localConnectFuture = connectFuture;
        if (localConnectFuture != null) {
            localConnectFuture.cancel(true);
            connectFuture = null;
        }
    }

    public synchronized void scheduleRefresh() {
        cancelRefresh();
        logger.debug("Scheduling refresh in {} seconds", refreshRate);
        // With an initial delay of 1 second the refresh job can be restarted when several channels are refreshed at
        // once e.g. due to channel linking
        refreshFuture = scheduler.scheduleWithFixedDelay(refreshRunnable, 1, refreshRate, TimeUnit.SECONDS);
    }

    public synchronized void cancelRefresh() {
        logger.debug("Canceling refresh");
        ScheduledFuture<?> localRefreshFuture = refreshFuture;
        if (localRefreshFuture != null) {
            localRefreshFuture.cancel(true);
            refreshFuture = null;
        }
    }
}
