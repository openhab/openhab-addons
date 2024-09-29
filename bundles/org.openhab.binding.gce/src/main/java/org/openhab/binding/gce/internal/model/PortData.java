/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PortData} is responsible for holding data regarding current status of a port.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PortData {
    private double value = -1;
    private ZonedDateTime timestamp = ZonedDateTime.now();
    private Optional<ScheduledFuture<?>> pulsing = Optional.empty();

    public void cancelPulsing() {
        pulsing.ifPresent(pulse -> pulse.cancel(true));
        pulsing = Optional.empty();
    }

    public void dispose() {
        cancelPulsing();
    }

    public void setData(double value, ZonedDateTime timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setPulsing(ScheduledFuture<?> pulsing) {
        this.pulsing = Optional.of(pulsing);
    }

    public boolean isInitializing() {
        return value == -1;
    }
}
