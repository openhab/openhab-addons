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
package org.openhab.binding.mielecloud.internal.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link MieleWebservice} that serves as a replacement when no webservice is available.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class UnavailableMieleWebservice implements MieleWebservice {
    public static final UnavailableMieleWebservice INSTANCE = new UnavailableMieleWebservice();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private UnavailableMieleWebservice() {
    }

    @Override
    public void setAccessToken(String accessToken) {
        logger.warn("Cannot set access token: The Miele cloud service is not available.");
    }

    @Override
    public boolean hasAccessToken() {
        logger.warn("There is no access token: The Miele cloud service is not available.");
        return false;
    }

    @Override
    public void connectSse() {
        logger.warn("Cannot connect to SSE stream: The Miele cloud service is not available.");
    }

    @Override
    public void disconnectSse() {
        logger.warn("Cannot disconnect from SSE stream: The Miele cloud service is not available.");
    }

    @Override
    public void fetchActions(String deviceId) {
        logger.warn("Cannot fetch actions for device '{}': The Miele cloud service is not available.", deviceId);
    }

    @Override
    public void putProcessAction(String deviceId, ProcessAction processAction) {
        logger.warn("Cannot perform '{}' operation for device '{}': The Miele cloud service is not available.",
                processAction, deviceId);
    }

    @Override
    public void putLight(String deviceId, boolean enabled) {
        logger.warn("Cannot set light state to '{}' for device '{}': The Miele cloud service is not available.",
                enabled ? "ON" : "OFF", deviceId);
    }

    @Override
    public void putPowerState(String deviceId, boolean enabled) {
        logger.warn("Cannot set power state to '{}' for device '{}': The Miele cloud service is not available.",
                enabled ? "ON" : "OFF", deviceId);
    }

    @Override
    public void putProgram(String deviceId, long programId) {
        logger.warn("Cannot activate program with ID '{}' for device '{}': The Miele cloud service is not available.",
                programId, deviceId);
    }

    @Override
    public void logout() {
        logger.warn("Cannot logout: The Miele cloud service is not available.");
    }

    @Override
    public void dispatchDeviceState(String deviceIdentifier) {
        logger.warn("Cannot re-emit device state for device '{}': The Miele cloud service is not available.",
                deviceIdentifier);
    }

    @Override
    public void addDeviceStateListener(DeviceStateListener listener) {
        logger.warn("Cannot add listener for all devices: The Miele cloud service is not available.");
    }

    @Override
    public void removeDeviceStateListener(DeviceStateListener listener) {
        logger.warn("Cannot remove listener: The Miele cloud service is not available.");
    }

    @Override
    public void addConnectionStatusListener(ConnectionStatusListener listener) {
        logger.warn("Cannot add connection error listener: The Miele cloud service is not available.");
    }

    @Override
    public void removeConnectionStatusListener(ConnectionStatusListener listener) {
        logger.warn("Cannot remove listener: The Miele cloud service is not available.");
    }

    @Override
    public void close() throws Exception {
    }
}
