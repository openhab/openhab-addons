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
package org.openhab.binding.tapocontrol.internal.devices.bridge.dto;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * TapoCloud DeviceList Data Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public record TapoCloudDevice(@Expose String deviceType, @Expose int role, @Expose String fwVer,
        @Expose String appServerUrl, @Expose String deviceRegion, @Expose String deviceId, @Expose String deviceName,
        @Expose String deviceHwVer, @Expose String alias, @Expose String deviceMac, @Expose String oemId,
        String deviceModel, @Expose String hwId, @Expose String fwId, @Expose String isSameRegion, @Expose int status) {

    /* init new emty record */
    public TapoCloudDevice() {
        this("", 0, "", "", "", "", "", "", "", "", "", "", "", "", "", 0);
    }

    /**********************************************
     * Return default data if recordobject is null
     **********************************************/

    @Override
    public String deviceType() {
        return Objects.requireNonNullElse(deviceType, "");
    }

    @Override
    public int role() {
        return Objects.requireNonNullElse(role, 0);
    }

    @Override
    public String fwVer() {
        return Objects.requireNonNullElse(fwVer, "");
    }

    @Override
    public String appServerUrl() {
        return Objects.requireNonNullElse(appServerUrl, "");
    }

    @Override
    public String deviceRegion() {
        return Objects.requireNonNullElse(deviceRegion, "");
    }

    @Override
    public String deviceId() {
        return Objects.requireNonNullElse(deviceId, "");
    }

    @Override
    public String deviceName() {
        return Objects.requireNonNullElse(deviceName, "");
    }

    @Override
    public String deviceHwVer() {
        return Objects.requireNonNullElse(deviceHwVer, "");
    }

    @Override
    public String alias() {
        return Objects.requireNonNullElse(alias, "");
    }

    @Override
    public String deviceMac() {
        return Objects.requireNonNullElse(deviceMac, "");
    }

    @Override
    public String oemId() {
        return Objects.requireNonNullElse(oemId, "");
    }

    @Override
    public String deviceModel() {
        return Objects.requireNonNullElse(deviceModel, "");
    }

    @Override
    public String hwId() {
        return Objects.requireNonNullElse(hwId, "");
    }

    @Override
    public String fwId() {
        return Objects.requireNonNullElse(fwId, "");
    }

    @Override
    public String isSameRegion() {
        return Objects.requireNonNullElse(isSameRegion, "");
    }

    @Override
    public int status() {
        return Objects.requireNonNullElse(status, 0);
    }
}
