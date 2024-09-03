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
 */
@NonNullByDefault
public class MideaACConfiguration {

    private String ipAddress = "";

    /**
     * @param ipAddress of the device. Get/Set
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    private String ipPort = "";

    /**
     * @param ipPort of the device. Get/Set
     */
    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    private String deviceId = "";

    /**
     * @param deviceId of the device. Get/Set
     */
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private String email = "";

    /**
     * @param email for your cloud provider. Get/Set
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private int reauth;

    /**
     * @param reauth interval to get new key
     *            and token. Get/Set. 0 = never
     */
    public Integer getReauth() {
        return reauth;
    }

    public void setReauth(Integer reauth) {
        this.reauth = reauth;
    }

    private String password = "";

    /**
     * @param password for your cloud provider. Get/Set
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String cloud = "";

    /**
     * @param cloud your cloud provider Name from
     *            supported options. Required V3 devices. Get/Set.
     */
    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    private String token = "";

    /**
     * @param token Required V3 devices. Get/Set.
     *            Discovery possible with email and password
     */
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String key = "";

    /**
     * @param key Required V3 devices. Get/Set.
     *            Discovery possible with email and password
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private int pollingTime;

    /**
     * @param pollingTime Frequency in seconds Get/Set.
     *            Thirty seconds minimum
     */
    public int getPollingTime() {
        return pollingTime;
    }

    public void setPollingTime(int pollingTime) {
        this.pollingTime = pollingTime;
    }

    private int timeout;

    /**
     * @param timeout How long after message is sent will the
     *            socket wait Get/Set. Two to 10 seconds
     */
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private boolean promptTone;

    /**
     * @param promptTone Set the device to "ding" when
     *            command is received.
     */
    public boolean getPromptTone() {
        return promptTone;
    }

    public void setPromptTone(boolean promptTone) {
        this.promptTone = promptTone;
    }

    /**
     * Check during initialization that the params are valid
     */
    public boolean isValid() {
        return !("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort.isBlank() || ipAddress.isBlank());
    }

    /**
     * Check during initialization if discovery is needed
     */
    public boolean isDiscoveryNeeded() {
        return ("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort.isBlank() || ipAddress.isBlank()
                || !Utils.validateIP(ipAddress));
    }
}
