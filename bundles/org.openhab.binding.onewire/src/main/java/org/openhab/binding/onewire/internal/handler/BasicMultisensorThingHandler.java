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
package org.openhab.binding.onewire.internal.handler;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.DS2438Configuration;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.config.MstxHandlerConfiguration;
import org.openhab.binding.onewire.internal.device.DS1923;
import org.openhab.binding.onewire.internal.device.DS2438;
import org.openhab.binding.onewire.internal.device.DS2438.CurrentSensorType;
import org.openhab.binding.onewire.internal.device.DS2438.LightSensorType;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BasicMultisensorThingHandler} is responsible for handling DS2438/DS1923 based multisensors (single
 * sensors)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BasicMultisensorThingHandler extends OwBaseThingHandler {
    public Logger logger = LoggerFactory.getLogger(BasicMultisensorThingHandler.class);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_MS_TX);
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Collections
            .unmodifiableSet(Stream.of(OwSensorType.MS_TH, OwSensorType.MS_TC, OwSensorType.MS_TL, OwSensorType.MS_TV,
                    OwSensorType.DS1923, OwSensorType.DS2438).collect(Collectors.toSet()));

    public BasicMultisensorThingHandler(Thing thing,
            OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES);
    }

    @Override
    public void initialize() {
        if (!super.configureThingHandler()) {
            return;
        }

        MstxHandlerConfiguration configuration = getConfig().as(MstxHandlerConfiguration.class);
        if (configuration.manualsensor != null && sensorType != configuration.manualsensor) {
            logger.debug("sensorType override for thing {}: old={}, new={}", thing.getUID(), sensorType,
                    configuration.manualsensor);
            sensorType = configuration.manualsensor;
        }

        // add sensors
        if (sensorType == OwSensorType.DS1923) {
            sensors.add(new DS1923(sensorId, this));
        } else {
            sensors.add(new DS2438(sensorId, this));
        }

        scheduler.execute(() -> {
            configureThingChannels();
        });
    }

    @Override
    protected void configureThingChannels() {
        switch (sensorType) {
            case DS2438:
                ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.INTERNAL);
                break;
            case MS_TC:
                ((DS2438) sensors.get(0)).setCurrentSensorType(CurrentSensorType.IBUTTONLINK);
                break;
            case MS_TL:
                ((DS2438) sensors.get(0)).setLightSensorType(LightSensorType.IBUTTONLINK);
                break;
            default:
        }

        super.configureThingChannels();
    }

    @Override
    public void updateSensorProperties(OwserverBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = editProperties();
        sensorType = bridgeHandler.getType(sensorId);

        if (sensorType == OwSensorType.DS1923) {
            properties.put(PROPERTY_MODELID, sensorType.toString());
            properties.put(PROPERTY_VENDOR, "Dallas/Maxim");
        } else {
            DS2438Configuration ds2438configuration = new DS2438Configuration(bridgeHandler, sensorId);

            sensorType = ds2438configuration.getSensorSubType();
            properties.put(PROPERTY_MODELID, sensorType.toString());

            String vendor = ds2438configuration.getVendor();
            properties.put(PROPERTY_VENDOR, vendor);
        }

        updateProperties(properties);
    }
}
