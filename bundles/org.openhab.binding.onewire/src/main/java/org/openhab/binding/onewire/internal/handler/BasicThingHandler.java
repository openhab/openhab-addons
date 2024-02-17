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
package org.openhab.binding.onewire.internal.handler;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.device.AbstractDigitalOwDevice;
import org.openhab.binding.onewire.internal.device.DS18x20;
import org.openhab.binding.onewire.internal.device.DS2401;
import org.openhab.binding.onewire.internal.device.DS2405;
import org.openhab.binding.onewire.internal.device.DS2406_DS2413;
import org.openhab.binding.onewire.internal.device.DS2408;
import org.openhab.binding.onewire.internal.device.DS2423;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BasicThingHandler} is responsible for handling simple sensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BasicThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BASIC);
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Set.of(OwSensorType.DS1420, OwSensorType.DS18B20,
            OwSensorType.DS18S20, OwSensorType.DS1822, OwSensorType.DS2401, OwSensorType.DS2405, OwSensorType.DS2406,
            OwSensorType.DS2408, OwSensorType.DS2413, OwSensorType.DS2423);

    private final Logger logger = LoggerFactory.getLogger(BasicThingHandler.class);

    public BasicThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES);
    }

    @Override
    public void initialize() {
        if (!super.configureThingHandler()) {
            return;
        }

        // add sensor
        switch (sensorType) {
            case DS18B20, DS18S20, DS1822 -> sensors.add(new DS18x20(sensorId, this));
            case DS1420, DS2401 -> sensors.add(new DS2401(sensorId, this));
            case DS2405 -> sensors.add(new DS2405(sensorId, this));
            case DS2406, DS2413 -> sensors.add(new DS2406_DS2413(sensorId, this));
            case DS2408 -> sensors.add(new DS2408(sensorId, this));
            case DS2423 -> sensors.add(new DS2423(sensorId, this));
            default -> throw new IllegalArgumentException(
                    "unsupported sensorType " + sensorType.name() + ", this should have been checked before!");
        }

        scheduler.execute(this::configureThingChannels);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            if (channelUID.getId().startsWith(CHANNEL_DIGITAL) && thing.getChannel(channelUID.getId()) != null) {
                Integer ioChannel = Integer.valueOf(channelUID.getId().substring(channelUID.getId().length() - 1));
                Bridge bridge = getBridge();
                if (bridge != null) {
                    OwserverBridgeHandler bridgeHandler = (OwserverBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        if (!((AbstractDigitalOwDevice) sensors.get(0)).writeChannel(bridgeHandler, ioChannel,
                                command)) {
                            logger.debug("writing to channel {} in thing {} not permitted (input channel)", channelUID,
                                    this.thing.getUID());
                        }
                    } else {
                        logger.warn("bridge handler not found");
                    }
                } else {
                    logger.warn("bridge not found");
                }
            }
        }
        super.handleCommand(channelUID, command);
    }
}
