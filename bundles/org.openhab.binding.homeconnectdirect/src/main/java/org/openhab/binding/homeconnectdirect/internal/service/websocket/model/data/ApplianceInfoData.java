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
package org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Appliance info data model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record ApplianceInfoData(@SerializedName("deviceID") String deviceId, String eNumber, String brand, String vib,
        String mac, @SerializedName("hwVersion") String hardwareVersion,
        @SerializedName("swVersion") String softwareVersion, String haVersion, String deviceType, String deviceInfo,
        String serialNumber, String fdString, String shipSki) {
}
