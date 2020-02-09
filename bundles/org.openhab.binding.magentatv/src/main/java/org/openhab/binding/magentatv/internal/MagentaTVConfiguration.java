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
package org.openhab.binding.magentatv.internal;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MagentaTVConfiguration} contains the thing config (updated at
 * runtime).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class MagentaTVConfiguration {
    private Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Initialize config
     *
     * @param config set of properties (thing config)
     */
    public void initializeConfig(Map<String, Object> newConfig) {
        updateConfig(newConfig);
        setDefault(PROPERTY_PORT, MR400_DEF_REMOTE_PORT);
    } // initialzeConfig()

    /**
     * Update config
     *
     * @param config set of properties (thing config)
     */
    public synchronized void updateConfig(Map<String, Object> newConfig) {
        synchronized (properties) {
            properties.putAll(newConfig);
        }
    }

    /**
     * return config property map
     *
     * @return property map of current thing config
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getModel() {
        return getValue(PROPERTY_MODEL_ID, MODEL_MR400).toUpperCase();
    }

    public boolean isMR400() {
        return getModel().equals(MODEL_MR400);
    }

    public void setModel(String model) {
        setValue(PROPERTY_MODEL_ID, model);
    }

    public void setHardwareVersion(String version) {
        setValue(PROPERTY_HARDWARE_VERSION, version);
    }

    public String getFirmwareVersion() {
        return getValue(PROPERTY_FIRMWARE_VERSION, "");
    }

    public void setFirmwareVersion(String version) {
        setValue(PROPERTY_FIRMWARE_VERSION, version);
    }

    public String getUDN() {
        return getValue(PROPERTY_UDN, "");
    }

    public void setUDN(String udn) {
        setValue(PROPERTY_UDN, udn);
    }

    public String getIpAddress() {
        return getValue(PROPERTY_IP, "");
    }

    public String getPort() {
        String model = getModel();
        String port = getValue(PROPERTY_PORT, "");
        if (model.contains(MODEL_MR400) && port.equals("49153")) {
            setPort(MR400_DEF_REMOTE_PORT);
        }
        return getValue(PROPERTY_PORT, MR400_DEF_REMOTE_PORT);
    }

    public void setPort(String port) {
        setValue(PROPERTY_PORT, port);
    }

    public String getMacAddress() {
        return getValue(PROPERTY_MAC_ADDRESS, "");
    }

    public void setMacAddress(String macAddress) {
        setValue(PROPERTY_MAC_ADDRESS, macAddress);
    }

    public String getWakeOnLAN() {
        return getValue(PROPERTY_WAKEONLAN, "");
    }

    public void setWakeOnLAN(String wol) {
        setValue(PROPERTY_WAKEONLAN, wol);
    }

    public String getDescriptionUrl() {
        // TO-DO: that's a hack!!
        String url = getValue(PROPERTY_DESC_URL, MR400_DEF_DESCRIPTION_URL);
        String port = getPort();
        if (url.equals(MR400_DEF_DESCRIPTION_URL) && !(port.equals(MR400_DEF_REMOTE_PORT) || port.equals("49153"))) {
            setValue(PROPERTY_DESC_URL, MR401B_DEF_DESCRIPTION_URL);
        }
        return getValue(PROPERTY_DESC_URL, MR400_DEF_DESCRIPTION_URL);
    }

    public void setDescriptionUrl(String url) {
        setValue(PROPERTY_DESC_URL, url);
    }

    public String getFriendlyName() {
        return getValue(PROPERTY_FRIENDLYNAME, "");
    }

    public void setFriendlyName(String friendlyName) {
        setValue(PROPERTY_FRIENDLYNAME, friendlyName);
    }

    public String getTerminalID() {
        return getValue(PROPERTY_TERMINALID, "");
    }

    public void setTerminalID(String terminalID) {
        setValue(PROPERTY_TERMINALID, terminalID);
    }

    public String getAccountName() {
        return getValue(PROPERTY_ACCT_NAME, "");
    }

    public void setAccountName(String accountName) {
        setValue(PROPERTY_ACCT_NAME, accountName);
    }

    public String getAccountPassword() {
        return getValue(PROPERTY_ACCT_PWD, "");
    }

    public void setAccountPassword(String accountPassword) {
        setValue(PROPERTY_ACCT_PWD, accountPassword);
    }

    public String getUserID() {
        return getValue(PROPERTY_USERID, "");
    }

    public void setUserID(String userID) {
        setValue(PROPERTY_USERID, userID);
    }

    public String getPairingCode() {
        return getValue(PROPERTY_PAIRINGCODE, "");
    }

    public void setPairingCode(String pairingCode) {
        setValue(PROPERTY_PAIRINGCODE, pairingCode);
    }

    public String getVerificationCode() {
        return getValue(PROPERTY_VERIFICATIONCODE, "");
    }

    public void setVerificationCode(String verificationCode) {
        setValue(PROPERTY_VERIFICATIONCODE, verificationCode);
    }

    public String getLocalIP() {
        return getValue(PROPERTY_LOCAL_IP, "");
    }

    public void setLocalIP(String localIP) {
        setValue(PROPERTY_LOCAL_IP, localIP);
    }

    public String getLocalMAC() {
        return getValue(PROPERTY_LOCAL_MAC, "");
    }

    public void setLocalMAC(String localMAC) {
        setValue(PROPERTY_LOCAL_MAC, localMAC);
    }

    public String getEpgUrl() {
        return getValue(PROPERTY_EPGHTTPSURL, "");
    }

    public void setEgpUrl(String egphttpsurl) {
        setValue(PROPERTY_EPGHTTPSURL, egphttpsurl);
    }

    @SuppressWarnings({ "null", "unused" })
    private void setDefault(String key, String value) {
        synchronized (properties) {
            if ((properties.get(key) != null) && properties.get(key).toString().isEmpty()) {
                properties.remove(key);
            }
            if (properties.get(key) == null) {
                properties.put(key, value);
            }
        }
    }

    @SuppressWarnings("null")
    private String getValue(String key, String defValue) {
        return properties.get(key) == null ? defValue : properties.get(key).toString();
    }

    @SuppressWarnings("null")
    private synchronized void setValue(String key, String value) {
        if (properties.get(key) != null) {
            properties.remove(key);
        }
        properties.put(key, value);
    }
}
