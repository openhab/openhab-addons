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
package org.openhab.binding.argoclima.internal.device.api;

import java.io.EOFException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.URIUtil;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationBase;
import org.openhab.binding.argoclima.internal.device.api.protocol.ArgoApiDataElement;
import org.openhab.binding.argoclima.internal.device.api.protocol.ArgoDeviceStatus;
import org.openhab.binding.argoclima.internal.device.api.protocol.elements.IArgoCommandableElement.IArgoElement;
import org.openhab.binding.argoclima.internal.device.api.types.ArgoDeviceSettingType;
import org.openhab.binding.argoclima.internal.exception.ArgoApiCommunicationException;
import org.openhab.binding.argoclima.internal.exception.ArgoApiProtocolViolationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation of Argo API (across local and remote connection modes)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public abstract class ArgoClimaDeviceApiBase implements IArgoClimaDeviceAPI {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpClient client;
    protected final TimeZoneProvider timeZoneProvider;
    protected final ArgoClimaTranslationProvider i18nProvider;
    protected final ArgoDeviceStatus deviceStatus;
    protected Consumer<SortedMap<String, String>> onDevicePropertiesUpdate;
    protected SortedMap<String, String> deviceProperties;
    private final String remoteEndName;

    /**
     * C-tor
     *
     * @param config The configuration class (common part)
     * @param client The common HTTP client used for making connections from OH to the device
     * @param timeZoneProvider The common TZ provider
     * @param onDevicePropertiesUpdate Callback to invoke on device-side dynamic property update (ex. lastSeen)
     * @param remoteEndName The name of the "remote end" party, for use in logging
     * @param i18nProvider Framework's translation provider
     */
    public ArgoClimaDeviceApiBase(ArgoClimaConfigurationBase config, HttpClient client,
            TimeZoneProvider timeZoneProvider, ArgoClimaTranslationProvider i18nProvider,
            Consumer<SortedMap<String, String>> onDevicePropertiesUpdate, String remoteEndName) {
        this.client = client;
        this.timeZoneProvider = timeZoneProvider;
        this.i18nProvider = i18nProvider;
        this.deviceStatus = new ArgoDeviceStatus(config);
        this.onDevicePropertiesUpdate = onDevicePropertiesUpdate;
        this.deviceProperties = new TreeMap<String, String>();
        this.remoteEndName = remoteEndName.isBlank() ? "DEVICE" : remoteEndName.trim().toUpperCase();
    }

    /**
     * Return the URL used for querying device state (poll)
     *
     * @return The "poll for status" URL (w/o any changes)
     */
    protected abstract URL getDeviceStateQueryUrl();

    /**
     * Return the URL used for updating device state (a command)
     *
     * @return The "send command" URL (effecting changes)
     */
    protected abstract URL getDeviceStateUpdateUrl();

    /**
     * Extract device status from just-polled API result (local or remote)
     *
     * @param apiResponse The response received from device (body of the response, ex. one obtained through
     *            {@link #pollForCurrentStatusFromDeviceSync(URL)}.
     * @return The {@link DeviceStatus} parsed from response (with properties pre-parsed)
     * @throws ArgoApiCommunicationException If the response body was not recognized as a valid protocol message
     */
    protected abstract DeviceStatus extractDeviceStatusFromResponse(String apiResponse)
            throws ArgoApiCommunicationException;

    /**
     * Helper class method for converting strings to URIs (assumes HTTP for the protocol)
     *
     * @implNote Throwing unchecked exceptions, as this function is used in practice only for URLs returned by
     *           {@link org.eclipse.jetty.util.URIUtil#newURI}, so a scenario where it would be malformed is extremely
     *           unlikely and we DO NOT want nice handling for it
     *
     * @param server The server address (hostname or IP)
     * @param port The server port
     * @param path The resource path (ex. '/')
     * @param query The query parameters
     *
     * @return Converted URL
     */
    protected static final URL newUrl(String server, int port, String path, String query) {
        var uriStr = URIUtil.newURI("http", server, port, path, query);
        try {
            return new URL(uriStr);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Failed to build url from: " + uriStr, e);
        }
    }

    /**
     * Trigger device-side communication (synchronous!) and get the response
     * <p>
     * Note: The Argo API violates HTTP spec. and uses GET requests for both state retrieval (idempotent) as well as
     * control! The query params of the URL determine the mode.
     * <p>
     * In case of binding/Thing shutdown, this function may terminate early (not waiting for I/O to complete) and return
     * an empty string
     *
     * @implNote This method should not be used if {@link ArgoClimaBindingConstants#PARAMETER_USE_LOCAL_CONNECTION} is
     *           false, though the implementation is NOT enforcing it (SHOULD NOT != MAY NOT).
     * @param url URL to call (should contain full protocol message to send to the device through HTTP GET (such as ones
     *            obtained through {@link #getDeviceStateQueryUrl} or {@link #getDeviceStateUpdateUrl()}
     * @return The device-side reply (HTTP response body)
     * @throws ArgoApiCommunicationException Thrown in case of communication issues (including timeouts) or if the
     *             API returned a response different from {@code HTTP 200 OK}
     */
    protected String pollForCurrentStatusFromDeviceSync(URL url) throws ArgoApiCommunicationException {
        try {
            logger.trace("Communication: OPENHAB --> {}: [GET {}]", remoteEndName, url);

            ContentResponse resp = this.client.GET(url.toString()); // sync

            logger.trace("   [response]: OPENHAB <-- {}: [{} {} {} - {} bytes], body=[{}]", remoteEndName,
                    resp.getVersion(), resp.getStatus(), resp.getReason(), resp.getContent().length,
                    resp.getContentAsString());

            if (resp.getStatus() != 200) {
                throw new ArgoApiCommunicationException(
                        "API request yielded invalid response status {0} {1} (expected HTTP 200 OK). URL was: {2}",
                        "thing-status.cause.argoclima.invalid-api-response-status", i18nProvider, resp.getStatus(),
                        resp.getReason(), url);
            }
            return Objects.requireNonNull(resp.getContentAsString());
        } catch (InterruptedException ex) {
            logger.trace("Interrupted...");
            return "";
        } catch (ExecutionException ex) {
            var cause = Optional.ofNullable(ex.getCause());
            if (cause.isPresent() && cause.get() instanceof EOFException) {
                throw new ArgoApiCommunicationException(
                        "Device did not respond on its socket (EOF). Check that the device is correctly communicating with Argo servers (or openHAB stub server)",
                        "thing-status.cause.argoclima.device-eof", i18nProvider);
            }

            throw new ArgoApiCommunicationException("Device communication error: {0}",
                    "thing-status.cause.argoclima.communication-error", i18nProvider, ex.getCause(),
                    Objects.requireNonNullElse(ex.getCause(), ex).getLocalizedMessage());

        } catch (TimeoutException e) {
            throw new ArgoApiCommunicationException("Timeout: {0}",
                    "thing-status.cause.argoclima.communication-error.timeout", i18nProvider, e.getLocalizedMessage());
        }
    }

    /**
     * Updates cached device properties with the values just received from device and notifies the framework through
     * callback
     *
     * @param metadata The properties received from device
     * @param status The status update from device
     */
    protected void updateDevicePropertiesFromDeviceResponse(DeviceStatus.DeviceProperties metadata,
            ArgoDeviceStatus status) {
        var metaProperties = metadata.asPropertiesRaw(this.timeZoneProvider);
        var responseProperties = Map.<String, String> of(ArgoClimaBindingConstants.PROPERTY_UNIT_FW,
                status.getSetting(ArgoDeviceSettingType.UNIT_FIRMWARE_VERSION).toString(false));

        synchronized (this) {
            // not clearing the existing properties (grow-only)
            this.deviceProperties.putAll(metaProperties);
            this.deviceProperties.putAll(responseProperties);
        }
        this.onDevicePropertiesUpdate.accept(getCurrentDeviceProperties());
    }

    @Override
    public final SortedMap<String, String> getCurrentDeviceProperties() {
        return Collections.unmodifiableSortedMap(this.deviceProperties);
    }

    @Override
    public Map<ArgoDeviceSettingType, State> queryDeviceForUpdatedState() throws ArgoApiCommunicationException {
        var deviceResponse = extractDeviceStatusFromResponse(
                pollForCurrentStatusFromDeviceSync(getDeviceStateQueryUrl()));
        try {
            this.deviceStatus.fromDeviceString(deviceResponse.getCommandString());
        } catch (ArgoApiProtocolViolationException e) {
            throw new ArgoApiCommunicationException("Unrecognized API response",
                    "thing-status.cause.argoclima.exception.unrecognized-response", i18nProvider, e);
        }
        this.updateDevicePropertiesFromDeviceResponse(deviceResponse.getProperties(), this.deviceStatus);
        deviceResponse.throwIfStatusIsStale();
        return this.deviceStatus.getCurrentStateMap();
    }

    @Override
    public Map<ArgoDeviceSettingType, State> getLastStateReadFromDevice() {
        return this.deviceStatus.getCurrentStateMap();
    }

    @Override
    public void sendCommandsToDevice() throws ArgoApiCommunicationException {
        var deviceResponse = pollForCurrentStatusFromDeviceSync(getDeviceStateUpdateUrl());

        notifyCommandsPassedToDevice(); // Just sent directly
        logger.trace("State update command finished. Device response: {}", deviceResponse);
    }

    @Override
    public void notifyCommandsPassedToDevice() {
        deviceStatus.getItemsWithPendingUpdates().forEach(x -> x.notifyCommandSent());
    }

    @Override
    public boolean handleSettingCommand(ArgoDeviceSettingType settingType, Command command) {
        return this.deviceStatus.getSetting(settingType).handleCommand(command);
    }

    @Override
    public State getCurrentStateNoPoll(ArgoDeviceSettingType settingType) {
        return this.deviceStatus.getSetting(settingType).getState();
    }

    @Override
    public boolean hasPendingCommands() {
        var itemsWithPendingUpdates = this.deviceStatus.getItemsWithPendingUpdates();
        logger.trace("Items to update: {}", itemsWithPendingUpdates);
        return !itemsWithPendingUpdates.isEmpty();
    }

    @Override
    public List<ArgoApiDataElement<IArgoElement>> getItemsWithPendingUpdates() {
        return this.deviceStatus.getItemsWithPendingUpdates();
    }
}
