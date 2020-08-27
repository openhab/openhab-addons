/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.magentatv.internal.config;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MagentaTVThingConfiguration} contains the thing config (updated at
 * runtime).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVThingConfiguration {
    public String modelId = MODEL_MR400; // MR model
    public String ipAddress = ""; // IP Address of the MR
    public String port = MR400_DEF_REMOTE_PORT; // Port of the remote service
    public String udn = ""; // UPnP UDN
    public String macAddress = ""; // Usually gets filled by the thing discovery (or set by .things file)
    public String accountName = ""; // Credentials: Account Name from Telekom Kundencenter (used for OAuth)
    public String accountPassword = ""; // Credentials: Account Password from Telekom Kundencenter (used for OAuth)
    public String userId = ""; // userId required for pairing (can be configured manually or gets auto-filled by the
                               // binding on successful OAuth. Value is persisted so OAuth nedds only to be redone when
                               // credentials change.

    public void update(MagentaTVThingConfiguration newConfig) {
        modelId = newConfig.modelId;
        ipAddress = newConfig.ipAddress;
        port = newConfig.port;
        udn = newConfig.udn;
        macAddress = newConfig.macAddress;
        accountName = newConfig.accountName;
        accountPassword = newConfig.accountPassword;
        userId = newConfig.userId;
    }

    public String getModel() {
        return modelId.toUpperCase();
    }

    public boolean isMR400() {
        return modelId.equals(MODEL_MR400);
    }

    public void setModel(String modelId) {
        this.modelId = modelId;
    }

    /*
     * public void setHardwareVersion(String version) {
     *
     * setValue(PROPERTY_HARDWARE_VERSION, version);
     * }
     *
     * public String getFirmwareVersion() {
     * return getValue(PROPERTY_FIRMWARE_VERSION, "");
     * }
     *
     * public void setFirmwareVersion(String version) {
     * setValue(PROPERTY_FIRMWARE_VERSION, version);
     * }
     */
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        if (modelId.contains(MODEL_MR400) && port.equals("49153")) {
            // overwrite port returned by discovery (invalid for this model)
            this.port = MR400_DEF_REMOTE_PORT;
        } else {
            this.port = port;
        }
    }

    public String getUDN() {
        return udn.toUpperCase();
    }

    public void setUDN(String udn) {
        this.udn = udn;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress.toUpperCase();
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public String getUserID() {
        return userId;
    }

    public void setUserID(String userId) {
        this.userId = userId;
    }

    protected String getString(@Nullable Object value) {
        return value != null ? (String) value : "";
    }
}
