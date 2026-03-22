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

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyThingConfiguration extends ShellyThingBasicConfig {
    protected final Logger logger = LoggerFactory.getLogger(ShellyThingConfiguration.class);

    // All access must be guarded by "this"
    private String realm;

    private final String localIp; // local ip addresses used to create callback url
    private final String localPort;

    public ShellyThingConfiguration(String thingName, ShellyThingBasicConfig basicConfig,
            ShellyBindingConfiguration bindingConfig, String realm, boolean gen2) {
        for (Field field : ShellyThingBasicConfig.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                field.set(this, field.get(basicConfig));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to copy field: " + field.getName(), e);
            }
        }

        if (deviceAddress.isEmpty()) {
            if (!deviceIp.isEmpty()) {
                try {
                    String ip = deviceIp.contains(":") ? substringBefore(deviceIp, ":") : deviceIp;
                    String port = deviceIp.contains(":") ? substringAfter(deviceIp, ":") : "";
                    InetAddress addr = InetAddress.getByName(ip);
                    String saddr = addr.getHostAddress();
                    if (!ip.equals(saddr)) {
                        logger.debug("{}: hostname {} resolved to IP address {}", thingName, deviceIp, saddr);
                        deviceIp = saddr + (port.isEmpty() ? "" : ":" + port);
                    }
                } catch (UnknownHostException e) {
                    logger.debug("{}: Unable to resolve hostname {}", thingName, deviceIp);
                }
            }

            deviceAddress = deviceIp;
        } else {
            // remove : from MAC address and convert to lower case
            deviceAddress = deviceAddress.toLowerCase(Locale.ROOT).replace(":", "");
        }

        if (!gen2 && userId.isEmpty() && !bindingConfig.defaultUserId.isEmpty()) {
            // Gen2 has hard coded user "admin"
            userId = bindingConfig.defaultUserId;
            logger.debug("{}: Using default user id '{}' from binding configuration", thingName, userId);
        }
        if (password.isEmpty() && !bindingConfig.defaultPassword.isEmpty()) {
            password = bindingConfig.defaultPassword;
            logger.debug("{}: Using default password from binding configuration", thingName);
        }

        if (updateInterval == 0) {
            updateInterval = UPDATE_STATUS_INTERVAL_SECONDS * UPDATE_SKIP_COUNT;
        }
        if (updateInterval < UPDATE_MIN_DELAY) {
            updateInterval = UPDATE_MIN_DELAY;
        }

        if (gen2) {
            eventsCoIoT = false;
        }
        if (eventsCoIoT) {

        }

        this.localIp = bindingConfig.localIP;
        this.localPort = String.valueOf(bindingConfig.httpPort != -1 ? bindingConfig.httpPort : DEFAULT_LOCAL_PORT);
        this.realm = getString(realm);
    }

    public ShellyThingConfiguration(ShellyBindingConfiguration bindingConfig, String realm, String deviceIp) {
        this.realm = realm; // mDNS service name or hostname provided by /shelly
        this.deviceIp = deviceIp;
        this.userId = getString(bindingConfig.defaultUserId);
        this.password = getString(bindingConfig.defaultPassword);
        this.localIp = getString(bindingConfig.localIP);
        this.localPort = String.valueOf(bindingConfig.httpPort != -1 ? bindingConfig.httpPort : DEFAULT_LOCAL_PORT);
    }

    public synchronized void disableGen1Events() {
        eventsCoIoT = true;
        eventsSwitch = false;
        eventsButton = false;
        eventsPush = false;
        eventsRoller = false;
        eventsSensorReport = false;
    }

    public synchronized String getLocalIp() {
        return localIp;
    }

    public synchronized String getLocalPort() {
        return localPort;
    }

    public synchronized String getRealm() {
        return realm;
    }

    public synchronized void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public String toString() {
        return "Device address=" + deviceAddress + ", HTTP user/password=" + userId + "/"
                + (password.isEmpty() ? "<none>" : "***") + ", update interval=" + updateInterval + "\n"
                + "Events: Button: " + eventsButton + ", Switch (on/off): " + eventsSwitch + ", Push: " + eventsPush
                + ", Roller: " + eventsRoller + "Sensor: " + eventsSensorReport + ", CoIoT: " + eventsCoIoT + "\n"
                + "Blu Gateway=" + enableBluGateway + ", Range Extender: " + enableRangeExtender;
    }
}
