/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hp1000.handler;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HP1000Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Bauer - Initial contribution
 */
@NonNullByDefault
public class HP1000Handler extends BaseThingHandler {

    private ChannelTypeRegistry channelTypeRegistry;
    private final Logger logger = LoggerFactory.getLogger(HP1000Handler.class);

    public HP1000Handler(ChannelTypeRegistry channelTypeRegistry, Thing thing) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    private State buildChannelState(Channel channel, String value) {
        String acceptedItemType = channel.getAcceptedItemType();
        if (acceptedItemType == null) {
            return UnDefType.NULL;
        }
        switch (acceptedItemType) {
            case "Number":
                Double doubleValue = StateConverterUtils.parseDouble(value);
                if (doubleValue != null) {
                    doubleValue = formatUnits(channel, doubleValue);
                }
                return StateConverterUtils.toDecimalType(doubleValue);
            case "String":
                return StateConverterUtils.toStringType(value);
            case "DateTime":
                return StateConverterUtils.toDateTimeType(value);
            default:
                logger.error("accepted item type {} not handeled", channel.getAcceptedItemType());
        }
        return UnDefType.NULL;
    }

    private Double formatUnits(Channel channel, Double value) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
        if (channelType == null) {
            return value;
        }
        String pattern = channelType.getState().getPattern();
        if (pattern.contains("°C")) {
            return UnitConverterUtils.fahrenheitToCelius(value);
        } else if (pattern.contains("km/h")) {
            return UnitConverterUtils.mphTokmh(value);
        } else if (pattern.contains("mm")) {
            return UnitConverterUtils.inchesToMillimeters(value);
        } else if (pattern.contains("hPa")) {
            return UnitConverterUtils.inhgToHpa(value);
        }

        return value;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not supported
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void webHookEvent(Map<String, String[]> paramterMap) {
        getThing().getChannels().stream().forEach(channel -> {
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID == null) {
                return;
            }
            String channelId = channelTypeUID.getId();
            Optional<String> parameterKey = paramterMap.keySet().stream().filter(key -> key.equalsIgnoreCase(channelId))
                    .findFirst();
            if (parameterKey.isPresent()) {
                String[] parameterValues = paramterMap.get(parameterKey.get());
                if (parameterValues.length > 0) {
                    updateState(channel.getUID(), buildChannelState(channel, parameterValues[0]));
                }
            } else {
                logger.error("parameter map not contains channelId {}", channelId);
            }
        });
    }
}
