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

    public static class ShellyAuthCredentials {
        private final String userId;
        private final String password;
        private final String bearer;

        public ShellyAuthCredentials(String defaultUserId, String defaultPassword, String userId, String password) {
            this.userId = userId.isBlank() ? defaultUserId : userId;
            this.password = password.isBlank() ? defaultPassword : password;
            this.bearer = this.userId + ":" + this.password;
        }
    }

    public static class ShellyApiUrls {
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
    private final String deviceHostname;

    /** Device IP with or without port or {@code null} */
    private final AtomicReference<@Nullable InetSocketAddress> deviceSocketAddr = new AtomicReference<>();

    /** Bluetooth device address, a unique 48-bit identifier, similar to a MAC address */
    private final @Nullable String deviceBdAddr;
    private final AtomicReference<ShellyAuthCredentials> credentials = new AtomicReference<>(
            new ShellyAuthCredentials("", "", "", "")); // auth credentials
    private final ShellyApiUrls urls;

    private final boolean eventsButton; // true: register for Relay btn_xxx events
    private final boolean eventsSwitch; // true: register for device out_xxx events
    private final boolean eventsPush; // true: register for short/long push events
    private final boolean eventsRoller; // true: register for short/long push events
    private final boolean eventsSensorReport; // true: register for sensor events

    // Gen2
    private final boolean enableBluGateway;
    private final boolean enableRangeExtender;

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
            this.deviceBdAddr = bdAddr.toLowerCase(Locale.ROOT).replace(":", "");
            this.deviceHostname = "";
        } else {
            this.deviceBdAddr = null;
            this.deviceHostname = thingConfig.getDeviceIp();
            if (resolveHostname) {
                this.deviceSocketAddr.set(resolveSocketAddr(this.deviceHostname));
            }
        }

        credentials.set(new ShellyAuthCredentials(gen2 ? SHELLY2_DEFAULT_USERID : bindingConfig.getDefaultUserId(),
                bindingConfig.getDefaultPassword(), thingConfig.getUserId(), thingConfig.getPassword()));

        enableBluGateway = thingConfig.getEnableBluGateway();
        enableRangeExtender = thingConfig.getEnableRangeExtender();

        enableCoIOT.set(gen2 ? false : thingConfig.getEventsCoIoT());
        eventsButton = thingConfig.getEventsButton();
        eventsSwitch = thingConfig.getEventsSwitch();
        eventsPush = thingConfig.getEventsPush();
        eventsRoller = thingConfig.getEventsRoller();
        eventsSensorReport = thingConfig.getEventsSensorReport();

        urls = new ShellyApiUrls(localIp, localPort, deviceHostname);
    }

    /**
     * Constructor for Discovery Mode
     *
     * @param bindingConfig Binding configuration
     * @param realm Realm, which is used for authentication (usually hostname / mDNS service name)
     * @param hostname Device host name
     */
    public ShellyApiConfiguration(ShellyBindingRuntimeConfig bindingConfig, String realm, String hostname) {
        this.realm.set(realm); // mDNS service name or hostname provided by /shelly
        this.deviceHostname = hostname;
        this.deviceSocketAddr.set(resolveSocketAddr(hostname));
        this.deviceBdAddr = null; // TODO: (Nad) Is the bdAddr used for discovery?

        localIp = bindingConfig.getLocalIP();
        localPort = String.valueOf(bindingConfig.getHttpPort());
        credentials.set(new ShellyAuthCredentials(bindingConfig.getDefaultUserId(), bindingConfig.getDefaultPassword(),
                "", ""));

        // Disable all features, which are not required in discoverymode
        enableCoIOT.set(false);
        eventsButton = eventsSwitch = eventsPush = eventsRoller = eventsSensorReport = false;
        enableBluGateway = enableRangeExtender = false;

        urls = new ShellyApiUrls(localIp, localPort, hostname);
    }

    private @Nullable InetSocketAddress resolveSocketAddr(String hostname) {
        InetSocketAddress result = null;
        if (!hostname.isBlank()) {
            try {
                String ip = hostname.contains(":") ? substringBefore(hostname, ":") : hostname;
                String port = hostname.contains(":") ? substringAfter(hostname, ":") : "";
                InetAddress addr = InetAddress.getByName(ip);
                int portNum = 0;
                if (!ip.isBlank()) {
                    try {
                        portNum = Integer.parseInt(port);
                    } catch (NumberFormatException e) {
                        logger.warn("{}: Invalid port number '{}' - ignoring", realm.get(), port);
                    }
                }
                result = new InetSocketAddress(addr, portNum);
                String resultAddr = addr.getHostAddress();
                if (!ip.equals(resultAddr) && logger.isDebugEnabled()) {
                    logger.debug("{}: hostname {} resolved to IP address {}", realm.get(), hostname, resultAddr + (port.isEmpty() ? "" : ":" + port));
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
    public String getDeviceHostname() {
        return deviceHostname;
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
     * @return The Bluetooth device addres or {@code null}.
     */
    public @Nullable String getBdAddr() {
        return deviceBdAddr;
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
        return eventsButton;
    }

    public boolean getEventsSwitch() {
        return eventsSwitch;
    }

    public boolean getEventsPush() {
        return eventsPush;
    }

    public boolean getEventsRoller() {
        return eventsRoller;
    }

    public boolean getEventsSensorReport() {
        return eventsSensorReport;
    }

    public String getDeviceApiUrl() {
        return urls.deviceApi;
    }

    public String getEventCallbackUrl() {
        return urls.eventCallback;
    }

    public String getWebSocketCallback() {
        return urls.websocketCallback;
    }

    public boolean getEnableBluGateway() {
        return enableBluGateway;
    }

    public boolean getEnableRangeExtender() {
        return enableRangeExtender;
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
        socketAddr = resolveSocketAddr(this.deviceHostname);
        if (socketAddr == null) {
            logger.debug("{}: Failed to resolve hostname '{}'", realm.get(), deviceHostname);
            return false;
        }
        deviceSocketAddr.set(socketAddr);
        return true;
    }

    @Override
    public String toString() {
        ShellyAuthCredentials credentials = this.credentials.get();
        return (deviceBdAddr != null ? "Bluetooth device address=" + deviceBdAddr + ", ": "") + "HTTP user/password=" + getUserId() + "/"
                + (credentials.password.isEmpty() ? "<none>" : "***") + "\n" + "Events: Button: " + eventsButton
                + ", Switch (on/off): " + eventsSwitch + ", Push: " + eventsPush + ", Roller: " + eventsRoller
                + ", Sensor: " + eventsSensorReport + ", CoIoT: " + enableCoIOT.get() + "\n" + "Blu Gateway="
                + enableBluGateway + ", Range Extender: " + enableRangeExtender;
    }
}
