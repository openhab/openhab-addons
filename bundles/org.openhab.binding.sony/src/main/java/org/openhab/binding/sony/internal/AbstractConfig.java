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
package org.openhab.binding.sony.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base Configuration class for all configs in the sony system. This class defines the common configuration for each
 * handler
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class AbstractConfig {
    /** The device address (ipAddress for simpleIP, Full URL for others) */
    private @Nullable String deviceAddress;

    /** The device mac address. */
    private @Nullable String deviceMacAddress;

    /** The refresh time in seconds (null for default, < 1 to disable) */
    private @Nullable Integer refresh;

    /** The retry polling in seconds (null for default, < 1 to disable) */
    private @Nullable Integer retryPolling;

    /** The check status polling in seconds (null for default, < 1 to disable) */
    private @Nullable Integer checkStatusPolling;

    // ---- the following properties are not part of the config.xml (and are properties) ----

    /** The mac address that was discovered */
    private @Nullable String discoveredMacAddress;

    /**
     * Constructs (and returns) a URL represented by the {@link #deviceAddress}
     *
     * @return the non-null URL
     * @throws MalformedURLException if the deviceURL was an improper URL (or null/empty)
     */
    public URL getDeviceUrl() throws MalformedURLException {
        if (StringUtils.isEmpty(deviceAddress)) {
            throw new MalformedURLException("deviceAddress was blank");
        }
        return new URL(deviceAddress);
    }

    /**
     * Returns the IP address part of the device address or null if malformed, empty or null
     *
     * @return a possibly null, possibly empty IP Address
     */
    public @Nullable String getDeviceIpAddress() {
        try {
            return getDeviceUrl().getHost();
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns the device address
     *
     * @return the possibly null, possibly empty device address
     */
    public @Nullable String getDeviceAddress() {
        return deviceAddress;
    }

    /**
     * Sets the device address
     *
     * @param deviceAddress the possibly null, possibly empty device address
     */
    public void setDeviceAddress(final String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    /**
     * Gets the device mac address.
     *
     * @return the device mac address
     */
    public @Nullable String getDeviceMacAddress() {
        return StringUtils.defaultIfEmpty(deviceMacAddress, discoveredMacAddress);
    }

    /**
     * Sets the device mac address.
     *
     * @param deviceMacAddress the new device mac address
     */
    public void setDeviceMacAddress(final @Nullable String deviceMacAddress) {
        this.deviceMacAddress = deviceMacAddress;
    }

    /**
     * Sets the discovered mac address.
     *
     * @param discoveredMacAddress the device mac address
     */
    public void setDiscoveredMacAddress(final @Nullable String discoveredMacAddress) {
        this.discoveredMacAddress = discoveredMacAddress;
    }

    /**
     * Checks if is wol.
     *
     * @return true, if is wol
     */
    public boolean isWOL() {
        return StringUtils.isNotBlank(getDeviceMacAddress());
    }

    /**
     * Returns the refresh interval (-1/null to disable)
     *
     * @return a possibly null refresh interval
     */
    public @Nullable Integer getRefresh() {
        return refresh;
    }

    /**
     * Sets the refresh interval
     *
     * @param refresh the possibly null refresh interval
     */
    public void setRefresh(final Integer refresh) {
        this.refresh = refresh;
    }

    /**
     * Returns the retry connection polling interval (-1/null to disable)
     *
     * @return a possibly null polling interval
     */
    public @Nullable Integer getRetryPolling() {
        return retryPolling;
    }

    /**
     * Sets the polling interval
     *
     * @param retryPolling the possibly null polling interval
     */
    public void setRetryPolling(final Integer retryPolling) {
        this.retryPolling = retryPolling;
    }

    /**
     * Returns the check status polling interval (-1/null to disable)
     *
     * @return a possibly null check status interval
     */
    public @Nullable Integer getCheckStatusPolling() {
        return checkStatusPolling;
    }

    /**
     * Sets the check status interval
     *
     * @param checkStatusPolling the possibly null check status interval
     */
    public void setCheckStatusPolling(final Integer checkStatusPolling) {
        this.checkStatusPolling = checkStatusPolling;
    }

    /**
     * Returns the configuration as a map of properties
     *
     * @return a non-null, non-empty map
     */
    public Map<String, Object> asProperties() {
        final Map<String, Object> props = new HashMap<>();

        props.put("deviceAddress", StringUtils.defaultIfEmpty(deviceAddress, ""));
        props.put("discoveredMacAddress", StringUtils.defaultIfEmpty(discoveredMacAddress, ""));
        conditionallyAddProperty(props, "deviceMacAddress", deviceMacAddress);
        conditionallyAddProperty(props, "refresh", refresh);
        conditionallyAddProperty(props, "retryPolling", retryPolling);
        conditionallyAddProperty(props, "checkStatusPolling", checkStatusPolling);

        return props;
    }

    /**
     * Conditionally adds a property to the property map if the property is not null (or empty if a string)
     * 
     * @param props a non-null, possibly empty property map
     * @param propName a non-null, non-empty property name
     * @param propValue a possibly null, possibly empty (if string) property value
     */
    protected void conditionallyAddProperty(final Map<String, Object> props, final String propName,
            final @Nullable Object propValue) {
        Objects.requireNonNull(props, "props cannot be null");
        Validate.notEmpty(propName, "propName cannot be empty");

        if (propValue == null) {
            return;
        }

        if (propValue instanceof String && StringUtils.isEmpty((String) propValue)) {
            return;
        }

        props.put(propName, propValue);
    }
}
