/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.toon.internal.discovery;

import static org.openhab.binding.toon.ToonBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.toon.handler.ToonBridgeHandler;
import org.openhab.binding.toon.internal.ToonApiClient;
import org.openhab.binding.toon.internal.api.Agreement;
import org.openhab.binding.toon.internal.api.DeviceConfig;
import org.openhab.binding.toon.internal.api.DeviceConfigInfo;
import org.openhab.binding.toon.internal.api.ToonConnectionException;
import org.openhab.binding.toon.internal.api.ToonState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ToonDiscoveryService} class is capable of discovering the available displays and plugs
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ToonDiscoveryService extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(ToonDiscoveryService.class);
    private static final int SEARCH_TIME = 2;
    private ToonBridgeHandler toonBridgeHandler;

    public ToonDiscoveryService(ToonBridgeHandler toonBridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.toonBridgeHandler = toonBridgeHandler;
    }

    @Override
    public void startScan() {
        logger.debug("Toon start scan");
        if (toonBridgeHandler != null && toonBridgeHandler.getApiClient() != null) {
            try {
                ToonApiClient api = toonBridgeHandler.getApiClient();
                api.logout();
                discoverAgreement(api.getAgreements());
                ToonState state = api.collect();
                discoverPlugs(state.getDeviceConfigInfo());
            } catch (Exception e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }
        stopScan();
    }

    private void discoverAgreement(List<Agreement> agreements)
            throws IllegalArgumentException, ToonConnectionException {
        for (Agreement agreement : agreements) {
            ThingUID thingUID = findThingUID(MAIN_THING_TYPE.getId(), agreement.getAgreementId());
            Map<String, Object> properties = new HashMap<>();

            properties.put(PROPERTY_AGREEMENT_ID, agreement.getAgreementId());
            properties.put(PROPERTY_COMMON_NAME, agreement.getDisplayCommonName());
            properties.put(PROPERTY_ADDRESS,
                    String.format("%s %s, %s", agreement.getStreet(), agreement.getHouseNumber(), agreement.getCity()));

            String name = String.format("Toon display @ %s %s", agreement.getStreet(), agreement.getHouseNumber());

            addDiscoveredThing(thingUID, properties, name);

            // only the first agreement is handled at the moment
            return;
        }
    }

    private void discoverPlugs(DeviceConfigInfo info) throws IllegalArgumentException, ToonConnectionException {
        if (info == null || info.getDevice() == null) {
            return;
        }
        for (DeviceConfig device : info.getDevice()) {
            ThingUID thingUID = findThingUID(PLUG_THING_TYPE.getId(), device.getDevUUID());
            Map<String, Object> properties = new HashMap<>();

            properties.put(PROPERTY_DEV_TYPE, device.getDevType());
            properties.put(PROPERTY_DEV_UUID, device.getDevUUID());

            String name = device.getName();
            logger.debug("found plug name:{} type:{} uuid:{}", name, device.getDevType(), device.getDevUUID());

            addDiscoveredThing(thingUID, properties, name);
        }
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(toonBridgeHandler.getThing().getUID()).withLabel(displayLabel).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {

                return new ThingUID(supportedThingTypeUID, toonBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered: " + thingType);
    }
}
