/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPIConfig;

/**
 * The Cloudrain account configuration class.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainConfig implements CloudrainAPIConfig {
    /**
     * The default timeout for connecting to the Cloudrain API (10 seconds)
     */
    public static final Integer DEFAULT_CONNECTION_TIMEOUT = 10;
    /**
     * The default interval for updating the irrigation status from the API (30 seconds)
     */
    public static final Integer DEFAULT_IRRIGATION_INTERVALL = 30;
    /**
     * The default interval for updating the zone status from the API (300 seconds)
     */
    public static final Integer DEFAULT_ZONE_INTERVALL = 300;
    /**
     * The default setting whether remaining seconds of active irrigations shall be updated in real time without API
     * access (default: true)
     */
    public static final Boolean DEFAULT_REALTIME = true;
    /**
     * The default setting whether the irrigation status shall be updated at projected irrigation ends additionally to
     * the regular polling (default: true)
     */
    public static final Boolean DEFAULT_UPDATE_AT_END = true;
    /**
     * The default setting whether the binding shall be used in test mode (default: false)
     */
    public static final Boolean DEFAULT_TEST_MODE = false;

    private @Nullable String user;
    private @Nullable String password;
    private @Nullable String clientId;
    private @Nullable String clientSecret;

    private Integer connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private Integer irrigationUpdateInterval = DEFAULT_IRRIGATION_INTERVALL;
    private Integer zoneUpdateInterval = DEFAULT_ZONE_INTERVALL;
    private Boolean realtimeUpdates = DEFAULT_REALTIME;
    private Boolean updateAfterIrrigation = DEFAULT_UPDATE_AT_END;
    private Boolean testMode = DEFAULT_TEST_MODE;

    public CloudrainConfig() {
    }

    /**
     * Returns the user to connect to the Cloudrain system.
     */
    public @Nullable String getUser() {
        return user;
    }

    /**
     * Sets the user to connect to the Cloudrain system.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Returns the password to connect to the Cloudrain system.
     */
    public @Nullable String getPassword() {
        return password;
    }

    /**
     * Sets the password to connect to the Cloudrain system.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the clientId to connect to the Cloudrain system.
     */
    public @Nullable String getClientId() {
        return clientId;
    }

    /**
     * Sets the clientId to connect to the Cloudrain system.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Returns the clientSecret to connect to the Cloudrain system.
     */
    public @Nullable String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the clientSecret to connect to the Cloudrain system.
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Returns the connection timeout to the Cloudrain system.
     */
    @Override
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connection timeout to the Cloudrain system.
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Returns the irrigation update interval for the Cloudrain API.
     */
    public Integer getIrrigationUpdateInterval() {
        return this.irrigationUpdateInterval;
    }

    /**
     * Sets the irrigation update interval for the Cloudrain API.
     */
    public void setIrrigationUpdateInterval(Integer irrigationUpdateInterval) {
        this.irrigationUpdateInterval = irrigationUpdateInterval;
    }

    /**
     * Returns the zone update interval for the Cloudrain API.
     */
    public Integer getZoneUpdateInterval() {
        return zoneUpdateInterval;
    }

    /**
     * Sets the zone update interval for the Cloudrain API.
     */
    public void setZoneUpdateInterval(Integer zoneUpdateInterval) {
        this.zoneUpdateInterval = zoneUpdateInterval;
    }

    /**
     * Returne whether to update remaining seconds of active irrigations in realtime
     */
    public boolean getRealtimeUpdates() {
        return realtimeUpdates;
    }

    /**
     * Sets whether to update remaining seconds of active irrigations in realtime
     */
    public void setRealtimeUpdates(Boolean realtimeUpdates) {
        this.realtimeUpdates = realtimeUpdates;
    }

    /**
     * Returns whether the status shall be updated using the API after the project end of irrigations
     */
    public Boolean getUpdateAfterIrrigation() {
        return updateAfterIrrigation;
    }

    /**
     * Sets whether the status shall be updated using the API after the project end of irrigations
     */
    public void setUpdateAfterIrrigation(Boolean updateAfterIrrigation) {
        this.updateAfterIrrigation = updateAfterIrrigation;
    }

    @Override
    public Boolean getTestMode() {
        return testMode;
    }

    /**
     * Defines whether the binding should be used in test mode or not
     */
    public void setTestMode(Boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * Validate the configuration if user, password, client id and client secret is specified.
     */
    public boolean isValid() {
        final String user = this.user;
        final String password = this.password;
        final String clientId = this.clientId;
        final String clientSecret = this.clientSecret;
        return user != null && !user.isBlank() && password != null && !password.isBlank() && clientId != null
                && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(CloudrainConfig.class.getSimpleName()).append("[");
        sb.append("user: ").append(user).append(", ");
        sb.append("clientId: ").append(clientId).append(", ");
        sb.append("clientSecret: ").append(clientSecret).append(", ");
        sb.append("connectionTimeout: ").append(connectionTimeout).append(", ");
        sb.append("irrigationUpdateInterval: ").append(irrigationUpdateInterval).append(", ");
        sb.append("zoneUpdateInterval: ").append(zoneUpdateInterval).append(", ");
        sb.append("realtimeUpdates: ").append(realtimeUpdates).append(", ");
        sb.append("updateAfterIrrigation: ").append(updateAfterIrrigation);
        sb.append("testMode: ").append(testMode);
        return sb.append("]").toString();
    }
}
