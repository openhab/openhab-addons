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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyApiConfiguration} class contains fields mapping thing configuration parameters.
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

    /** Unresolved device hostname, can be an IP or a name, with or without a {@code :port} suffix */
    private final AtomicReference<String> deviceHostAddress = new AtomicReference<>("");

    /** Device IP with or without port or {@code null} */
    private final AtomicReference<@Nullable InetSocketAddress> deviceSocketAddr = new AtomicReference<>();

    /** Bluetooth device address, a unique 48-bit identifier, similar to a MAC address */
    private final AtomicReference<@Nullable String> deviceBdAddr = new AtomicReference<>();
    private final AtomicReference<ShellyAuthCredentials> credentials = new AtomicReference<>(
            new ShellyAuthCredentials("", "", "", "")); // auth credentials
    private final AtomicReference<ShellyApiUrls> urls = new AtomicReference<>(new ShellyApiUrls("", "0", ""));

    private final AtomicBoolean eventsButton = new AtomicBoolean(false);
    private final AtomicBoolean eventsSwitch = new AtomicBoolean(false);
    private final AtomicBoolean eventsPush = new AtomicBoolean(false);
    private final AtomicBoolean eventsRoller = new AtomicBoolean(false);
    private final AtomicBoolean eventsSensorReport = new AtomicBoolean(false);

    // Gen2
    private final AtomicBoolean enableBluGateway = new AtomicBoolean(false);
    private final AtomicBoolean enableRangeExtender = new AtomicBoolean(false);

    private final String localIp; // local ip addresses used to create callback url
    private final String localPort; // local port, used by callbacks through servlet

    /*
     * The two fields below are intentionally mutable after construction:
     *
     * - realm: not known until the first /shelly response is parsed (hostname may be returned by
     * the device only after getDeviceInfo() or getDeviceProfile() succeeds).
     * - enableCoIOT: the auto-CoIoT feature (bindingConfig.autoCoIoT) promotes this flag from
     * false → true at runtime after the firmware version is checked in checkVersion().
     *
     * Both use thread-safe atomic types and are the only allowed exceptions to the
     * "immutable after construction" rule for ShellyApiConfiguration.
     */
    private final AtomicReference<String> realm = new AtomicReference<>("");
    private final AtomicBoolean enableCoIOT = new AtomicBoolean(true); // true: CoIoT/COAP enabled, event urls disabled

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
        this.realm.set(realm);

        // deviceIP can be an IP address, IP address:port or a FQDN, which needs to be resolved
        // deviceAddress is the MAC address for BLU device or the resolved IP address
        String bdAddr = thingConfig.getDeviceAddress();
        if (!bdAddr.isBlank()) {
            // BLU: remove : from MAC address and convert to lower case
            this.deviceBdAddr.set(bdAddr.toLowerCase(Locale.ROOT).replace(":", ""));
            this.deviceHostAddress.set("");
        } else {
            this.deviceBdAddr.set(null);
            String host = thingConfig.getDeviceIp();
            this.deviceHostAddress.set(host);
            if (resolveHostname) {
                this.deviceSocketAddr.set(resolveSocketAddr(host));
            }
        }

        credentials.set(new ShellyAuthCredentials(gen2 ? SHELLY2_DEFAULT_USERID : bindingConfig.getDefaultUserId(),
                bindingConfig.getDefaultPassword(), thingConfig.getUserId(), thingConfig.getPassword()));

        enableBluGateway.set(thingConfig.getEnableBluGateway());
        enableRangeExtender.set(thingConfig.getEnableRangeExtender());

        enableCoIOT.set(!gen2 && thingConfig.getEventsCoIoT());
        eventsButton.set(thingConfig.getEventsButton());
        eventsSwitch.set(thingConfig.getEventsSwitch());
        eventsPush.set(thingConfig.getEventsPush());
        eventsRoller.set(thingConfig.getEventsRoller());
        eventsSensorReport.set(thingConfig.getEventsSensorReport());

        urls.set(new ShellyApiUrls(localIp, localPort, this.deviceHostAddress.get()));
    }

    /**
     * Constructor for Discovery Mode
     *
     * @param bindingConfig Binding configuration
     * @param realm Realm, which is used for authentication (usually hostname / mDNS service name)
     * @param host Device IP address/hostname
     */
    public ShellyApiConfiguration(ShellyBindingRuntimeConfig bindingConfig, String realm, String host) {
        this.realm.set(realm); // mDNS service name or hostname provided by /shelly
        this.deviceHostAddress.set(host);
        this.deviceSocketAddr.set(resolveSocketAddr(host));
        this.deviceBdAddr.set(null);

        localIp = bindingConfig.getLocalIP();
        localPort = String.valueOf(bindingConfig.getHttpPort());
        credentials.set(new ShellyAuthCredentials(bindingConfig.getDefaultUserId(), bindingConfig.getDefaultPassword(),
                "", ""));

        // Disable all features not required in discovery mode
        // enableCoIOT defaults to true at class level, so explicitly clear it here
        enableCoIOT.set(false);
        urls.set(new ShellyApiUrls(localIp, localPort, host));
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
                        logger.warn("{}: Invalid port number '{}' - ignoring", realm.get(), port);
                    }
                }
                result = new InetSocketAddress(addr, portNum);
                String resultAddr = addr.getHostAddress();
                if (!ip.equals(resultAddr) && logger.isDebugEnabled()) {
                    logger.debug("{}: hostname {} resolved to IP address {}", realm.get(), hostname,
                            resultAddr + (port.isEmpty() ? "" : ":" + port));
                }
            } catch (UnknownHostException e) {
                logger.debug("{}: Unable to resolve hostname {}", realm.get(), hostname);
            }
        } else {
            logger.debug("{}: Hostname is missing", realm.get());
            return null;
        }
        if (result == null) {
            logger.debug("{}: Hostname '{}' is invalid", realm.get(), hostname);
        }
        return result;
    }

    /**
     * The unresolved device hostname, can be an IP or a name, with or without a {@code :port} suffix.
     *
     * @return The unresolved device hostname.
     */
    public String getDeviceHostAddress() {
        return deviceHostAddress.get();
    }

    /**
     * @return The device IP or {@code null}.
     */
    public @Nullable InetAddress getDeviceIpAddress() {
        InetSocketAddress isa = deviceSocketAddr.get();
        return isa == null ? null : isa.getAddress();
    }

    /**
     * The device IP with or without port or {@code null}. The port is {@code 0} if not specified.
     *
     * @return The device socket address or {@code null}.
     */
    public @Nullable InetSocketAddress getDeviceSocketAddress() {
        return deviceSocketAddr.get();
    }

    public void setDeviceIp(@Nullable InetSocketAddress deviceIp) {
        this.deviceSocketAddr.set(deviceIp);
    }

    /**
     * The Bluetooth device address, a unique 48-bit identifier, similar to a MAC address.
     *
     * @return The Bluetooth device addres or {@code null}.
     */
    public @Nullable String getBdAddr() {
        return deviceBdAddr.get();
    }

    public String getUserId() {
        return credentials.get().userId;
    }

    public String getPassword() {
        return credentials.get().password;
    }

    public void setCredentials(String userId, String password) {
        ShellyAuthCredentials cred = new ShellyAuthCredentials("", "", userId, password);
        credentials.set(cred);
    }

    public String getBearer() {
        return credentials.get().bearer;
    }

    public boolean getEventsButton() {
        return eventsButton.get();
    }

    public boolean getEventsSwitch() {
        return eventsSwitch.get();
    }

    public boolean getEventsPush() {
        return eventsPush.get();
    }

    public boolean getEventsRoller() {
        return eventsRoller.get();
    }

    public boolean getEventsSensorReport() {
        return eventsSensorReport.get();
    }

    public String getDeviceApiUrl() {
        return urls.get().deviceApi;
    }

    public String getEventCallbackUrl() {
        return urls.get().eventCallback;
    }

    public String getWebSocketCallback() {
        return urls.get().websocketCallback;
    }

    public boolean getEnableBluGateway() {
        return enableBluGateway.get();
    }

    public boolean getEnableRangeExtender() {
        return enableRangeExtender.get();
    }

    public String getLocalIp() {
        return localIp;
    }

    public boolean getEnableCoIOT() {
        return enableCoIOT.get();
    }

    public void setEnableCoIOT(boolean value) {
        enableCoIOT.set(value);
    }

    public String getRealm() {
        return realm.get();
    }

    public void setRealm(String value) {
        realm.set(value);
    }

    /**
     * Attempt to resolve the device host name into an IP address.
     *
     * @return {@code true} if the IP address is resolved, {@code false} if it isn't.
     */
    public boolean resolveIp() {
        InetSocketAddress socketAddr = deviceSocketAddr.get();
        if (socketAddr != null) {
            // Already resolved, just return
            return true;
        }
        socketAddr = resolveSocketAddr(this.deviceHostAddress.get());
        if (socketAddr == null) {
            logger.debug("{}: Failed to resolve hostname '{}'", realm.get(), deviceHostAddress.get());
            return false;
        }
        deviceSocketAddr.set(socketAddr);
        return true;
    }

    /**
     * Refreshes all fields derived from the thing configuration so that changes made via
     * {@code handleConfigurationUpdate()} (new credentials, device address, event flags, etc.)
     * take effect on the next re-initialization cycle without rebuilding the handler or API client.
     * Thread-safe: every mutable field uses an atomic type.
     *
     * @param thingConfig refreshed thing configuration
     * @param bindingConfig current binding runtime configuration
     * @param gen2 true for Gen2+ devices
     */
    public void updateFromThingConfig(ShellyThingConfiguration thingConfig, ShellyBindingRuntimeConfig bindingConfig,
            boolean gen2) {
        credentials.set(new ShellyAuthCredentials(gen2 ? SHELLY2_DEFAULT_USERID : bindingConfig.getDefaultUserId(),
                bindingConfig.getDefaultPassword(), thingConfig.getUserId(), thingConfig.getPassword()));

        eventsButton.set(thingConfig.getEventsButton());
        eventsSwitch.set(thingConfig.getEventsSwitch());
        eventsPush.set(thingConfig.getEventsPush());
        eventsRoller.set(thingConfig.getEventsRoller());
        eventsSensorReport.set(thingConfig.getEventsSensorReport());
        enableCoIOT.set(!gen2 && thingConfig.getEventsCoIoT());
        enableBluGateway.set(thingConfig.getEnableBluGateway());
        enableRangeExtender.set(thingConfig.getEnableRangeExtender());

        String bdAddr = thingConfig.getDeviceAddress();
        if (!bdAddr.isBlank()) {
            deviceBdAddr.set(bdAddr.toLowerCase(Locale.ROOT).replace(":", ""));
            deviceHostAddress.set("");
            deviceSocketAddr.set(null);
        } else {
            deviceBdAddr.set(null);
            String newHost = thingConfig.getDeviceIp();
            if (!newHost.equals(deviceHostAddress.get())) {
                deviceHostAddress.set(newHost);
                deviceSocketAddr.set(null); // force re-resolve on next resolveIp() call
                urls.set(new ShellyApiUrls(localIp, localPort, newHost));
            }
        }
    }

    @Override
    public String toString() {
        ShellyAuthCredentials cred = this.credentials.get();
        String bdAddr = deviceBdAddr.get();
        return (bdAddr != null ? "Bluetooth device address=" + bdAddr + ", " : "") + "HTTP user/password=" + getUserId()
                + "/" + (cred.password.isEmpty() ? "<none>" : "***") + "\n" + "Events: Button: " + eventsButton.get()
                + ", Switch (on/off): " + eventsSwitch.get() + ", Push: " + eventsPush.get() + ", Roller: "
                + eventsRoller.get() + ", Sensor: " + eventsSensorReport.get() + ", CoIoT: " + enableCoIOT.get() + "\n"
                + "Blu Gateway=" + enableBluGateway.get() + ", Range Extender: " + enableRangeExtender.get();
    }
}
