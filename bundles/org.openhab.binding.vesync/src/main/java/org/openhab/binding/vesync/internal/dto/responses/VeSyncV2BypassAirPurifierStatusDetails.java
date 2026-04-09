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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncV2BypassAirPurifierStatusDetails} is a Java class used as a DTO to hold the Vesync's API's common
 * response
 * data, with regard's to an air purifier device's current status.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncV2BypassAirPurifierStatusDetails {
    @SerializedName("enabled")
    public boolean enabled;

    @SerializedName("filter_life")
    public int filterLife;

    @SerializedName("mode")
    public String mode;

    @SerializedName("level")
    public int level;

    @SerializedName("air_quality")
    public int airQuality;

    @SerializedName("air_quality_value")
    public int airQualityValue;

    @SerializedName("display")
    public boolean display;

    @SerializedName("child_lock")
    public boolean childLock;

    @SerializedName("night_light")
    public String nightLight;

    @SerializedName("configuration")
    public VeSyncV2BypassAirPurifierStatusConfig configuration;

    @SerializedName("extension")
    public VeSyncV2BypassAirPurifierStatusExtension extension;

    @SerializedName("device_error_code")
    public int deviceErrorCode;
}
