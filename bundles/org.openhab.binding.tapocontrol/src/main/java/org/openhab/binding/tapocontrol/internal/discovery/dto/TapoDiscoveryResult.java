/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.discovery.dto;

import static org.openhab.binding.tapocontrol.internal.api.protocol.TapoProtocolEnum.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoEncoder.isBase64Encoded;

import java.util.Base64;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.helpers.utils.TapoUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * TapoDiscoveryResult Data Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public record TapoDiscoveryResult(@Expose @SerializedName("factory_default") boolean factoryDefault,
        @Expose @SerializedName("is_support_iot_cloud") boolean isSupportIOT,
        @Expose @SerializedName("mgt_encrypt_schm") EncryptionShema encryptionShema, @Expose int role,
        @Expose int status, @Expose String alias, @Expose String appServerUrl, @Expose String deviceHwVer,
        @Expose @SerializedName(value = "deviceID", alternate = "device_id") String deviceId,
        @Expose @SerializedName(value = "deviceMac", alternate = "mac") String deviceMac,
        @Expose @SerializedName(value = "deviceModel", alternate = "device_model") String deviceModel,
        @Expose String deviceName, @Expose String deviceRegion,
        @Expose @SerializedName(value = "deviceType", alternate = "device_type") String deviceType, @Expose String fwId,
        @Expose String fwVer, @Expose String hwId, @Expose String ip, @Expose String isSameRegion,
        @Expose String oemId) {

    public record EncryptionShema(@Expose @SerializedName("is_support_https") boolean isSupportHttps,
            @Expose @SerializedName("encrypt_type") String encryptType,
            @Expose @SerializedName("http_port") int httpPort, @Expose int lv2) {
    }

    /* init new emty record */

    public TapoDiscoveryResult() {
        this(false, false, new EncryptionShema(false, SECUREPASSTROUGH.toString(), 80, 0), 0, 0, "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "");
    }

    /**********************************************
     * Return default data if recordobject is null
     **********************************************/

    @Override
    public boolean factoryDefault() {
        return Objects.requireNonNullElse(factoryDefault, false);
    }

    @Override
    public boolean isSupportIOT() {
        return Objects.requireNonNullElse(isSupportIOT, false);
    }

    @Override
    public int role() {
        return Objects.requireNonNullElse(role, 0);
    }

    @Override
    public int status() {
        return Objects.requireNonNullElse(status, 0);
    }

    @Override
    public String alias() {
        String encodedAlias = Objects.requireNonNullElse(alias, "");

        if (isBase64Encoded(encodedAlias)) {
            return new String(Base64.getDecoder().decode(encodedAlias));
        } else {
            return alias;
        }
    }

    @Override
    public String appServerUrl() {
        return Objects.requireNonNullElse(appServerUrl, "");
    }

    @Override
    public String deviceHwVer() {
        return Objects.requireNonNullElse(deviceHwVer, "");
    }

    @Override
    public String deviceId() {
        return Objects.requireNonNullElse(deviceId, "");
    }

    @Override
    public String deviceMac() {
        String mac = Objects.requireNonNullElse(deviceMac, "");
        return TapoUtils.unformatMac(mac).toUpperCase();
    }

    @Override
    public String deviceModel() {
        return Objects.requireNonNullElse(deviceModel, "");
    }

    @Override
    public String deviceName() {
        return Objects.requireNonNullElse(deviceName, "");
    }

    @Override
    public String deviceRegion() {
        return Objects.requireNonNullElse(deviceRegion, "");
    }

    @Override
    public String deviceType() {
        return Objects.requireNonNullElse(deviceType, "");
    }

    @Override
    public EncryptionShema encryptionShema() {
        return Objects.requireNonNullElse(encryptionShema,
                new EncryptionShema(false, SECUREPASSTROUGH.toString(), 80, 0));
    }

    @Override
    public String fwId() {
        return Objects.requireNonNullElse(fwId, "");
    }

    @Override
    public String fwVer() {
        return Objects.requireNonNullElse(fwVer, "");
    }

    @Override
    public String hwId() {
        return Objects.requireNonNullElse(hwId, "");
    }

    @Override
    public String ip() {
        return Objects.requireNonNullElse(ip, "");
    }

    @Override
    public String isSameRegion() {
        return Objects.requireNonNullElse(isSameRegion, "");
    }

    @Override
    public String oemId() {
        return Objects.requireNonNullElse(oemId, "");
    }
}
