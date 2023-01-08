/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.siemenshvac.internal;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.openhab.binding.siemenshvac.handler.SiemensHvacHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueBridgeServiceTracker} tracks for hue lights which are connected
 * to a paired hue bridge. The default search time for hue is 60 seconds.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - changed search timeout, changed discovery result creation to support generic thing types
 * @author Thomas Höfer - Added representation
 */
public class siemensHvacDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(siemensHvacDiscoveryService.class);

    private final static int SEARCH_TIME = 60;
    private final static String MODEL_ID = "modelId";

    // @formatter:off

    private SiemensHvacHandler hvacHandler;

    public siemensHvacDiscoveryService(SiemensHvacHandler hvacHandler) {
        super(SEARCH_TIME);
        this.hvacHandler = hvacHandler;
    }

    public void activate() {
        //hvacHandler.registerLightStatusListener(this);
    }

    @Override
    public void deactivate() {
        //removeOlderResults(new Date().getTime());
        //hueBridgeHandler.unregisterLightStatusListener(this);
    }

    /*
    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return HueLightHandler.SUPPORTED_THING_TYPES;
    }
*/

    @Override
    public void startScan() {
        /*
        List<FullLight> lights = hueBridgeHandler.getFullLights();
        if (lights != null) {
            for (FullLight l : lights) {
                onLightAddedInternal(l);
            }
        }
        // search for unpaired lights
        hueBridgeHandler.startSearch();
        */
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

/*
    private void onLightAddedInternal(FullLight light) {
        ThingUID thingUID = getThingUID(light);
        ThingTypeUID thingTypeUID = getThingTypeUID(light);

        String modelId = light.getModelID().replaceAll(HueLightHandler.NORMALIZE_ID_REGEX, "_");

        if (thingUID != null && thingTypeUID != null) {
            ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>(1);
            properties.put(LIGHT_ID, light.getId());
            properties.put(MODEL_ID, modelId);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(light.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported light of type '{}' and model '{}' with id {}", light.getType(),
                    modelId, light.getId());
        }
    }


    @Override
    public void onLightRemoved(HueBridge bridge, FullLight light) {
        ThingUID thingUID = getThingUID(light);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    public void onLightStateChanged(HueBridge bridge, FullLight light) {
        // nothing to do
    }

    private ThingUID getThingUID(FullLight light) {
        ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = getThingTypeUID(light);

        if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, light.getId());
        } else {
            return null;
        }
    }

    private ThingTypeUID getThingTypeUID(FullLight light) {
        String thingTypeId = TYPE_TO_ZIGBEE_ID_MAP
                .get(light.getType().replaceAll(HueLightHandler.NORMALIZE_ID_REGEX, "_").toLowerCase());
        return thingTypeId != null ? new ThingTypeUID(BINDING_ID, thingTypeId) : null;
    }

    */
}
