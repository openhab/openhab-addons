/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.config;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.SHELLY1_CALLBACK_URI;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLY2_DEFAULT_USERID;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyApiConfiguration} class contains fields mapping thing configuration parameters.
 * Derived configuration consumed exclusively by the API layer.
 *
 * Thread-safe, mutable configuration (all access is synchronized). Created in
 * {@link ShellyBaseHandler#initializeThingConfig()}
 *
 * DNS resolution is controlled by the resolveHostname constructor parameter.
 * The handler constructor passes false (no DNS, safe on OSGi framework thread);
 * initializeThingConfig() rebuilds apiConfig with resolveHostname=true on the
 * scheduler thread where blocking is permitted.
 *
 * For regular devices deviceIp and deviceAddress end up identical (the resolved IP).
 * For BLU devices deviceIp is empty and deviceAddress holds the normalized MAC.
 * ShellyThingTable uses deviceAddress as the lookup key when routing gateway RPC
 * notifications to the correct BLU handler.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyApiConfiguration {
    private final Logger logger = LoggerFactory.getLogger(ShellyApiConfiguration.class);

    private static class ShellyAuthCredentials {
        private final String userId;
        private final String password;
        private final String bearer;

        public ShellyAuthCredentials(String defaultUserId, String defaultPassword, String userId, String password) {
            this.userId = userId.isBlank() ? defaultUserId : userId;
            this.password = password.isBlank() ? defaultPassword : password;
            this.bearer = this.userId + ":" + this.password;
        }
    }

    private static class ShellyApiUrls {
        private final String deviceApi;
        private final String websocketCallback;
        private final String eventCallback;

        public ShellyApiUrls(String localIp, String localPort, String deviceIp) {
            deviceApi = "http://" + deviceIp;
            if (localIp.isBlank()) {
                websocketCallback = "";
                eventCallback = "";
            } else {
                websocketCallback = "ws://" + localIp + ":" + localPort + "/shelly/wsevent";
                eventCallback = "http://" + localIp + ":" + localPort + SHELLY1_CALLBACK_URI + "/";
            }
        }
    }

    // All access must be guarded by "this"
    /** Unresolved device hostname, can be an IP or a name, with or without a {@code :port} suffix */
    private String deviceHostAddress;

    // All access must be guarded by "this"
    /** Device IP with or without port or {@code null} */
    private @Nullable InetSocketAddress deviceSocketAddr;

    // All access must be guarded by "this"
    /** Bluetooth device address, a unique 48-bit identifier, similar to a MAC address */
    private @Nullable String deviceBdAddr;

    /** Auth credentials */
    // All access must be guarded by "this"
    private @Nullable ShellyAuthCredentials credentials;

    // All access must be guarded by "this"
    private ShellyApiUrls urls;

    // All access must be guarded by "this"
    /** {@code true}: register for Relay btn_xxx events */
    private boolean eventsButton;

    // All access must be guarded by "this"
    /** {@code true}: register for device out_xxx events */
    private boolean eventsSwitch;

    // All access must be guarded by "this"
    /** {@code true}: register for short/long push events */
    private boolean eventsPush;

    // All access must be guarded by "this"
    /** {@code true}: register for roller events */
    private boolean eventsRoller;

    // All access must be guarded by "this"
    /** {@code true}: register for sensor events */
    private boolean eventsSensorReport;

    // Gen2
    // All access must be guarded by "this"
    private boolean enableBluGateway;

    // All access must be guarded by "this"
    private boolean enableRangeExtender;

    /** Local ip addresses used to create callback url */
    private final String localIp;

    /** Local port, used by callbacks through servlet */
    private final String localPort;

    // All access must be guarded by "this"
    /** mDNS service name or hostname provided by /shelly */
    private String realm = "";

    // All access must be guarded by "this"
    /** {@code true}: CoIoT/COAP enabled, event urls disabled */
    private boolean enableCoIOT = true;

    /**
     * Constructor for Thing handler — resolves the device hostname to an IP address.
     * Must only be called from a background/scheduler thread. Use the overload with
     * {@code resolveHostname=false} when constructing on the OSGi framework thread
     * (e.g. inside a ThingHandler constructor) to avoid blocking DNS lookups.
     *
     * @param thingConfig OH Thing configuration
     * @param bindingConfig Binding configuration
     * @param realm Realm, which is used for authentication (usually hostname / mDNS service name)
     * @param gen2 True for Generation 2 or newer devices
     */
    public ShellyApiConfiguration(ShellyThingConfiguration thingConfig, ShellyBindingRuntimeConfig bindingConfig,
            String realm, boolean gen2) {
        this(thingConfig, bindingConfig, realm, gen2, true);
    }

    /**
     * Constructor for Thing handler.
     *
     * @param thingConfig OH Thing configuration
     * @param bindingConfig Binding configuration
     * @param realm Realm, which is used for authentication (usually hostname / mDNS service name)
     * @param gen2 True for Generation 2 or newer devices
     * @param resolveHostname When {@code true} the device hostname is resolved to an IP address via
     *            DNS (blocking). Pass {@code false} when called from the framework/OSGi thread to
     *            avoid blocking; hostname resolution then happens on the first scheduler-thread call
     *            to {@code buildApiConfig()} inside {@code initializeThingConfig()}.
     */
    public ShellyApiConfiguration(ShellyThingConfiguration thingConfig, ShellyBindingRuntimeConfig bindingConfig,
            String realm, boolean gen2, boolean resolveHostname) {
        this.localIp = bindingConfig.getLocalIP();
        this.localPort = String.valueOf(bindingConfig.getHttpPort());
        this.realm = realm;

        // deviceIP can be an IP address, IP address:port or a FQDN, which needs to be resolved
        // deviceAddress is the MAC address for BLU device or the resolved IP address
        String bdAddr = thingConfig.getDeviceAddress();
        if (!bdAddr.isBlank()) {
            // BLU: remove : from MAC address and convert to lower case
            this.deviceBdAddr = bdAddr.toLowerCase(Locale.ROOT).replace(":", "");
            this.deviceHostAddress = "";
        } else {
            this.deviceBdAddr = null;
            String host = thingConfig.getDeviceIp();
            this.deviceHostAddress = host;
            if (resolveHostname) {
                this.deviceSocketAddr = resolveSocketAddr(host);
            }
        }

        credentials = new ShellyAuthCredentials(gen2 ? SHELLY2_DEFAULT_USERID : bindingConfig.getDefaultUserId(),
                bindingConfig.getDefaultPassword(), thingConfig.getUserId(), thingConfig.getPassword());

        enableBluGateway = thingConfig.getEnableBluGateway();
        enableRangeExtender = thingConfig.getEnableRangeExtender();

        enableCoIOT = gen2 ? false : thingConfig.getEventsCoIoT();
        eventsButton = thingConfig.getEventsButton();
        eventsSwitch = thingConfig.getEventsSwitch();
        eventsPush = thingConfig.getEventsPush();
        eventsRoller = thingConfig.getEventsRoller();
        eventsSensorReport = thingConfig.getEventsSensorReport();

        urls = new ShellyApiUrls(localIp, localPort, deviceHostAddress);
    }

    /**
     * Constructor for Discovery Mode
     *
     * @param bindingConfig Binding configuration
     * @param realm Realm, which is used for authentication (usually hostname / mDNS service name)
     * @param host Device IP address/hostname
     */
    public ShellyApiConfiguration(ShellyBindingRuntimeConfig bindingConfig, String realm, String host) {
        this.realm = realm; // mDNS service name or hostname provided by /shelly
        this.deviceHostAddress = host;
        this.deviceSocketAddr = resolveSocketAddr(host);
        this.deviceBdAddr = null;

        localIp = bindingConfig.getLocalIP();
        localPort = String.valueOf(bindingConfig.getHttpPort());
        credentials = new ShellyAuthCredentials(bindingConfig.getDefaultUserId(), bindingConfig.getDefaultPassword(),
                "", "");

        // Disable all features not required in discovery mode
        // enableCoIOT defaults to true at class level, so explicitly clear it here
        enableCoIOT = false;
        urls = new ShellyApiUrls(localIp, localPort, host);
    }

    private @Nullable InetSocketAddress resolveSocketAddr(String hostname) {
        InetSocketAddress result = null;
        if (!hostname.isBlank()) {
            try {
                String ip = hostname.contains(":") ? substringBefore(hostname, ":") : hostname;
                String port = hostname.contains(":") ? substringAfter(hostname, ":") : "";
                InetAddress addr = InetAddress.getByName(ip);
                int portNum = 0;
                if (!port.isBlank()) {
                    try {
                        portNum = Integer.parseInt(port);
                    } catch (NumberFormatException e) {
                        logger.warn("{}: Invalid port number '{}' - ignoring", realm, port);
                    }
                }
                result = new InetSocketAddress(addr, portNum);
                String resultAddr = addr.getHostAddress();
                if (!ip.equals(resultAddr) && logger.isDebugEnabled()) {
                    logger.debug("{}: hostname {} resolved to IP address {}", realm, hostname,
                            resultAddr + (port.isEmpty() ? "" : ":" + port));
                }
            } catch (UnknownHostException e) {
                logger.debug("{}: Unable to resolve hostname {}", realm, hostname);
            }
        } else {
            logger.debug("{}: Hostname is missing", realm);
            return null;
        }
        if (result == null) {
            logger.debug("{}: Hostname '{}' is invalid", realm, hostname);
        }
        return result;
    }

    /**
     * The unresolved device hostname, can be an IP or a name, with or without a {@code :port} suffix.
     *
     * @return The unresolved device hostname.
     */
    public synchronized String getDeviceHostAddress() {
        return deviceHostAddress;
    }

    /**
     * @return The device IP or {@code null}.
     */
    public synchronized @Nullable InetAddress getDeviceIpAddress() {
        InetSocketAddress isa = deviceSocketAddr;
        return isa == null ? null : isa.getAddress();
    }

    /**
     * The device IP with or without port or {@code null}. The port is {@code 0} if not specified.
     *
     * @return The device socket address or {@code null}.
     */
    public synchronized @Nullable InetSocketAddress getDeviceSocketAddress() {
        return deviceSocketAddr;
    }

    public synchronized void setDeviceIp(@Nullable InetSocketAddress deviceIp) {
        this.deviceSocketAddr = deviceIp;
    }

    /**
     * The Bluetooth device address, a unique 48-bit identifier, similar to a MAC address.
     *
     * @return The Bluetooth device addres or {@code null}.
     */
    public synchronized @Nullable String getBdAddr() {
        return deviceBdAddr;
    }

    public synchronized String getUserId() {
        ShellyAuthCredentials credentials = this.credentials;
        return credentials == null ? "" : credentials.userId;
    }

    public synchronized String getPassword() {
        ShellyAuthCredentials credentials = this.credentials;
        return credentials == null ? "" : credentials.password;
    }

    public synchronized void setCredentials(String userId, String password) {
        credentials = new ShellyAuthCredentials("", "", userId, password);
    }

    public synchronized String getBearer() {
        ShellyAuthCredentials credentials = this.credentials;
        return credentials == null ? "" : credentials.bearer;
    }

    public synchronized boolean getEventsButton() {
        return eventsButton;
    }

    public synchronized boolean getEventsSwitch() {
        return eventsSwitch;
    }

    public synchronized boolean getEventsPush() {
        return eventsPush;
    }

    public synchronized boolean getEventsRoller() {
        return eventsRoller;
    }

    public synchronized boolean getEventsSensorReport() {
        return eventsSensorReport;
    }

    public synchronized String getDeviceApiUrl() {
        return urls.deviceApi;
    }

    public synchronized String getEventCallbackUrl() {
        return urls.eventCallback;
    }

    public synchronized String getWebSocketCallback() {
        return urls.websocketCallback;
    }

    public synchronized boolean getEnableBluGateway() {
        return enableBluGateway;
    }

    public synchronized boolean getEnableRangeExtender() {
        return enableRangeExtender;
    }

    public String getLocalIp() {
        return localIp;
    }

    public synchronized boolean getEnableCoIOT() {
        return enableCoIOT;
    }

    public synchronized void setEnableCoIOT(boolean value) {
        enableCoIOT = value;
    }

    public synchronized String getRealm() {
        return realm;
    }

    public synchronized void setRealm(String value) {
        realm = value;
    }

    /**
     * Attempt to resolve the device host name into an IP address.
     *
     * @return {@code true} if the IP address is resolved, {@code false} if it isn't.
     */
    public synchronized boolean resolveIp() {
        InetSocketAddress socketAddr = deviceSocketAddr;
        if (socketAddr != null) {
            // Already resolved, just return
            return true;
        }
        socketAddr = resolveSocketAddr(this.deviceHostAddress);
        if (socketAddr == null) {
            logger.debug("{}: Failed to resolve hostname '{}'", realm, deviceHostAddress);
            return false;
        }
        deviceSocketAddr = socketAddr;
        return true;
    }

    /**
     * Refreshes all fields derived from the thing configuration so that changes made via
     * {@code handleConfigurationUpdate()} (new credentials, device address, event flags, etc.)
     * take effect on the next re-initialization cycle without rebuilding the handler or API client.
     *
     * @param thingConfig refreshed thing configuration
     * @param bindingConfig current binding runtime configuration
     * @param gen2 true for Gen2+ devices
     */
    public synchronized void updateFromThingConfig(ShellyThingConfiguration thingConfig,
            ShellyBindingRuntimeConfig bindingConfig, boolean gen2) {
        credentials = new ShellyAuthCredentials(gen2 ? SHELLY2_DEFAULT_USERID : bindingConfig.getDefaultUserId(),
                bindingConfig.getDefaultPassword(), thingConfig.getUserId(), thingConfig.getPassword());

        eventsButton = thingConfig.getEventsButton();
        eventsSwitch = thingConfig.getEventsSwitch();
        eventsPush = thingConfig.getEventsPush();
        eventsRoller = thingConfig.getEventsRoller();
        eventsSensorReport = thingConfig.getEventsSensorReport();
        enableCoIOT = !gen2 && thingConfig.getEventsCoIoT();
        enableBluGateway = thingConfig.getEnableBluGateway();
        enableRangeExtender = thingConfig.getEnableRangeExtender();

        String bdAddr = thingConfig.getDeviceAddress();
        if (!bdAddr.isBlank()) {
            deviceBdAddr = bdAddr.toLowerCase(Locale.ROOT).replace(":", "");
            deviceHostAddress = "";
            deviceSocketAddr = null;
        } else {
            deviceBdAddr = null;
            String newHost = thingConfig.getDeviceIp();
            if (!newHost.equals(deviceHostAddress)) {
                deviceHostAddress = newHost;
                deviceSocketAddr = null; // force re-resolve on next resolveIp() call
                resolveIp();
                urls = new ShellyApiUrls(localIp, localPort, newHost);
            }
        }
    }

    @Override
    public synchronized String toString() {
        ShellyAuthCredentials credentials = this.credentials;
        String bdAddr = this.deviceBdAddr;
        return (bdAddr != null ? "Bluetooth device address=" + bdAddr + ", " : "") + "HTTP user/password=" + getUserId()
                + "/" + (credentials == null || credentials.password.isEmpty() ? "<none>" : "***") + "\n"
                + "Events: Button: " + eventsButton + ", Switch (on/off): " + eventsSwitch + ", Push: " + eventsPush
                + ", Roller: " + eventsRoller + ", Sensor: " + eventsSensorReport + ", CoIoT: " + enableCoIOT + "\n"
                + "Blu Gateway=" + enableBluGateway + ", Range Extender: " + enableRangeExtender;
    }
}
