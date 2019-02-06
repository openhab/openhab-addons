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
package org.openhab.binding.hue.internal.discovery;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hue.internal.FullHueObject;
import org.openhab.binding.hue.internal.FullLight;
import org.openhab.binding.hue.internal.FullSensor;
import org.openhab.binding.hue.internal.HueBridge;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.binding.hue.internal.handler.HueLightHandler;
import org.openhab.binding.hue.internal.handler.LightStatusListener;
import org.openhab.binding.hue.internal.handler.SensorStatusListener;
import org.openhab.binding.hue.internal.handler.sensors.DimmerSwitchHandler;
import org.openhab.binding.hue.internal.handler.sensors.LightLevelHandler;
import org.openhab.binding.hue.internal.handler.sensors.PresenceHandler;
import org.openhab.binding.hue.internal.handler.sensors.TapSwitchHandler;
import org.openhab.binding.hue.internal.handler.sensors.TemperatureHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueBridgeServiceTracker} tracks for hue lights which are connected
 * to a paired hue bridge. The default search time for hue is 60 seconds.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - changed search timeout, changed discovery result creation to support generic thing types;
 *         added representationProperty to discovery result
 * @author Thomas Höfer - Added representation
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 */
@NonNullByDefault
public class HueLightDiscoveryService extends AbstractDiscoveryService
        implements LightStatusListener, SensorStatusListener {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(HueLightHandler.SUPPORTED_THING_TYPES.stream(), DimmerSwitchHandler.SUPPORTED_THING_TYPES.stream(),
                    TapSwitchHandler.SUPPORTED_THING_TYPES.stream(), PresenceHandler.SUPPORTED_THING_TYPES.stream(),
                    TemperatureHandler.SUPPORTED_THING_TYPES.stream(), LightLevelHandler.SUPPORTED_THING_TYPES.stream())
            .flatMap(i -> i).collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(HueLightDiscoveryService.class);

    private static final int SEARCH_TIME = 10;

    // @formatter:off
    private static final Map<String, @Nullable String> TYPE_TO_ZIGBEE_ID_MAP = Stream.of(
            new SimpleEntry<>("on_off_light", "0000"),
            new SimpleEntry<>("on_off_plug_in_unit", "0010"),
            new SimpleEntry<>("dimmable_light", "0100"),
            new SimpleEntry<>("dimmable_plug_in_unit", "0110"),
            new SimpleEntry<>("color_light", "0200"),
            new SimpleEntry<>("extended_color_light", "0210"),
            new SimpleEntry<>("color_temperature_light", "0220"),
            new SimpleEntry<>("zllswitch", "0820"),
            new SimpleEntry<>("zgpswitch", "0830"),
            new SimpleEntry<>("zllpresence", "0107"),
            new SimpleEntry<>("zlltemperature", "0302"),
            new SimpleEntry<>("zlllightlevel", "0106")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
    // @formatter:on

    private final HueBridgeHandler hueBridgeHandler;

    public HueLightDiscoveryService(HueBridgeHandler hueBridgeHandler) {
        super(SEARCH_TIME);
        this.hueBridgeHandler = hueBridgeHandler;
    }

    public void activate() {
        hueBridgeHandler.registerLightStatusListener(this);
        hueBridgeHandler.registerSensorStatusListener(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime(), hueBridgeHandler.getThing().getUID());
        hueBridgeHandler.unregisterLightStatusListener(this);
        hueBridgeHandler.unregisterSensorStatusListener(this);

    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        List<FullLight> lights = hueBridgeHandler.getFullLights();
        for (FullLight l : lights) {
            onLightAddedInternal(l);
        }
        List<FullSensor> sensors = hueBridgeHandler.getFullSensors();
        for (FullSensor s : sensors) {
            onSensorAddedInternal(s);
        }
        // search for unpaired lights
        hueBridgeHandler.startSearch();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onLightAdded(@Nullable HueBridge bridge, FullLight light) {
        onLightAddedInternal(light);
    }

    private void onLightAddedInternal(FullLight light) {
        ThingUID thingUID = getThingUID(light);
        ThingTypeUID thingTypeUID = getThingTypeUID(light);

        String modelId = light.getNormalizedModelID();

        if (thingUID != null && thingTypeUID != null) {
            ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>();
            properties.put(LIGHT_ID, light.getId());
            if (modelId != null) {
                properties.put(Thing.PROPERTY_MODEL_ID, modelId);
            }
            String uniqueID = light.getUniqueID();
            if (uniqueID != null) {
                properties.put(UNIQUE_ID, uniqueID);
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(UNIQUE_ID)
                    .withLabel(light.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported light of type '{}' and model '{}' with id {}", light.getType(),
                    modelId, light.getId());
        }
    }

    @Override
    public void onLightGone(@Nullable HueBridge bridge, FullLight light) {
        onLightRemovedInternal(light);
    }

    @Override
    public void onLightRemoved(@Nullable HueBridge bridge, FullLight light) {
        onLightRemovedInternal(light);
    }

    private void onLightRemovedInternal(FullLight light) {
        ThingUID thingUID = getThingUID(light);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    public void onLightStateChanged(@Nullable HueBridge bridge, FullLight light) {
        // nothing to do
    }

    private @Nullable ThingUID getThingUID(FullHueObject hueObject) {
        ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = getThingTypeUID(hueObject);

        if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, hueObject.getId());
        } else {
            return null;
        }
    }

    private @Nullable ThingTypeUID getThingTypeUID(FullHueObject hueObject) {
        String thingTypeId = TYPE_TO_ZIGBEE_ID_MAP
                .get(hueObject.getType().replaceAll(NORMALIZE_ID_REGEX, "_").toLowerCase());

        return thingTypeId != null ? new ThingTypeUID(BINDING_ID, thingTypeId) : null;
    }

    @Override
    public void onSensorAdded(@Nullable HueBridge bridge, FullSensor sensor) {
        onSensorAddedInternal(sensor);
    }

    private void onSensorAddedInternal(FullSensor sensor) {
        ThingUID thingUID = getThingUID(sensor);
        ThingTypeUID thingTypeUID = getThingTypeUID(sensor);

        String modelId = sensor.getNormalizedModelID();

        if (thingUID != null && thingTypeUID != null) {
            ThingUID bridgeUID = hueBridgeHandler.getThing().getUID();
            Map<String, Object> properties = new HashMap<>();
            properties.put(SENSOR_ID, sensor.getId());
            if (modelId != null) {
                properties.put(Thing.PROPERTY_MODEL_ID, modelId);
            }
            String uniqueID = sensor.getUniqueID();
            if (uniqueID != null) {
                properties.put(UNIQUE_ID, uniqueID);
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(UNIQUE_ID)
                    .withLabel(sensor.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported sensor of type '{}' and model '{}' with id {}", sensor.getType(),
                    modelId, sensor.getId());
        }
    }

    @Override
    public void onSensorGone(@Nullable HueBridge bridge, FullSensor sensor) {
        onSensorRemovedInternal(sensor);
    }

    @Override
    public void onSensorRemoved(@Nullable HueBridge bridge, FullSensor sensor) {
        onSensorRemovedInternal(sensor);
    }

    public void onSensorRemovedInternal(FullSensor sensor) {
        ThingUID thingUID = getThingUID(sensor);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    public void onSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor) {
        // nothing to do
    }
}
