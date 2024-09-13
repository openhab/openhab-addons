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
package org.openhab.binding.mideaac.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MideaACConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - JavaDoc
 */
@NonNullByDefault
public class MideaACConfiguration {

    private String ipAddress = "";

    /**
     * Device IP Address
     * 
     * @return ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Device IP Address
     * 
     * @param ipAddress Device IP Address
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    private String ipPort = "";

    /**
     * Device IP Port
     * 
     * @return ipPort
     */
    public String getIpPort() {
        return ipPort;
    }

    /**
     * Set Device IP port
     * 
     * @param ipPort Device IP port
     */
    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    private String deviceId = "";

    /**
     * Device Device ID
     * 
     * @return deviceId
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets device ID
     * 
     * @param deviceId device id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private String email = "";

    /**
     * Your email for your cloud provider.
     * 
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email
     * 
     * @param email email for your cloud provider
     */
    public void setEmail(String email) {
        this.email = email;
    }

    private String password = "";

    /**
     * Password for your cloud provider.
     * 
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets Password for your cloud provider.
     * 
     * @param password Password for your cloud provider
     */
    public void setPassword(String password) {
        this.password = password;
    }

    private String cloud = "";

    /**
     * Your cloud provider Name from supported options. Required V3 devices.
     * 
     * @return cloud
     * 
     */
    public String getCloud() {
        return cloud;
    }

    /**
     * Sets Cloud Provider
     * 
     * @param cloud Cloud provider
     */
    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    private String token = "";

    /**
     * Required V3 devices. Get/Set.
     * Discovery possible with email and password
     * 
     * @return token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets token
     * 
     * @param token cloud provider token
     */
    public void setToken(String token) {
        this.token = token;
    }

    private String key = "";

    /**
     * Required for V3 devices.
     * Discovery possible with email and password
     * 
     * @return key
     * 
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets Could provider key
     * 
     * @param key Cloud provider key
     */
    public void setKey(String key) {
        this.key = key;
    }

    private int pollingTime;

    /**
     * Frequency in seconds Thirty seconds minimum
     * 
     * @return pollingTime
     * 
     */
    public int getPollingTime() {
        return pollingTime;
    }

    /**
     * Sets polling frequency 30 seconds minimum
     * 
     * @param pollingTime frequency of polling
     */
    public void setPollingTime(int pollingTime) {
        this.pollingTime = pollingTime;
    }

    private int timeout;

    /**
     * How long after message is sent will the socket wait Get/Set. Two to 10 seconds
     * 
     * @return timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets length of time socket is open for reading
     * 
     * @param timeout socket timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private boolean promptTone;

    /**
     * Status of device to "ding" when command is received.
     * 
     * @return promptTone
     * 
     */
    public boolean getPromptTone() {
        return promptTone;
    }

    /**
     * Set device to chime with Set command
     * 
     * @param promptTone indoor unit chime
     */
    public void setPromptTone(boolean promptTone) {
        this.promptTone = promptTone;
    }

    /**
     * Check during initialization that the params are valid
     * 
     * @return true(valid), false (not valid)
     */
    public boolean isValid() {
        return !("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort.isBlank() || ipAddress.isBlank());
    }

    /**
     * Check during initialization if discovery is needed
     * 
     * @return true(discovery needed), false (not needed)
     */
    public boolean isDiscoveryNeeded() {
        return ("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort.isBlank() || ipAddress.isBlank()
                || !Utils.validateIP(ipAddress));
    }
}
