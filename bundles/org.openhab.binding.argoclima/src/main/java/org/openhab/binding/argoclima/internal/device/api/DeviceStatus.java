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

import java.net.URL;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationRemote;
import org.openhab.binding.argoclima.internal.device.passthrough.requests.DeviceSideUpdateDTO;
import org.openhab.binding.argoclima.internal.exception.ArgoApiCommunicationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the current device status, as-communicated by the device (either push or pull model)
 * <p>
 * Includes both the "raw" {@link #getCommandString() commandString} as well as {@link #getProperties() properties}
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
class DeviceStatus {
    //////////////
    // TYPES
    //////////////
    /**
     * Helper class for dealing with device properties
     *
     * @author Mateusz Bronk - Initial contribution
     */
    static class DeviceProperties {
        private static final Logger LOGGER = LoggerFactory.getLogger(DeviceProperties.class);

        private OffsetDateTime lastSeen = OffsetDateTime.MIN;

        @Nullable
        private final String localIP;
        @Nullable
        private final URL vendorUiUrl;
        @Nullable
        private String cpuId;
        @Nullable
        private String webUiUsername;
        @Nullable
        private String webUiPassword;
        @Nullable
        private String unitFWVersion;
        @Nullable
        private String wifiFWVersion;
        @Nullable
        private String wifiSSID;
        @Nullable
        private String wifiPassword;
        @Nullable
        private String localTime;

        /**
         * C-tor (from remote server query response)
         *
         * @param localIP The local IP of the Argo device (or empty string if N/A)
         * @param lastSeenStr The ISO-8601-formatted date/time of last update (or empty string if N/A)
         * @param vendorUiAddress The optional full URL to vendor's web UI
         */
        public DeviceProperties(String localIP, String lastSeenStr, URL vendorUiAddress) {
            this.localIP = !localIP.isBlank() ? localIP : null;
            this.vendorUiUrl = vendorUiAddress;
            this.lastSeen = dateFromISOString(lastSeenStr, "LastSeen");
        }

        /**
         * C-tor (from live poll response)
         *
         * @param lastSeen The date/time of last update (when the response got received)
         */
        public DeviceProperties(OffsetDateTime lastSeen) {
            this.localIP = null;
            this.lastSeen = lastSeen;
            this.vendorUiUrl = null;
        }

        /**
         * C-tor (from intercepted device-side query to remote)
         *
         * @param lastSeen The date/time of last update (when the message got intercepted)
         * @param properties The intercepted device-side request (most rich with properties)
         */
        public DeviceProperties(OffsetDateTime lastSeen, DeviceSideUpdateDTO properties) {
            this.localIP = Objects.requireNonNull(properties.setup.localIP.orElse(properties.deviceIp));
            this.lastSeen = lastSeen;
            this.vendorUiUrl = ArgoClimaRemoteDevice.getWebUiUrl(properties.remoteServerId, 80);
            this.cpuId = properties.cpuId;
            this.webUiUsername = properties.setup.username.orElse(properties.username);
            this.webUiPassword = properties.setup.password.get();
            this.unitFWVersion = Objects
                    .requireNonNull(properties.setup.unitVersionInstalled.orElse(properties.unitFirmware));
            this.wifiFWVersion = properties.setup.wifiVersionInstalled.orElse(properties.wifiFirmware);
            this.wifiSSID = properties.setup.wifiSSID.get();
            this.wifiPassword = properties.setup.wifiPassword.get();
            this.localTime = properties.setup.localTime.get();
        }

        private static OffsetDateTime dateFromISOString(String isoDateTime, String contextualName) {
            if (isoDateTime.isEmpty()) {
                return OffsetDateTime.MIN;
            }

            try {
                return OffsetDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(isoDateTime));
            } catch (DateTimeException ex) {
                // Swallowing exception (no need to handle - proceed as if the date was never provided)
                LOGGER.debug("Failed to parse [{}] timestamp: {}. Exception: {}", contextualName, isoDateTime,
                        ex.getMessage());
                return OffsetDateTime.MIN;
            }
        }

        private static String dateTimeToStringLocal(OffsetDateTime toConvert, TimeZoneProvider timeZoneProvider) {
            var timeAtZone = toConvert.atZoneSameInstant(timeZoneProvider.getTimeZone());
            return timeAtZone.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG));
        }

        /**
         * Returns duration between last update and now. If last update is N/A, picking lowest possible time value
         *
         * @return Time elapsed since last device-side update
         */
        Duration getLastSeenDelta() {
            return Duration.between(lastSeen.toInstant(), Instant.now());
        }

        /**
         * Return the properties in a map (ready to pass on to openHAB engine)
         *
         * @param timeZoneProvider TZ provider, for parsing date/time values
         * @return Properties map
         */
        SortedMap<String, String> asPropertiesRaw(TimeZoneProvider timeZoneProvider) {
            var result = new TreeMap<String, String>();

            String localIP = this.localIP;
            String cpuId = this.cpuId;
            URL vendorUiUrl = this.vendorUiUrl;
            String webUiUsername = this.webUiUsername;
            String webUiPassword = this.webUiPassword;
            String unitFWVersion = this.unitFWVersion;
            String wifiFWVersion = this.wifiFWVersion;
            String wifiSSID = this.wifiSSID;
            String wifiPassword = this.wifiPassword;
            String localTime = this.localTime;

            result.put(ArgoClimaBindingConstants.PROPERTY_LAST_SEEN,
                    dateTimeToStringLocal(this.lastSeen, timeZoneProvider));

            if (localIP != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_LOCAL_IP_ADDRESS, localIP);
            }
            if (cpuId != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_CPU_ID, cpuId);
            }
            if (vendorUiUrl != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_WEB_UI, vendorUiUrl.toString());
            }
            if (webUiUsername != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_WEB_UI_USERNAME, webUiUsername);
            }
            if (webUiPassword != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_WEB_UI_PASSWORD, webUiPassword);
            }
            if (unitFWVersion != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_UNIT_FW, unitFWVersion);
            }
            if (wifiFWVersion != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_WIFI_FW, wifiFWVersion);
            }
            if (wifiSSID != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_WIFI_SSID, wifiSSID);
            }
            if (wifiPassword != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_WIFI_PASSWORD, wifiPassword);
            }
            if (localTime != null) {
                result.put(ArgoClimaBindingConstants.PROPERTY_LOCAL_TIME, localTime);
            }
            return Collections.unmodifiableSortedMap(result);
        }
    }

    //////////////
    // FIELDS
    //////////////
    private final ArgoClimaTranslationProvider i18nProvider;
    private String commandString;
    private DeviceProperties properties;

    /**
     * C-tor (from command string and properties - either from remote server response or device-side poll intercept)
     *
     * @param commandString The device-side {@code HMI} string, carrying its updates and commands
     * @param properties The parsed device-side properties
     * @param i18nProvider Framework's translation provider
     * @implNote Consider: rewrite to a factory instead of this
     */
    public DeviceStatus(String commandString, DeviceProperties properties, ArgoClimaTranslationProvider i18nProvider) {
        this.commandString = commandString;
        this.properties = properties;
        this.i18nProvider = i18nProvider;
    }

    /**
     * C-tor (from just-received status response - live poll)
     *
     * @param commandString The command string received
     * @param lastSeenDateTime The date/time when the request has been received
     * @param i18nProvider Framework's translation provider
     */
    public DeviceStatus(String commandString, OffsetDateTime lastSeenDateTime,
            ArgoClimaTranslationProvider i18nProvider) {
        this(commandString, new DeviceProperties(lastSeenDateTime), i18nProvider);
    }

    /**
     * Retrieve the device {@code HMI} string, carrying its updates and commands
     *
     * @return The status/command string
     */
    public String getCommandString() {
        return this.commandString;
    }

    /**
     * Retrieve device-side properties
     *
     * @return Device properties
     */
    public DeviceProperties getProperties() {
        return this.properties;
    }

    /**
     * Throw exception if last update time is older than
     * {@link ArgoClimaConfigurationRemote#LAST_SEEN_UNAVAILABILITY_THRESHOLD the threshold}
     *
     * @throws ArgoApiCommunicationException If status is stale
     */
    public void throwIfStatusIsStale() throws ArgoApiCommunicationException {
        var delta = this.properties.getLastSeenDelta();
        if (delta.toSeconds() > ArgoClimaConfigurationRemote.LAST_SEEN_UNAVAILABILITY_THRESHOLD.toSeconds()) {
            throw new ArgoApiCommunicationException(
                    // "or more", since this message is also used in thing status (and we're not updating
                    // offline->offline). Actual "Last seen" can always be retrieved from properties
                    "Device was last seen {0} (or more) mins ago (threshold is set at {1} min). Please ensure the HVAC is connected to Wi-Fi and communicating with Argo servers",
                    "thing-status.cause.argoclima.remote-device-stale", i18nProvider, delta.toMinutes(),
                    ArgoClimaConfigurationRemote.LAST_SEEN_UNAVAILABILITY_THRESHOLD.toMinutes());
        }
    }
}
