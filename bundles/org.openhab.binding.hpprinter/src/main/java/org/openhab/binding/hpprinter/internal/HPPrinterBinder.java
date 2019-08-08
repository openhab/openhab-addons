/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hpprinter.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.hpprinter.internal.api.HPServerResult;
import org.openhab.binding.hpprinter.internal.api.HPStatus;
import org.openhab.binding.hpprinter.internal.api.HPUsage;
import org.openhab.binding.hpprinter.internal.api.HPWebServerClient;
import org.openhab.binding.hpprinter.internal.api.HPServerResult.requestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.hpprinter.internal.HPPrinterBindingConstants.*;

/**
 * The {@link HPPrinterBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class HPPrinterBinder {

    private final Logger logger = LoggerFactory.getLogger(HPPrinterBinder.class);

    private HPWebServerClient printerClient;
    private ScheduledExecutorService scheduler;
    //private HttpClient httpClient;

    private int statusCheckInterval = 4;
    private int usageCheckInterval = 0;
    private int offlineCheckInterval = 15;

    private HPPrinterBinderEvent handler;
    @Nullable
    private ScheduledFuture<?> statusScheduler = null;
    @Nullable
    private ScheduledFuture<?> usageScheduler = null;
    @Nullable
    private ScheduledFuture<?> offlineScheduler = null;

    public HPPrinterBinder(HPPrinterBinderEvent handler, @Nullable HttpClient httpClient, ScheduledExecutorService scheduler, @Nullable HPPrinterConfiguration config) {
        this.handler = handler;
        this.scheduler = scheduler;
        usageCheckInterval = config.refreshInterval;
        printerClient = new HPWebServerClient(httpClient, config.ipAddress, config.useSSL);
    }

    public void start() {
        goneOffline();
    }

    private void stopBackgroundSchedules() {
        if (usageScheduler != null) {
            usageScheduler.cancel(true);
            usageScheduler = null;
        }

        if (statusScheduler != null) {
            statusScheduler.cancel(true);
            statusScheduler = null;
        }
    }

    private void startBackgroundSchedules() {
        handler.binderStatus(true);

        if (usageScheduler != null) {
            usageScheduler.cancel(true);
            usageScheduler = null;
        }
        usageScheduler = scheduler.scheduleWithFixedDelay(() -> {
            checkUsage();
        }, 0, usageCheckInterval, TimeUnit.SECONDS);

        if (statusScheduler != null) {
            statusScheduler.cancel(true);
            statusScheduler = null;
        }
        statusScheduler = scheduler.scheduleWithFixedDelay(() -> {
            checkStatus();
        }, 0, statusCheckInterval, TimeUnit.SECONDS);
    }

    private void checkStatus() {
        HPServerResult<HPStatus> result = printerClient.getStatus();

        if (result.getStatus() == requestStatus.SUCCESS) {
            handler.binderState(CGROUP_STATUS, CHANNEL_STATUS,
                        new StringType(result.getData().getPrinterStatus()));

        } else {
            goneOffline();
        }
    }

    private void checkUsage() {
        HPServerResult<HPUsage> result = printerClient.getUsage();

        if (result.getStatus() == requestStatus.SUCCESS) {
            //Inks
            handler.binderState(CGROUP_INK, CHANNEL_BLACK_LEVEL, new DecimalType(result.getData().getInkBlack()));
            handler.binderState(CGROUP_INK, CHANNEL_COLOUR_LEVEL, new DecimalType(result.getData().getInkColor()));
            handler.binderState(CGROUP_INK, CHANNEL_CYAN_LEVEL, new DecimalType(result.getData().getInkCyan()));
            handler.binderState(CGROUP_INK, CHANNEL_MAGENTA_LEVEL, new DecimalType(result.getData().getInkMagenta()));
            handler.binderState(CGROUP_INK, CHANNEL_YELLOW_LEVEL, new DecimalType(result.getData().getInkYellow()));

            handler.binderState(CGROUP_USAGE, CHANNEL_JAM_EVENTS, new DecimalType(result.getData().getJamEvents()));
            handler.binderState(CGROUP_USAGE, CHANNEL_SUBSCRIPTION, new DecimalType(result.getData().getTotalSubscriptionImpressions()));
            handler.binderState(CGROUP_USAGE, CHANNEL_TOTAL_COLOURPAGES, new DecimalType(result.getData().getTotalColorImpressions()));
            handler.binderState(CGROUP_USAGE, CHANNEL_TOTAL_MONOPAGES, new DecimalType(result.getData().getTotalMonochromeImpressions()));
            handler.binderState(CGROUP_USAGE, CHANNEL_TOTAL_PAGES, new DecimalType(result.getData().getTotalImpressions()));
        } else {
            goneOffline();
        }
    }

    private void goneOnline() {
        handler.binderStatus(true);

        if (offlineScheduler != null) {
            offlineScheduler.cancel(true);
            offlineScheduler = null;
        }

        startBackgroundSchedules();
    }

    public void goneOffline() {
        handler.binderStatus(false);

        stopBackgroundSchedules();

        if (offlineScheduler != null) {
            offlineScheduler.cancel(true);
            offlineScheduler = null;
        }
        offlineScheduler = scheduler.scheduleWithFixedDelay(() -> {
            checkOnline();
        }, 0, offlineCheckInterval, TimeUnit.SECONDS);

    }

    private void checkOnline() {
        HPServerResult<HPStatus> result = printerClient.getStatus();

        if (result.getStatus() == requestStatus.SUCCESS) {
            goneOnline();
        }
    }

    public void close() {
        stopBackgroundSchedules();
        
        if (offlineScheduler != null) {
            offlineScheduler.cancel(true);
            offlineScheduler = null;
        }
    }

}