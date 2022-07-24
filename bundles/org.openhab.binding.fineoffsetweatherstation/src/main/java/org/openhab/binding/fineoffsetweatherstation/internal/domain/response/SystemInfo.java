/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal.domain.response;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Information about the gateway
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class SystemInfo {
    /**
     * in MHz
     */
    private final @Nullable Integer frequency;
    private final LocalDateTime dateTime;
    /**
     * Daylight saving time
     */
    private final boolean dst;
    private final boolean useWh24;

    public SystemInfo(@Nullable Integer frequency, LocalDateTime dateTime, boolean dst, boolean useWh24) {
        this.frequency = frequency;
        this.dateTime = dateTime;
        this.dst = dst;
        this.useWh24 = useWh24;
    }

    public @Nullable Integer getFrequency() {
        return frequency;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public boolean isDst() {
        return dst;
    }

    public boolean isUseWh24() {
        return useWh24;
    }

    @Override
    public String toString() {
        return "SystemInfo{" + "frequency=" + frequency + " MHz" + ", dateTime=" + dateTime + ", dst=" + dst
                + ", useWh24=" + useWh24 + '}';
    }
}
