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
package org.openhab.binding.sinope.internal.discovery;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sinope.SinopeBindingConstants;
import org.openhab.binding.sinope.handler.SinopeGatewayHandler;
import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SinopeThingsDiscoveryService} listens for Sinopé devices reporting to a Sinopé Gateway
 * during a manual discovery scan.
 *
 * @author Pascal Larin - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = SinopeThingsDiscoveryService.class)
@NonNullByDefault
public class SinopeThingsDiscoveryService extends AbstractThingHandlerDiscoveryService<SinopeGatewayHandler> {

    private final Logger logger = LoggerFactory.getLogger(SinopeThingsDiscoveryService.class);

    private static final int SEARCH_TIME = 120;

    public SinopeThingsDiscoveryService() {
        super(SinopeGatewayHandler.class, SinopeBindingConstants.SUPPORTED_DISCOVERY_THING_TYPES_UIDS, SEARCH_TIME,
                false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SinopeBindingConstants.SUPPORTED_DISCOVERY_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        logger.debug("Sinope Things starting scan");
        try {
            thingHandler.startSearch(this::newThermostat);
        } catch (IOException e) {
            logger.debug("Search failed with an exception", e);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
        thingHandler.stopSearch();
        logger.debug("Sinope Things scan stopped");
    }

    public void newThermostat(byte[] deviceId) {
        if (deviceId.length == 0) {
            return;
        }
        String formattedId = ByteUtil.toString(deviceId).trim();
        String uid = toUID(deviceId);
        logger.debug("Sinope Things service discovered a new device with id: {}", formattedId);
        ThingTypeUID thingTypeUID = SinopeBindingConstants.THING_TYPE_THERMO;
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, uid);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withBridge(bridgeUID).withLabel("Sinopé Thermostat " + uid)
                .withProperty(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID, formattedId)
                .withRepresentationProperty(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID).build();

        thingDiscovered(discoveryResult);
    }

    private static String toUID(byte[] deviceId) {
        StringBuilder sb = new StringBuilder();
        for (byte b : deviceId) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
