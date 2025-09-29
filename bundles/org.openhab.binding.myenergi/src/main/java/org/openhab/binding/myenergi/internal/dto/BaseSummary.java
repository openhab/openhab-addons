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
package org.openhab.binding.myenergi.internal.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.myenergi.internal.MyenergiBindingConstants;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BaseSummary} is a DTO class used to represent an abstract MyEnergi device.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
public class BaseSummary {

    @SerializedName("sno")
    public Long serialNumber = 0L;

    public String dat; // raw date in DD-MM-YYYY format
    public String tim; // raw time in HH:MM:SS format
    public Integer dst; // daylight saving time active

    @SerializedName("fwv")
    public String firmwareVersion;

    public BaseSummary() {
        super();
    }

    public BaseSummary(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public ZonedDateTime getLastUpdateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(dat + " " + tim, formatter);
        return ZonedDateTime.of(ldt, ZoneId.systemDefault());
    }

    public Map<@NonNull String, String> getThingProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(MyenergiBindingConstants.PROP_SERIAL_NUMBER, String.valueOf(serialNumber));
        properties.put(MyenergiBindingConstants.PROP_FIRMWARE_VERSION, firmwareVersion);
        return properties;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + ((serialNumber == null) ? 0 : serialNumber.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseSummary other = (BaseSummary) obj;
        if (serialNumber == null) {
            if (other.serialNumber != null) {
                return false;
            }
        } else if (!serialNumber.equals(other.serialNumber)) {
            return false;
        }
        return true;
    }
}
