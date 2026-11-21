/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The {@link AbstractSnapshotDefinition}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSnapshotDefinition implements SnapshotDefinition {

    protected final Map<String, Object> otherInfo = new HashMap<>();
    private Map<String, Object> rawData = new HashMap<>();

    @JsonAnySetter
    public void addOtherInfo(String propertyKey, Object value) {
        this.otherInfo.put(propertyKey, value);
    }

    @Nullable
    public Object getOtherInfo(String propertyKey) {
        return this.otherInfo.get(propertyKey);
    }

    @JsonIgnore
    public Map<String, Object> getRawData() {
        return rawData;
    }

    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }

    /**
     * Formats an hour/minute pair as {@code HH:MM}.
     * Some devices report the whole duration in the minute field (e.g. hour 0, minute 65) instead of keeping
     * minutes within 0-59 as their capability declares, so the pair is normalized to total minutes first.
     */
    protected static String formatHourMinute(double hour, double minute) {
        long totalMinutes = Math.round(hour * 60 + minute);
        return String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60);
    }
}
