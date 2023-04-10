/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.config.BAE091xHandlerConfiguration;
import org.openhab.binding.onewire.internal.device.BAE0910;
import org.openhab.binding.onewire.internal.device.OwChannelConfig;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BAE091xSensorThingHandler} is responsible for handling BAE0910 based multisensors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BAE091xSensorThingHandler extends OwBaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BAE091X);

    private final Logger logger = LoggerFactory.getLogger(BAE091xSensorThingHandler.class);

    public static final Set<OwSensorType> SUPPORTED_SENSOR_TYPES = Set.of(OwSensorType.BAE0910);

    public BAE091xSensorThingHandler(Thing thing, OwDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider, SUPPORTED_SENSOR_TYPES);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            if (channelUID.getId().startsWith(CHANNEL_DIGITAL)) {
                Bridge bridge = getBridge();
                if (bridge != null) {
                    OwserverBridgeHandler bridgeHandler = (OwserverBridgeHandler) bridge.getHandler();
                    if (bridgeHandler != null) {
                        if (!((BAE0910) sensors.get(0)).writeChannel(bridgeHandler, channelUID.getId(), command)) {
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
        // TODO: PWM channels
        super.handleCommand(channelUID, command);
    }

    @Override
    public void initialize() {
        if (!super.configureThingHandler()) {
            return;
        }

        sensors.add(new BAE0910(sensorId, this));

        scheduler.execute(this::configureThingChannels);
    }

    @Override
    protected void configureThingChannels() {
        ThingUID thingUID = getThing().getUID();
        logger.debug("configuring sensors for {}", thingUID);

        BAE091xHandlerConfiguration configuration = getConfig().as(BAE091xHandlerConfiguration.class);

        Set<OwChannelConfig> wantedChannel = new HashSet<>(SENSOR_TYPE_CHANNEL_MAP.getOrDefault(sensorType, Set.of()));

        // Pin1:
        switch (configuration.pin1) {
            case CONFIG_BAE_PIN_DISABLED:
                break;
            case CONFIG_BAE_PIN_COUNTER:
                wantedChannel.add(new OwChannelConfig(CHANNEL_COUNTER, CHANNEL_TYPE_UID_BAE_COUNTER));
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 1");
                return;
        }

        // Pin2:
        switch (configuration.pin2) {
            case CONFIG_BAE_PIN_DISABLED:
                break;
            case CONFIG_BAE_PIN_OUT:
                wantedChannel.add(
                        new OwChannelConfig(CHANNEL_DIGITAL2, CHANNEL_TYPE_UID_BAE_DIGITAL_OUT, "Digital Out Pin 2"));
                break;
            case CONFIG_BAE_PIN_PWM:
                wantedChannel
                        .add(new OwChannelConfig(CHANNEL_PWM_DUTY3, CHANNEL_TYPE_UID_BAE_PWM_DUTY, "Duty Cycle PWM 3"));
                wantedChannel.add(new OwChannelConfig(CHANNEL_PWM_FREQ1, CHANNEL_TYPE_UID_BAE_PWM_FREQUENCY,
                        "Frequency PWM 1/3"));
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 2");
                return;
        }

        // Pin6:
        switch (configuration.pin6) {
            case CONFIG_BAE_PIN_DISABLED:
                break;
            case CONFIG_BAE_PIN_PIO:
                wantedChannel.add(new OwChannelConfig(CHANNEL_DIGITAL6, CHANNEL_TYPE_UID_BAE_PIO, "PIO Pin 6"));
                break;
            case CONFIG_BAE_PIN_PWM:
                wantedChannel
                        .add(new OwChannelConfig(CHANNEL_PWM_DUTY4, CHANNEL_TYPE_UID_BAE_PWM_DUTY, "Duty Cycle PWM 4"));
                wantedChannel.add(new OwChannelConfig(CHANNEL_PWM_FREQ2, CHANNEL_TYPE_UID_BAE_PWM_FREQUENCY,
                        "Frequency PWM 2/4"));
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 6");
                return;
        }

        // Pin7:
        switch (configuration.pin7) {
            case CONFIG_BAE_PIN_DISABLED:
                break;
            case CONFIG_BAE_PIN_ANALOG:
                wantedChannel.add(new OwChannelConfig(CHANNEL_VOLTAGE, CHANNEL_TYPE_UID_BAE_ANALOG, "Analog Input"));
                break;
            case CONFIG_BAE_PIN_OUT:
                wantedChannel.add(
                        new OwChannelConfig(CHANNEL_DIGITAL7, CHANNEL_TYPE_UID_BAE_DIGITAL_OUT, "Digital Out Pin 7"));
                break;
            case CONFIG_BAE_PIN_PWM:
                wantedChannel
                        .add(new OwChannelConfig(CHANNEL_PWM_DUTY2, CHANNEL_TYPE_UID_BAE_PWM_DUTY, "Duty Cycle PWM 2"));
                wantedChannel.add(new OwChannelConfig(CHANNEL_PWM_FREQ2, CHANNEL_TYPE_UID_BAE_PWM_FREQUENCY,
                        "Frequency PWM 2/4"));
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 7");
                return;
        }

        // Pin8:
        switch (configuration.pin8) {
            case CONFIG_BAE_PIN_DISABLED:
                break;
            case CONFIG_BAE_PIN_IN:
                wantedChannel.add(new OwChannelConfig(CHANNEL_DIGITAL8, CHANNEL_TYPE_UID_BAE_DIN, "Digital In Pin 8"));
                break;
            case CONFIG_BAE_PIN_OUT:
                wantedChannel
                        .add(new OwChannelConfig(CHANNEL_DIGITAL8, CHANNEL_TYPE_UID_BAE_DOUT, "Digital Out Pin 8"));
                break;
            case CONFIG_BAE_PIN_PWM:
                wantedChannel
                        .add(new OwChannelConfig(CHANNEL_PWM_DUTY1, CHANNEL_TYPE_UID_BAE_PWM_DUTY, "Duty Cycle PWM 1"));
                wantedChannel.add(new OwChannelConfig(CHANNEL_PWM_FREQ1, CHANNEL_TYPE_UID_BAE_PWM_FREQUENCY,
                        "Frequency PWM 1/3"));

                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "unknown configuration option for pin 8");
                return;
        }

        ThingBuilder thingBuilder = editThing();

        // remove unwanted channels
        Set<String> existingChannelIds = thing.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toSet());
        Set<String> wantedChannelIds = wantedChannel.stream().map(channelConfig -> channelConfig.channelId)
                .collect(Collectors.toSet());
        existingChannelIds.stream().filter(channelId -> !wantedChannelIds.contains(channelId))
                .forEach(channelId -> removeChannelIfExisting(thingBuilder, channelId));

        // add or update wanted channels
        wantedChannel.forEach(channelConfig -> {
            addChannelIfMissingAndEnable(thingBuilder, channelConfig);
        });

        updateThing(thingBuilder.build());

        try {
            sensors.get(0).configureChannels();
        } catch (OwException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        validConfig = true;
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
    }

    @Override
    public void updateSensorProperties(OwserverBridgeHandler bridgeHandler) throws OwException {
        Map<String, String> properties = editProperties();

        sensorType = BAE0910.getDeviceSubType(bridgeHandler, sensorId);

        properties.put(PROPERTY_MODELID, sensorType.toString());
        properties.put(PROPERTY_VENDOR, "Brain4home");

        updateProperties(properties);

        logger.trace("updated modelid/vendor to {} / {}", sensorType.name(), "Brain4home");
    }
}
