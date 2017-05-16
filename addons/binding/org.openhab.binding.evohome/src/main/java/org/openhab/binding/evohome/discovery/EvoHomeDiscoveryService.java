/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.discovery;

import static org.openhab.binding.evohome.EvoHomeBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvoHomeDiscoveryService} class is capable of discovering the available data from Evohome
 *
 * @author Neil Renaud - Initial contribution
 */
public class EvoHomeDiscoveryService extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(EvoHomeDiscoveryService.class);
    private static final int SEARCH_TIME = 2;
    private EvoHomeBridgeHandler toonBridgeHandler;

    public EvoHomeDiscoveryService(EvoHomeBridgeHandler evoHomeBridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.evoHomeBridgeHandler = toonBridgeHandler;
    }

    @Override
    public void startScan() {
        logger.debug("Evohome start scan");
        if (evoHomeBridgeHandler != null){
            try {
                EvoHomeData evoHomeData = evoHomeBridgeHandler.refreshData();
                discoverWeather(evoHomeData);
                discoverRadiatorValves(evoHomeData);
            } catch (Exception e) {
                logger.warn("{}", e.getMessage(), e);
            }
        }
        stopScan();
    }

    private void discoverWeather(EvoHomeData evoHomeData)
            throws IllegalArgumentException {
        for (EvoHomeWeather evoHomeWeather : evoHomeData.getWeather()) {

			//TODO implement this...
//			ThingUID thingUID = findThingUID(MAIN_THING_TYPE.getId(), agreement.getAgreementId());

//            Map<String, Object> properties = new HashMap<>();

//            properties.put(PROPERTY_AGREEMENT_ID, agreement.getAgreementId());
//            properties.put(PROPERTY_COMMON_NAME, agreement.getDisplayCommonName());
//            properties.put(PROPERTY_ADDRESS,
//                    String.format("%s %s, %s", agreement.getStreet(), agreement.getHouseNumber(), agreement.getCity()));

//            String name = String.format("Toon display @ %s %s", agreement.getStreet(), agreement.getHouseNumber());

//            addDiscoveredThing(thingUID, properties, name);

            // only the first agreement is handled at the moment
//            return;
        }
    }

    private void discoverRadiatorValves(EvoHomeData evoHomeData) throws IllegalArgumentException, ToonConnectionException {
        if (evoHomeData == null) {
            return;
        }
		//TODO implement this
//        for (EvoHomeDeviceConfig device : evoHomeData.getDevice()) {
//		
//            ThingUID thingUID = findThingUID(PLUG_THING_TYPE.getId(), device.getDevUUID());
//            Map<String, Object> properties = new HashMap<>();
//            properties.put(PROPERTY_DEV_TYPE, device.getDevType());
//            properties.put(PROPERTY_DEV_UUID, device.getDevUUID());

//            String name = device.getName();
//            logger.debug("found plug name:{} type:{} uuid:{}", name, device.getDevType(), device.getDevUUID());

//            addDiscoveredThing(thingUID, properties, name);
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
