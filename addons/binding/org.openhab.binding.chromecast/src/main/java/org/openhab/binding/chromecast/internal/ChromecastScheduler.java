/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Handles scheduling of connect and refresh events.
 *
 * @author Jason Holmes - Initial Author.
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

    public ChromecastScheduler(ScheduledExecutorService scheduler, Integer connectDelay, Runnable connectRunnable, Integer refreshRate, Runnable refreshRunnable) {
        this.scheduler = scheduler;
        this.connectDelay = connectDelay;
        this.connectRunnable = connectRunnable;
        this.refreshRate = refreshRate;
        this.refreshRunnable = refreshRunnable;
    }

    public void destroy() {
        cancelConnect();
        cancelRefresh();
    }

    public void scheduleConnect() {
        logger.debug("Scheduling connection");
        cancelConnect();
        connectFuture = scheduler.schedule(connectRunnable, connectDelay, SECONDS);
    }

    private void cancelConnect() {
        logger.debug("Canceling connection");
        if (connectFuture != null) {
            connectFuture.cancel(true);
            connectFuture = null;
        }
    }

    public void scheduleRefresh() {
        cancelRefresh();
        logger.debug("Scheduling refresh in {} seconds", refreshRate);
        refreshFuture = scheduler.scheduleAtFixedRate(refreshRunnable, 0, refreshRate, SECONDS);
    }

    public void cancelRefresh() {
        logger.debug("Canceling refresh");
        if (refreshFuture != null) {
            refreshFuture.cancel(true);
        }
    }
}
