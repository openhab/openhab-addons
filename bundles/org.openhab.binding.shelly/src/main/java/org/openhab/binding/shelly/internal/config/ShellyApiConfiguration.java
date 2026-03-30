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

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
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
        public final String userId;
        public final String password;
        public final String bearer;

        public ShellyAuthCredentials(String defaultUserId, String defaultPassword, String userId, String password) {
            this.userId = userId.isBlank() ? defaultPassword : userId;
            this.password = password.isBlank() ? defaultPassword : password;
            this.bearer = this.userId + ":" + this.password;
        }
    }

    public class ShellyApiUrls {
        public final String deviceApi;
        public final String websocketCallback;
        public final String eventCallback;

        public ShellyApiUrls(String localIp, String localPort, String deviceIp) {
            deviceApi = "http://" + deviceIp;
            websocketCallback = "ws://" + localIp + ":" + localPort + "/shelly/wsevent";
            eventCallback = "http://" + localIp + ":" + localPort + SHELLY1_CALLBACK_URI + "/";
        }
    }

    public final String deviceIp; // device ip address
    public final String deviceAddress; // resolved IP address or MAC adress for BLU devices
    public final AtomicReference<ShellyAuthCredentials> credentials = new AtomicReference<>(); // auth credentials
    public final ShellyApiUrls urls;

    public final boolean eventsButton; // true: register for Relay btn_xxx events
    public final boolean eventsSwitch; // true: register for device out_xxx events
    public final boolean eventsPush; // true: register for short/long push events
    public final boolean eventsRoller; // true: register for short/long push events
    public final boolean eventsSensorReport; // true: register for sensor events

    // Gen2
    public final boolean enableBluGateway;
    public final boolean enableRangeExtender;

    public final String localIp; // local ip addresses used to create callback url
    public final String localPort; // local port, used by callbacks through servlet

    /*
     * Those values are updated after device settings has been read
     */

    public final AtomicReference<String> realm = new AtomicReference<>();
    public final AtomicBoolean enableCoIOT = new AtomicBoolean(); // true: CoIoT/COAP enabled, event urls disabled

    /**
     * Constructor for Thing handler
     *
     * @param thingName Thing name used for logging
     * @param thingConfig OH Thing configuration
     * @param bindingConfig Binding configuration
     * @param realm Realm, which is used for authentication (usually hostname / mDNS service name)
     * @param gen2 True for Generation 2 or newer devices
     */
    public ShellyApiConfiguration(ShellyThingConfiguration thingConfig, ShellyBindingConfiguration bindingConfig,
            String realm, boolean gen2) {

        this.localIp = bindingConfig.localIP;
        this.localPort = String.valueOf(bindingConfig.httpPort != -1 ? bindingConfig.httpPort : DEFAULT_LOCAL_PORT);
        this.realm.set(realm);

        // deviceIP can be, an IP address, IP address:port or a FQDN, which needs to be resolved
        // deviceAddress is the MAC address for BLU device or the resolved IP address
        if (!thingConfig.getDeviceAddress().isEmpty()) {
            // BLU: remove : from MAC address and convert to lower case
            deviceAddress = thingConfig.getDeviceAddress().toLowerCase(Locale.ROOT).replace(":", "");
            deviceIp = "";
        } else {
            deviceIp = deviceAddress = resolveIp(thingConfig.getDeviceIp());
        }

        credentials.set(new ShellyAuthCredentials(gen2 ? "admin" : bindingConfig.defaultUserId,
                bindingConfig.defaultPassword, thingConfig.getUserId(), thingConfig.getPassword()));

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
    public ShellyApiConfiguration(ShellyBindingConfiguration bindingConfig, String realm, String deviceIp) {
        this.realm.set(realm); // mDNS service name or hostname provided by /shelly
        this.deviceIp = deviceIp;
        this.deviceAddress = resolveIp(deviceIp);

        localIp = getString(bindingConfig.localIP);
        localPort = String.valueOf(bindingConfig.httpPort != -1 ? bindingConfig.httpPort : DEFAULT_LOCAL_PORT);
        credentials.set(new ShellyAuthCredentials(getString(bindingConfig.defaultUserId),
                getString(bindingConfig.defaultPassword), "", ""));

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
                        logger.debug("{}: hostname {} resolved to IP address {}", realm, deviceIp, resolvedIp);
                    }
                }
            } catch (UnknownHostException e) {
                logger.debug("{}: Unable to resolve hostname {}", realm, deviceIp);
            }
        }
        if (resolvedIp.isBlank()) {
            logger.debug("{}: Device IP is missing or invalid", realm);
        }
        return resolvedIp;
    }

    @Override
    public String toString() {
        ShellyAuthCredentials credentials = this.credentials.get();
        return "Device address=" + deviceAddress + ", HTTP user/password=" + credentials.userId + "/"
                + (credentials.password.isEmpty() ? "<none>" : "***") + "\n" + "Events: Button: " + eventsButton
                + ", Switch (on/off): " + eventsSwitch + ", Push: " + eventsPush + ", Roller: " + eventsRoller
                + "Sensor: " + eventsSensorReport + ", CoIoT: " + enableCoIOT.get() + "\n" + "Blu Gateway="
                + enableBluGateway + ", Range Extender: " + enableRangeExtender;
    }
}
