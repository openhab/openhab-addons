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
package org.openhab.binding.dirigera.internal.interfaces;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dirigera.internal.DirigeraCommandProvider;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.internal.exception.ApiMissingException;
import org.openhab.binding.dirigera.internal.exception.ModelMissingException;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.i18n.TimeZoneProvider;
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
    public Thing getThing();

    /**
     * Get IP address from gateway for API calls and WebSocket connections.
     *
     * @return ip address as String
     */
    public String getIpAddress();

    /**
     * Get token associated to this gateway for API calls and WebSocket connections.
     *
     * @return token as String
     */
    public String getToken();

    /**
     * Get CommandProvider associated to this binding. For links and link candidates the command options are filled with
     * the right link options.
     *
     * @return DirigeraCommandProvider as DynamicCommandDescriptionProvider
     */
    public DirigeraCommandProvider getCommandProvider();

    /**
     * Get TimeZoneProvider to convert timestamps correctly.
     *
     * @return TimeZoneProvider
     */
    public TimeZoneProvider getTimeZoneProvider();

    /**
     * Returns the configuration setting if discovery is enabled.
     *
     * @return boolean discovery flag
     */
    public boolean discoveryEnabled();

    /**
     * Register a handler with the given deviceId reflecting a device or scene. Shall be called during
     * initialization.
     *
     * This function is handled asynchronous.
     *
     * @param deviceHandler handler of this binding
     * @param deviceId connected device id
     */
    public void registerDevice(BaseHandler deviceHandler, String deviceId);

    /**
     * Unregister a handler associated with the given deviceId reflecting a device or scene. Shall be called
     * during dispose.
     *
     * This function is handled asynchronous.
     *
     * @param deviceHandler handler of this binding
     * @param deviceId connected device id
     */
    public void unregisterDevice(BaseHandler deviceHandler, String deviceId);

    /**
     * Deletes an openHAB handler associated with the given deviceId reflecting a device or scene. Shall be called
     * during handleRemoval.
     *
     * This function is handled asynchronous.
     *
     * @param deviceHandler handler of this binding
     * @param deviceId connected device id
     */
    public void deleteDevice(BaseHandler deviceHandler, String deviceId);

    /**
     * Deletes a device or scene detected by the model. A device can be deleted without openHAB interaction in IKEA Home
     * smart app and openHAB needs to be informed about this removal to update ThingStatus accordingly.
     *
     * @param deviceId device id to be removed
     */
    public void deleteDevice(String deviceId);

    /**
     * Check if device id is known in the gateway namely if a handler is created or not.
     *
     * @param deviceId connected device id
     */
    public boolean isKnownDevice(String deviceId);

    /**
     * Update from websocket regarding changed data.
     *
     * This function is handled asynchronous.
     *
     * @param String content of update
     */
    public void websocketUpdate(String update);

    /**
     * Update links for all devices. Devices which are storing the links (hard link) are responsible to detect changes.
     * If change is detected the linked device will be updated with a soft link.
     *
     * This function is handled asynchronous.
     *
     * @param String content of update
     */
    public void updateLinks();

    /**
     * Read a resource file from this bundle. Some presets and commands sent to API shall not be implemented
     * in code if they are just needing minor String replacements.
     * Root path in project is src/main/resources. Line breaks and white spaces will
     *
     * @return
     */
    // public String getResourceFile(String fileName);

    /**
     * Next sunrise ZonedDateTime. Value is presented if gateway allows access to GPS position. Handler needs to take
     * care regarding null values.
     *
     * @return next sunrise as ZonedDateTime
     */
    public @Nullable ZonedDateTime getSunriseDateTime();

    /**
     * Next sunset ZonedDateTime. Value is presented if gateway allows access to GPS position. Handler needs to take
     * care regarding null values.
     *
     * @return next sunrise as ZonedDateTime
     */
    public @Nullable ZonedDateTime getSunsetDateTime();

    /**
     * Comfort access towards API which is only present after initialization.
     *
     * @throws ApiMissingException
     * @return DirigeraAPI
     */
    public DirigeraAPI api();

    /**
     * Comfort access towards Model which is only present after initialization.
     *
     * @throws ModelMissingException
     * @return Model
     */
    public Model model();

    /**
     * Comfort access towards DirigeraDiscoveryManager.
     *
     * @return DirigeraDiscoveryManager
     */
    public DirigeraDiscoveryManager discovery();

    /**
     * Comfort access towards DirigeraDiscoveryManager.
     *
     * @return DirigeraDiscoveryManager
     */
    public BundleContext getBundleContext();
}
