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
package org.openhab.binding.gce.internal.model;

import java.time.Instant;
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
    private Instant timestamp = Instant.now();
    private @Nullable ScheduledFuture<?> pulsing;
    private @Nullable ScheduledFuture<?> pulseCanceler;

    public void cancelPulsing() {
        if (pulsing instanceof ScheduledFuture job) {
            job.cancel(true);
            pulsing = null;
        }
        cancelCanceler();
    }

    public void cancelCanceler() {
        if (pulseCanceler instanceof ScheduledFuture job) {
            job.cancel(true);
            pulseCanceler = null;
        }
    }

    public void dispose() {
        cancelPulsing();
        cancelCanceler();
    }

    public void setData(double value, Instant timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setPulsing(ScheduledFuture<?> pulsing) {
        cancelPulsing();
        this.pulsing = pulsing;
    }

    public boolean isInitialized() {
        return value != -1;
    }

    public void setPulseCanceler(ScheduledFuture<?> pulseCanceler) {
        this.pulseCanceler = pulseCanceler;
    }
}
