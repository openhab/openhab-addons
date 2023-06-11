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
package org.openhab.binding.magentatv.internal.config;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MagentaTVDynamicConfig} extends MagentaTVThingConfiguration contains additional dynamic data
 * runtime).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVDynamicConfig extends MagentaTVThingConfiguration {
    protected String modelId = MODEL_MR400; // MR model
    protected String hardwareVersion = "";
    protected String firmwareVersion = "";
    protected String friendlyName = ""; // Receiver's Friendly Name from UPnP descriptin
    protected String descriptionUrl = MR401B_DEF_DESCRIPTION_URL; // Device description, usually from UPnP discovery
    protected String localIP = ""; // Outbound IP for pairing/communication
    protected String localMAC = ""; // used to compute the terminalID
    protected String wakeOnLAN = ""; // Device supports Wake-on-LAN
    protected String terminalID = ""; // terminalID for pairing process
    protected String pairingCode = ""; // Input to the paring process
    protected String verificationCode = ""; // Result of the paring process

    public MagentaTVDynamicConfig() {
    }

    public MagentaTVDynamicConfig(MagentaTVThingConfiguration config) {
        super.update(config);
    }

    public void updateNetwork(MagentaTVDynamicConfig network) {
        this.setLocalIP(network.getLocalIP());
        this.setLocalMAC(network.getLocalMAC());
        this.setTerminalID(network.getTerminalID());
    }

    public String getModel() {
        return modelId.toUpperCase();
    }

    public String getPort() {
        return !port.isEmpty() ? port : isMR400() ? MR400_DEF_REMOTE_PORT : MR401B_DEF_REMOTE_PORT;
    }

    public void setPort(String port) {
        if (modelId.contains(MODEL_MR400) && port.equals("49153")) {
            // overwrite port returned by discovery (invalid for this model)
            this.port = MR400_DEF_REMOTE_PORT;
        } else {
            this.port = port;
        }
    }

    public boolean isMR400() {
        return modelId.equals(MODEL_MR400);
    }

    public void setModel(String modelId) {
        this.modelId = modelId;
    }

    public String getWakeOnLAN() {
        return wakeOnLAN;
    }

    public void setWakeOnLAN(String wakeOnLAN) {
        this.wakeOnLAN = wakeOnLAN.toUpperCase();
    }

    public String getDescriptionUrl() {
        if (descriptionUrl.equals(MR400_DEF_DESCRIPTION_URL)
                && !(port.equals(MR400_DEF_REMOTE_PORT) || port.equals("49153"))) {
            // MR401B returns the wrong URL
            return MR401B_DEF_DESCRIPTION_URL;
        }
        return descriptionUrl;
    }

    public void setDescriptionUrl(String descriptionUrl) {
        this.descriptionUrl = getString(descriptionUrl);
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID.toUpperCase();
    }

    public String getPairingCode() {
        return pairingCode;
    }

    public void setPairingCode(String pairingCode) {
        this.pairingCode = pairingCode;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public String getLocalMAC() {
        return localMAC;
    }

    public void setLocalMAC(String localMAC) {
        this.localMAC = localMAC.toUpperCase();
    }
}
