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
package org.openhab.binding.lgthinq.lgservices.model.devices.ac;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link ExtendedDeviceInfo} containing extended information obout the device. In
 * AC cases, it holds instant power consumption, filter used in hours and max time to use.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedDeviceInfo {
    private String instantPower = "";
    private String filterHoursUsed = "";
    private String filterHoursMax = "";

    @JsonProperty(CAP_EXTRA_ATTR_FILTER_USED_TIME)
    @JsonAlias("airState.filterMngStates.useTime")
    public String getFilterHoursUsed() {
        return filterHoursUsed;
    }

    public void setFilterHoursUsed(String filterHoursUsed) {
        this.filterHoursUsed = filterHoursUsed;
    }

    @JsonProperty(CAP_EXTRA_ATTR_FILTER_MAX_TIME_TO_USE)
    @JsonAlias("airState.filterMngStates.maxTime")
    public String getFilterHoursMax() {
        return filterHoursMax;
    }

    public void setFilterHoursMax(String filterHoursMax) {
        this.filterHoursMax = filterHoursMax;
    }

    /**
     * Returns the instant total power consumption
     *
     * @return the instant total power consumption
     */
    @JsonProperty(CAP_EXTRA_ATTR_INSTANT_POWER)
    @JsonAlias("airState.energy.totalCurrent")
    public String getRawInstantPower() {
        return instantPower;
    }

    public void setRawInstantPower(String instantPower) {
        this.instantPower = instantPower;
    }

    public Double getInstantPower() {
        try {
            return Double.parseDouble(instantPower);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
