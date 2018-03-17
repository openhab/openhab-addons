/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * RefreshGroup.
 *
 * @author Radovan Sninsky
 * @since 2018-02-12 22:42
 */
public class RefreshGroup {

    public static final String RG_LOW = "LOW";
    public static final String RG_MEDIUM = "MEDIUM";
    public static final String RG_HIGH = "HIGH";
    public static final String RG_REALTIME = "REALTIME";

    private final Logger logger = LoggerFactory.getLogger(RefreshGroup.class);

    private final String name;
    private PlcComSClient plcClient;
    private final List<RefreshableHandler> handlers = new ArrayList<>(100);
    private ScheduledFuture<?> job;

    public RefreshGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void start(ScheduledExecutorService scheduler, long interval, PlcComSClient client) throws IOException {
        logger.debug("Opening {} refresh group w interval: {} ...", this.name, interval);
        this.plcClient = client;
        this.plcClient.open();
        this.job = scheduler.scheduleWithFixedDelay(
                () -> new ArrayList<>(handlers).forEach(h -> {
                    try {
                        h.refreshFromPlc(plcClient);
                    } catch (Exception e) {
                        logger.error("Refreshing handler {} failed w error: {}", h, e.getMessage());
                    }
                }), 10, interval, TimeUnit.SECONDS);
    }

    public void dispose() {
        logger.debug("Canceling {} refresh job", name);
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        if (plcClient != null) {
            plcClient.close();
        }
    }

    public void addHandler(RefreshableHandler handler) {
        handlers.add(handler);
    }

    public void removeHandler(final RefreshableHandler handler) {
        handlers.stream().filter(h -> h.equals(handler)).findFirst().ifPresent(handlers::remove);
    }
}
