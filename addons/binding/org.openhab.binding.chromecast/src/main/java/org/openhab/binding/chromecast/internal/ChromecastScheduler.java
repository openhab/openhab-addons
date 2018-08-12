/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.internal;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles scheduling of connect and refresh events.
 *
 * @author Jason Holmes - Initial contribution
 * @author Wouter Born - Make sure only at most one refresh job is scheduled and running
 */
public class ChromecastScheduler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ScheduledExecutorService scheduler;
    private final Integer connectDelay;
    private final Integer refreshRate;
    private final Runnable connectRunnable;
    private final Runnable refreshRunnable;

    private ScheduledFuture<?> connectFuture;
    private ScheduledFuture<?> refreshFuture;

    public ChromecastScheduler(ScheduledExecutorService scheduler, Integer connectDelay, Runnable connectRunnable,
            Integer refreshRate, Runnable refreshRunnable) {
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
        logger.debug("Scheduling connection");
        cancelConnect();
        connectFuture = scheduler.schedule(connectRunnable, connectDelay, SECONDS);
    }

    private synchronized void cancelConnect() {
        logger.debug("Canceling connection");
        if (connectFuture != null) {
            connectFuture.cancel(true);
            connectFuture = null;
        }
    }

    public synchronized void scheduleRefresh() {
        cancelRefresh();
        logger.debug("Scheduling refresh in {} seconds", refreshRate);
        // With an initial delay of 1 second the refresh job can be restarted when several channels
        // are refreshed at once e.g. due to channel linking
        refreshFuture = scheduler.scheduleWithFixedDelay(refreshRunnable, 1, refreshRate, SECONDS);
    }

    public synchronized void cancelRefresh() {
        logger.debug("Canceling refresh");
        if (refreshFuture != null) {
            refreshFuture.cancel(true);
        }
    }
}
