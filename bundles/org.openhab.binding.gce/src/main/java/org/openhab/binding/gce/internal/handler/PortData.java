/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gce.internal.handler;

import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PortData} is responsible for holding data regarding current status of a port.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PortData {
    private double value = -1;
    private ZonedDateTime timestamp = ZonedDateTime.now();
    private @Nullable ScheduledFuture<?> pulsing;
    private @Nullable ScheduledFuture<?> pullJob;

    public void cancelPulsing() {
        if (pulsing != null) {
            pulsing.cancel(true);
        }
        pulsing = null;
    }

    @Override
    protected void finalize() {
        cancelPulsing();
        if (pullJob != null) {
            pullJob.cancel(true);
        }
        pullJob = null;
    }

    public void setData(double value, ZonedDateTime timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public @Nullable ScheduledFuture<?> getPulsing() {
        return pulsing;
    }

    public void setPulsing(ScheduledFuture<?> pulsing) {
        this.pulsing = pulsing;
    }

    public void setPullJob(ScheduledFuture<?> pullJob) {
        this.pullJob = pullJob;
    }

    public boolean isInitializing() {
        return value == -1;
    }
}
