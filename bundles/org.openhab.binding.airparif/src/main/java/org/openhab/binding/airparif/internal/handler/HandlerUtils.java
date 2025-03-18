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
package org.openhab.binding.airparif.internal.handler;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BridgeHandler;
import org.slf4j.Logger;

/**
 * The {@link HandlerUtils} defines and implements some common methods for Thing Handlers
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public interface HandlerUtils {
    default @Nullable ScheduledFuture<?> cancelFuture(@Nullable ScheduledFuture<?> job) {
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    default @Nullable <T extends BridgeHandler> T getBridgeHandler(Class<T> clazz) {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler != null) {
                if (bridgeHandler.getClass() == clazz) {
                    return (T) bridgeHandler;
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/incorrect-bridge");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/incorrect-bridge");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "");
        }
        return null;
    }

    default void schedule(String jobName, Runnable job, Duration duration) {
        ScheduledFuture<?> result = getJobs().remove(jobName);

        getLogger().debug("{} {} in {}", result != null ? "Rescheduled" : "Scheduling", jobName, duration);
        if (result != null) {
            cancelFuture(result);
        }

        getJobs().put(jobName, getScheduler().schedule(job, duration.getSeconds(), TimeUnit.SECONDS));
    }

    default void cleanJobs() {
        getJobs().values().forEach(job -> cancelFuture(job));
        getJobs().clear();
    }

    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);

    @Nullable
    Bridge getBridge();

    ScheduledExecutorService getScheduler();

    Logger getLogger();

    Map<String, ScheduledFuture<?>> getJobs();
}
