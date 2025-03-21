/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.interfaces;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dirigera.internal.DirigeraCommandProvider;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryService;
import org.openhab.binding.dirigera.internal.exception.ApiException;
import org.openhab.binding.dirigera.internal.exception.ModelException;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.core.thing.Thing;
import org.osgi.framework.BundleContext;

/**
 * The {@link Gateway} Gateway interface to access data from other instances.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface Gateway {

    /**
     * Get the thing attached to this gateway.
     *
     * @return Thing
     */
    Thing getThing();

    /**
     * Get IP address from gateway for API calls and WebSocket connections.
     *
     * @return ip address as String
     */
    String getIpAddress();

    /**
     * Get token associated to this gateway for API calls and WebSocket connections.
     *
     * @return token as String
     */
    String getToken();

    /**
     * Get CommandProvider associated to this binding. For links and link candidates the command options are filled with
     * the right link options.
     *
     * @return DirigeraCommandProvider as DynamicCommandDescriptionProvider
     */
    DirigeraCommandProvider getCommandProvider();

    /**
     * Returns the configuration setting if discovery is enabled.
     *
     * @return boolean discovery flag
     */
    boolean discoveryEnabled();

    /**
     * Register a handler with the given deviceId reflecting a device or scene. Shall be called during
     * initialization.
     *
     * This function is handled asynchronous.
     *
     * @param deviceHandler handler of this binding
     * @param deviceId connected device id
     */
    void registerDevice(BaseHandler deviceHandler, String deviceId);

    /**
     * Unregister a handler associated with the given deviceId reflecting a device or scene. Shall be called
     * during dispose.
     *
     * This function is handled asynchronous.
     *
     * @param deviceHandler handler of this binding
     * @param deviceId connected device id
     */
    void unregisterDevice(BaseHandler deviceHandler, String deviceId);

    /**
     * Deletes an openHAB handler associated with the given deviceId reflecting a device or scene. Shall be called
     * during handleRemoval.
     *
     * This function is handled asynchronous.
     *
     * @param deviceHandler handler of this binding
     * @param deviceId connected device id
     */
    void deleteDevice(BaseHandler deviceHandler, String deviceId);

    /**
     * Deletes a device or scene detected by the model. A device can be deleted without openHAB interaction in IKEA Home
     * smart app and openHAB needs to be informed about this removal to update ThingStatus accordingly.
     *
     * @param deviceId device id to be removed
     */
    void deleteDevice(String deviceId);

    /**
     * Check if device id is known in the gateway namely if a handler is created or not.
     *
     * @param deviceId connected device id
     */
    boolean isKnownDevice(String deviceId);

    /**
     * Update websocket connected statues.
     *
     * @param boolean connected
     * @param reason as String
     */
    void websocketConnected(boolean connected, String reason);

    /**
     * Update from websocket regarding changed data.
     *
     * This function is handled asynchronous.
     *
     * @param String content of update
     */
    void websocketUpdate(String update);

    /**
     * Update links for all devices. Devices which are storing the links (hard link) are responsible to detect changes.
     * If change is detected the linked device will be updated with a soft link.
     *
     * This function is handled asynchronous.
     *
     * @param String content of update
     */
    void updateLinks();

    /**
     * Next sunrise ZonedDateTime. Value is presented if gateway allows access to GPS position. Handler needs to take
     * care regarding null values.
     *
     * @return next sunrise as ZonedDateTime
     */
    @Nullable
    Instant getSunriseDateTime();

    /**
     * Next sunset ZonedDateTime. Value is presented if gateway allows access to GPS position. Handler needs to take
     * care regarding null values.
     *
     * @return next sunrise as ZonedDateTime
     */
    @Nullable
    Instant getSunsetDateTime();

    /**
     * Comfort access towards API which is only present after initialization.
     *
     * @throws ApiMissingException
     * @return DirigeraAPI
     */
    DirigeraAPI api() throws ApiException;

    /**
     * Comfort access towards Model which is only present after initialization.
     *
     * @throws ModelMissingException
     * @return Model
     */
    Model model() throws ModelException;

    /**
     * Comfort access towards DirigeraDiscoveryManager.
     *
     * @return DirigeraDiscoveryManager
     */
    DirigeraDiscoveryService discovery();

    /**
     * Comfort access towards DirigeraDiscoveryManager.
     *
     * @return DirigeraDiscoveryManager
     */
    BundleContext getBundleContext();
}
