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
package org.openhab.binding.rfxcom.internal.discovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.rfxcom.internal.RFXComBindingConstants;
import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jd2xx.JD2XX;

/**
 * The {@link RFXComBridgeDiscovery} is responsible for discovering new RFXCOM
 * transceivers.
 *
 * @author Pauli Anttila - Initial contribution
 *
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.rfxcom")
public class RFXComBridgeDiscovery extends AbstractDiscoveryService {
    private static final long REFRESH_INTERVAL_IN_SECONDS = 600;

    private final Logger logger = LoggerFactory.getLogger(RFXComBridgeDiscovery.class);

    private boolean unsatisfiedLinkErrorLogged = false;

    private ScheduledFuture<?> discoveryJob;

    @Activate
    public RFXComBridgeDiscovery(@Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider) {
        super(RFXComBindingConstants.DISCOVERABLE_BRIDGE_THING_TYPES_UIDS, 10, false);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return RFXComBindingConstants.DISCOVERABLE_BRIDGE_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        logger.debug("Start discovery scan for RFXCOM transceivers");
        discoverRfxcom();
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start background discovery for RFXCOM transceivers");
        discoveryJob = scheduler.scheduleWithFixedDelay(this::discoverRfxcom, 0, REFRESH_INTERVAL_IN_SECONDS,
                TimeUnit.SECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop background discovery for RFXCOM transceivers");
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    private synchronized void discoverRfxcom() {
        try {
            JD2XX jd2xx = new JD2XX();
            logger.debug("Discovering RFXCOM transceiver devices by JD2XX version {}", jd2xx.getLibraryVersion());
            String[] devDescriptions = (String[]) jd2xx.listDevicesByDescription();
            String[] devSerialNumbers = (String[]) jd2xx.listDevicesBySerialNumber();
            logger.debug("Discovered {} FTDI device(s)", devDescriptions.length);

            for (int i = 0; i < devSerialNumbers.length; ++i) {
                if (devDescriptions.length > 0) {
                    switch (devDescriptions[i]) {
                        case RFXComBindingConstants.BRIDGE_TYPE_RFXTRX433:
                            addBridge(RFXComBindingConstants.BRIDGE_RFXTRX443, devSerialNumbers[i]);
                            break;
                        case RFXComBindingConstants.BRIDGE_TYPE_RFXTRX315:
                            addBridge(RFXComBindingConstants.BRIDGE_RFXTRX315, devSerialNumbers[i]);
                            break;
                        case RFXComBindingConstants.BRIDGE_TYPE_RFXREC433:
                            addBridge(RFXComBindingConstants.BRIDGE_RFXREC443, devSerialNumbers[i]);
                            break;
                        default:
                            logger.trace("Ignore unknown device '{}'", devDescriptions[i]);
                    }
                }
            }

            logger.debug("Discovery done");
        } catch (IOException e) {
            logger.error("Error occurred during discovery", e);
        } catch (UnsatisfiedLinkError e) {
            if (unsatisfiedLinkErrorLogged) {
                logger.debug(
                        "Error occurred when trying to load native library for OS '{}' version '{}', processor '{}'",
                        System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"),
                        e);
            } else {
                logger.error(
                        "Error occurred when trying to load native library for OS '{}' version '{}', processor '{}'",
                        System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"),
                        e);
                unsatisfiedLinkErrorLogged = true;
            }
        }
    }

    private void addBridge(ThingTypeUID bridgeType, String bridgeId) {
        logger.debug("Discovered RFXCOM transceiver, bridgeType='{}', bridgeId='{}'", bridgeType, bridgeId);

        Map<String, Object> properties = new HashMap<>();
        properties.put(RFXComBridgeConfiguration.BRIDGE_ID, bridgeId);

        ThingUID uid = new ThingUID(bridgeType, bridgeId);
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel("@text/discovery.bridge.label").build();
        thingDiscovered(result);
    }
}
