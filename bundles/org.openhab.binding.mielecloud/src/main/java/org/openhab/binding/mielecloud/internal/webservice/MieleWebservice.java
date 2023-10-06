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
package org.openhab.binding.mielecloud.internal.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;

/**
 * The {@link MieleWebservice} serves as an interface to the Miele REST API and wraps all calls to it.
 *
 * @author Björn Lange and Roland Edelhoff - Initial contribution
 */
@NonNullByDefault
public interface MieleWebservice extends AutoCloseable {
    /**
     * Sets the OAuth2 access token to use.
     */
    void setAccessToken(String accessToken);

    /**
     * Returns whether an access token is available.
     */
    boolean hasAccessToken();

    /**
     * Connects to the Miele webservice SSE endpoint and starts receiving events.
     */
    void connectSse();

    /**
     * Disconnects a running connection from the Miele SSE endpoint.
     */
    void disconnectSse();

    /**
     * Fetches the available actions for the device with the given {@code deviceId}.
     *
     * @param deviceId The unique ID of the device to fetch the available actions for.
     * @throws MieleWebserviceException if an error occurs during webservice requests or content parsing.
     * @throws AuthorizationFailedException if the authorization against the webservice failed.
     * @throws TooManyRequestsException if too many requests have been made against the webservice recently.
     */
    void fetchActions(String deviceId);

    /**
     * Performs a PUT operation with the given {@code processAction}.
     *
     * @param deviceId ID of the device to trigger the action for.
     * @param processAction The action to perform.
     * @throws MieleWebserviceException if an error occurs during webservice requests or content parsing.
     * @throws AuthorizationFailedException if the authorization against the webservice failed.
     * @throws TooManyRequestsException if too many requests have been made against the webservice recently.
     */
    void putProcessAction(String deviceId, ProcessAction processAction);

    /**
     * Performs a PUT operation enabling or disabling the device's light.
     *
     * @param deviceId ID of the device to trigger the action for.
     * @param enabled {@code true} to enable or {@code false} to disable the light.
     * @throws MieleWebserviceException if an error occurs during webservice requests or content parsing.
     * @throws AuthorizationFailedException if the authorization against the webservice failed.
     * @throws TooManyRequestsException if too many requests have been made against the webservice recently.
     */
    void putLight(String deviceId, boolean enabled);

    /**
     * Performs a PUT operation switching the device on or off.
     *
     * @param deviceId ID of the device to trigger the action for.
     * @param enabled {@code true} to switch on or {@code false} to switch off the device.
     * @throws MieleWebserviceException if an error occurs during webservice requests or content parsing.
     * @throws AuthorizationFailedException if the authorization against the webservice failed.
     * @throws TooManyRequestsException if too many requests have been made against the webservice recently.
     */
    void putPowerState(String deviceId, boolean enabled);

    /**
     * Performs a PUT operation setting the active program.
     *
     * @param deviceId ID of the device to trigger the action for.
     * @param programId The program to activate.
     * @throws MieleWebserviceException if an error occurs during webservice requests or content parsing.
     * @throws AuthorizationFailedException if the authorization against the webservice failed.
     * @throws TooManyRequestsException if too many requests have been made against the webservice recently.
     */
    void putProgram(String deviceId, long programId);

    /**
     * Performs a logout and invalidates the current OAuth2 token. This operation is assumed to work on the first try
     * and is never retried. HTTP errors are ignored.
     *
     * @throws MieleWebserviceException if the request operation fails.
     */
    void logout();

    /**
     * Dispatches the cached state of the device identified by the given device identifier.
     */
    void dispatchDeviceState(String deviceIdentifier);

    /**
     * Adds a {@link DeviceStateListener}.
     *
     * @param listener The listener to add.
     */
    void addDeviceStateListener(DeviceStateListener listener);

    /**
     * Removes a {@link DeviceStateListener}.
     *
     * @param listener The listener to remove.
     */
    void removeDeviceStateListener(DeviceStateListener listener);

    /**
     * Adds a {@link ConnectionStatusListener}.
     *
     * @param listener The listener to add.
     */
    void addConnectionStatusListener(ConnectionStatusListener listener);

    /**
     * Removes a {@link ConnectionStatusListener}.
     *
     * @param listener The listener to remove.
     */
    void removeConnectionStatusListener(ConnectionStatusListener listener);
}
