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
package org.openhab.binding.liquidcheck.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.liquidcheck.internal.json.Response;

@NonNullByDefault
public class LiquidCheckProperties {
    public final String firmware;
    public final String hardware;
    public final String name;
    public final String manufacturer;
    public final String uuid;
    public final String code;
    public final String ip;
    public final String mac;
    public final String ssid;

    public LiquidCheckProperties(Response response) {
        firmware = response.payload.device.firmware;
        hardware = response.payload.device.hardware;
        name = response.payload.device.name;
        manufacturer = response.payload.device.manufacturer;
        uuid = response.payload.device.uuid;
        code = response.payload.device.security.code;
        ip = response.payload.wifi.station.ip;
        mac = response.payload.wifi.station.mac;
        ssid = response.payload.wifi.accessPoint.ssid;
    }
}
