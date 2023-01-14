/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.draytonwiser.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class HotWaterDTO {

    @SerializedName("id")
    private Integer id;
    private String overrideType;
    private String mode;
    private String waterHeatingState;
    private String hotWaterRelayState;
    private Integer overrideTimeoutUnixTime;

    public Integer getId() {
        return id;
    }

    public String getOverrideType() {
        return overrideType;
    }

    public String getMode() {
        return mode;
    }

    public String getWaterHeatingState() {
        return waterHeatingState;
    }

    public String getHotWaterRelayState() {
        return hotWaterRelayState;
    }

    public Integer getOverrideTimeoutUnixTime() {
        return overrideTimeoutUnixTime;
    }
}
