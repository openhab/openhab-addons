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
package org.openhab.binding.vesync.internal.dto.responses.devices.v1.airpurifier;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FilterLife} class is used as a DTO to hold the VeSync V1
 * API response data with regard to an air purifier's filter life.
 *
 * @author David Goodyear - Initial contribution
 */
public class FilterLife {
    @SerializedName("percent")
    public int percent;

    public int getPercent() {
        return percent;
    }
}
