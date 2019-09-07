/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.sinope.SinopeBindingConstants;
import org.openhab.binding.sinope.handler.SinopeGatewayHandler;
import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractDiscoveryService} provides methods which handle the {@link DiscoveryListener}s.
 *
 * Subclasses do not have to care about adding and removing those listeners.
 * They can use the protected methods {@link #thingDiscovered(DiscoveryResult)} and {@link #thingRemoved(String)} in
 * order to notify the registered {@link DiscoveryListener}s.
 *
 * @author Pascal Larin - Initial contribution
 *
 */
public class SinopeThingsDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SinopeThingsDiscoveryService.class);

    private final static int SEARCH_TIME = 120;

    private SinopeGatewayHandler sinopeGatewayHandler;

    public SinopeThingsDiscoveryService(SinopeGatewayHandler sinopeGatewayHandler) {
        super(SinopeBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        this.sinopeGatewayHandler = sinopeGatewayHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SinopeBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {

        logger.debug("Sinope Things starting scan");
        try {
            sinopeGatewayHandler.startSearch(this);
        } catch (IOException e) {
            logger.debug("Search failed with an exception", e);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
        try {
            sinopeGatewayHandler.stopSearch();
        } catch (IOException e) {
            logger.debug("Can't stop search with an exception", e);
        } finally {
            logger.debug("Sinope Things scan stopped");
        }
    }

    public void newThermostat(byte[] deviceId) {
        logger.debug("Sinope Things service discovered a new device with id: {}", ByteUtil.toString(deviceId));
        ThingTypeUID thingTypeUID = SinopeBindingConstants.THING_TYPE_THERMO;
        ThingUID bridgeUID = sinopeGatewayHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, toUID(deviceId));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withBridge(bridgeUID).withLabel("Device-Sinope")
                .withProperty(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID, ByteUtil.toString(deviceId)).build();

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
