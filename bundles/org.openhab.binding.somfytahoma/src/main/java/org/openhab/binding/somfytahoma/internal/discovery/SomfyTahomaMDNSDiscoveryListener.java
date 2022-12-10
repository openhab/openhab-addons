/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal.discovery;

import java.util.Enumeration;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.somfytahoma.internal.handler.SomfyTahomaBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaMDNSDiscoveryListener} represents a mDNS listener
 * for a mDNS discovery.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaMDNSDiscoveryListener implements ServiceListener {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaMDNSDiscoveryListener.class);
    private final SomfyTahomaBridgeHandler handler;

    public SomfyTahomaMDNSDiscoveryListener(SomfyTahomaBridgeHandler handler) {
        this.handler = handler;
    }

    @Override
    public void serviceAdded(@Nullable ServiceEvent event) {
        if (event != null) {
            logger.trace("Service added: {}", event.getInfo());
        }
    }

    @Override
    public void serviceRemoved(@Nullable ServiceEvent event) {
        if (event != null) {
            logger.trace("Service removed: {}", event.getInfo());
        }
    }

    @Override
    public void serviceResolved(@Nullable ServiceEvent event) {
        if (event == null || event.getInfo() == null) {
            logger.debug("Null event received");
            return;
        }

        ServiceInfo info = event.getInfo();
        logger.trace("Service resolved: {}", info);
        if (info.getInet4Addresses().length > 0) {
            logger.debug("Server address: {}", info.getInet4Addresses()[0].getHostAddress());
            handler.setGatewayIPAddress(info.getInet4Addresses()[0].getHostAddress());
        }
        Enumeration<String> e = info.getPropertyNames();
        if (e != null) {
            while (e.hasMoreElements()) {
                String name = e.nextElement();
                if ("gateway_pin".equals(name)) {
                    String pin = info.getPropertyString(name);
                    logger.debug("Gateway PIN: {}", pin);
                    handler.setGatewayPin(pin);
                    handler.updateConfiguration();
                    break;
                }
            }
        }
    }
}
