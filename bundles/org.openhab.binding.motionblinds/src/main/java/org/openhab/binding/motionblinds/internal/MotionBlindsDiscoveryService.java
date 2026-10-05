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
package org.openhab.binding.motionblinds.internal;

import static org.openhab.binding.motionblinds.internal.MotionBlindsBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.motionblinds.internal.MotionBlindsCommunicationManager.DiscoveryListener;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsMessage;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MotionBlindsDiscoveryService} discovers Wi-Fi motors by sending a multicast {@code GetDeviceList}
 * request. Every motor answers with a {@code GetDeviceListAck} carrying its MAC address and device type.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.motionblinds")
public class MotionBlindsDiscoveryService extends AbstractDiscoveryService implements DiscoveryListener {

    private static final int SCAN_TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(MotionBlindsDiscoveryService.class);
    private final MotionBlindsCommunicationManager communicationManager;

    @Activate
    public MotionBlindsDiscoveryService(final @Reference MotionBlindsCommunicationManager communicationManager,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider) {
        super(SUPPORTED_THING_TYPES_UIDS, SCAN_TIMEOUT_SECONDS, false);
        this.communicationManager = communicationManager;
        // needed to resolve the @text labels of the discovery results
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    @Deactivate
    protected void deactivate() {
        communicationManager.stopDiscovery(this);
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting discovery of Wi-Fi motors");
        communicationManager.startDiscovery(this);
        scheduler.schedule(() -> communicationManager.stopDiscovery(this), SCAN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    protected synchronized void stopScan() {
        communicationManager.stopDiscovery(this);
        super.stopScan();
    }

    @Override
    public void onDeviceFound(MotionBlindsMessage message, String sourceIp) {
        DiscoveryResult result = toDiscoveryResult(message, sourceIp);
        if (result != null) {
            thingDiscovered(result);
        }
    }

    static @Nullable DiscoveryResult toDiscoveryResult(MotionBlindsMessage message, String sourceIp) {
        String mac = message.mac;
        String deviceType = message.deviceType;
        if (mac == null || deviceType == null || !DEVICE_TYPES_WIFI.contains(deviceType)) {
            // hubs are not supported yet
            return null;
        }
        String normalizedMac = MotionBlindsCommunicationManager.normalizeMac(mac);
        DiscoveryResultBuilder builder = DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_WIFI_MOTOR, normalizedMac))
                // the IP address is only used for the label, the handler finds the motor by its MAC address
                .withProperty(CONFIG_MAC_ADDRESS, normalizedMac).withProperty(PROPERTY_DEVICE_TYPE, deviceType)
                .withRepresentationProperty(CONFIG_MAC_ADDRESS)
                .withLabel("@text/discovery.wifi-motor.label [ \"%s\" ]".formatted(sourceIp));
        String protocolVersion = message.protocolVersion;
        if (protocolVersion != null) {
            builder.withProperty(PROPERTY_PROTOCOL_VERSION, protocolVersion);
        }
        return builder.build();
    }
}
