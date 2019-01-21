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
package org.openhab.binding.onewire.internal.handler;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.AbstractDigitalOwDevice;
import org.openhab.binding.onewire.internal.device.DS2405;
import org.openhab.binding.onewire.internal.device.DS2406_DS2413;
import org.openhab.binding.onewire.internal.device.DS2408;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DigitalIOThingHandler} is responsible for handling the Digital I/O devices
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DigitalIOThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_DIGITALIO, THING_TYPE_DIGITALIO2, THING_TYPE_DIGITALIO8).collect(Collectors.toSet()));
    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Collections.unmodifiableSet(
            Stream.of(OwSensorType.DS2405, OwSensorType.DS2406, OwSensorType.DS2408, OwSensorType.DS2413)
                    .collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(DigitalIOThingHandler.class);

    public DigitalIOThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            Integer ioChannel = Integer.valueOf(channelUID.getId().substring(channelUID.getId().length() - 1));
            if (ioChannel != null && ioChannel < ((AbstractDigitalOwDevice) sensors.get(0)).getChannelCount()) {
                Bridge bridge = getBridge();
                if (bridge != null) {
                    OwBaseBridgeHandler bridgeHandler = (OwBaseBridgeHandler) bridge.getHandler();
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

    @Override
    public void initialize() {
        Configuration configuration = getConfig();

        if (!super.configure()) {
            return;
        }

        if (this.thing.getThingTypeUID().equals(THING_TYPE_DIGITALIO)) {
            sensors.add(new DS2405(sensorId, this));
        } else if (this.thing.getThingTypeUID().equals(THING_TYPE_DIGITALIO2)) {
            sensors.add(new DS2406_DS2413(sensorId, this));
        } else if (this.thing.getThingTypeUID().equals(THING_TYPE_DIGITALIO8)) {
            sensors.add(new DS2408(sensorId, this));
        }

        // sensor configuration
        try {
            sensors.get(0).configureChannels();
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        validConfig = true;

        if (configuration.get(CONFIG_REFRESH) == null) {
            // override default of 300s from base thing handler if no user-defined value is present
            refreshInterval = 10 * 1000;
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
    }

    @Override
    public void refresh(OwBaseBridgeHandler bridgeHandler, long now) {
        try {
            Boolean forcedRefresh = lastRefresh == 0;

            if (now >= (lastRefresh + refreshInterval)) {
                logger.trace("refreshing {}", this.thing.getUID());
                lastRefresh = now;

                if (!sensors.get(0).checkPresence(bridgeHandler)) {
                    return;
                }

                sensors.get(0).refresh(bridgeHandler, forcedRefresh);
            }
        } catch (OwException e) {
            logger.debug("{}: refresh exception {}", this.thing.getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "refresh exception");
            return;
        }
    }
}
