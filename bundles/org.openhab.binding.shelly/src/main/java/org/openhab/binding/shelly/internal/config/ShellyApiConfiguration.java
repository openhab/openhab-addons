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
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    private final String deviceIp; // device ip address
    private final String deviceAddress; // resolved IP address or MAC address for BLU devices
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
        if (!thingConfig.getDeviceAddress().isEmpty()) {
            // BLU: remove : from MAC address and convert to lower case
            deviceAddress = thingConfig.getDeviceAddress().toLowerCase(Locale.ROOT).replace(":", "");
            deviceIp = "";
        } else {
            String raw = thingConfig.getDeviceIp();
            deviceIp = deviceAddress = resolveHostname ? resolveIp(raw) : raw;
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

        urls = new ShellyApiUrls(localIp, localPort, deviceIp);
    }

    /**
     * Constructor for Discovery Mode
     *
     * @param bindingConfig Binding configuration
     * @param realm Realm, which is used for authentication (usually hostname / mDNS service name)
     * @param deviceIp Device IP address
     */
    public ShellyApiConfiguration(ShellyBindingRuntimeConfig bindingConfig, String realm, String deviceIp) {
        this.realm.set(realm); // mDNS service name or hostname provided by /shelly
        this.deviceIp = deviceIp;
        this.deviceAddress = resolveIp(deviceIp);

        localIp = bindingConfig.getLocalIP();
        localPort = String.valueOf(bindingConfig.getHttpPort());
        credentials.set(new ShellyAuthCredentials(bindingConfig.getDefaultUserId(), bindingConfig.getDefaultPassword(),
                "", ""));

        // Disable all features, which are not required in discoverymode
        enableCoIOT.set(false);
        eventsButton = eventsSwitch = eventsPush = eventsRoller = eventsSensorReport = false;
        enableBluGateway = enableRangeExtender = false;

        urls = new ShellyApiUrls(localIp, localPort, deviceIp);
    }

    private String resolveIp(String deviceIp) {
        String resolvedIp = deviceIp;
        if (!resolvedIp.isEmpty()) {
            try {
                String ip = deviceIp.contains(":") ? substringBefore(deviceIp, ":") : deviceIp;
                String port = deviceIp.contains(":") ? substringAfter(deviceIp, ":") : "";
                InetAddress addr = InetAddress.getByName(ip);
                String saddr = addr.getHostAddress();
                if (!ip.equals(saddr)) {
                    resolvedIp = saddr + (port.isEmpty() ? "" : ":" + port);
                    if (logger.isDebugEnabled()) {
                        logger.debug("{}: hostname {} resolved to IP address {}", realm.get(), deviceIp, resolvedIp);
                    }
                }
            } catch (UnknownHostException e) {
                logger.debug("{}: Unable to resolve hostname {}", realm.get(), deviceIp);
            }
        }
        if (resolvedIp.isBlank()) {
            logger.debug("{}: Device IP is missing or invalid", realm.get());
        }
        return resolvedIp;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public String getDeviceAddress() {
        return deviceAddress;
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

    @Override
    public String toString() {
        ShellyAuthCredentials credentials = this.credentials.get();
        return "Device address=" + deviceAddress + ", HTTP user/password=" + getUserId() + "/"
                + (credentials.password.isEmpty() ? "<none>" : "***") + "\n" + "Events: Button: " + eventsButton
                + ", Switch (on/off): " + eventsSwitch + ", Push: " + eventsPush + ", Roller: " + eventsRoller
                + ", Sensor: " + eventsSensorReport + ", CoIoT: " + enableCoIOT.get() + "\n" + "Blu Gateway="
                + enableBluGateway + ", Range Extender: " + enableRangeExtender;
    }
}
