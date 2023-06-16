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
package org.openhab.binding.growatt.internal.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.growatt.internal.GrowattBindingConstants;
import org.openhab.binding.growatt.internal.GrowattBindingConstants.UoM;
import org.openhab.binding.growatt.internal.config.GrowattInverterConfiguration;
import org.openhab.binding.growatt.internal.dto.GrottDevice;
import org.openhab.binding.growatt.internal.dto.GrottValues;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GrowattInverterHandler} is a thing handler for Growatt inverters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattInverterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GrowattInverterHandler.class);

    private String deviceId = "unknown";

    public GrowattInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // everything is read only so do nothing
    }

    /**
     * Receives a GrottDevice object containing data for this thing. Process the respective data values and update the
     * channels accordingly.
     *
     * @param grottDevice a GrottDevice object containing the new status values.
     */
    public void handleGrottDevice(GrottDevice grottDevice) {
        GrottValues grottValues = grottDevice.getValues();
        if (grottValues == null) {
            logger.debug("handleValues() device '{}' contained no values", grottDevice.getDeviceId());
            return;
        }

        Map<String, State> channelStates = new HashMap<>();
        List<String> missingFields = new ArrayList<>();

        // read channel states from DTO
        for (Entry<String, UoM> entry : GrowattBindingConstants.CHANNEL_ID_UOM_MAP.entrySet()) {
            String channelId = entry.getKey();
            Field field;
            try {
                field = GrottValues.class.getField(channelId);
            } catch (NoSuchFieldException e) {
                missingFields.add(channelId);
                continue;
            } catch (SecurityException e) {
                logger.debug("handleValues() security exception field '{}'", channelId);
                continue;
            }
            Object value;
            try {
                value = field.get(grottValues);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.debug("handleValues() error reading field '{}' value", channelId);
                continue;
            }
            if (value != null && (value instanceof Integer)) {
                UoM uom = entry.getValue();
                channelStates.put(channelId,
                        QuantityType.valueOf(((Integer) value).doubleValue() / uom.divisor, uom.units));
            }
        }

        // warn if fields missing from DTO
        if (!missingFields.isEmpty() && logger.isWarnEnabled()) {
            logger.warn("handleValues() please notify maintainers: GrottValues.class is missing fields: {}",
                    missingFields.stream().collect(Collectors.joining(",")));
        }

        // remove unused channels
        List<Channel> unusedChannels = thing.getChannels().stream()
                .filter(channel -> !channelStates.containsKey(channel.getUID().getId())).collect(Collectors.toList());
        if (!unusedChannels.isEmpty()) {
            updateThing(editThing().withoutChannels(unusedChannels).build());
            logger.debug("handleValues() removed {} unused channels", unusedChannels.size());
        }

        // update channel states
        List<String> channelIds = thing.getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toList());
        channelStates.forEach((channelId, state) -> {
            if (channelIds.contains(channelId)) {
                updateState(channelId, state);
            } else {
                logger.debug("handleValues() channel '{}' not implemented in thing", channelId);
            }
        });
    }

    /**
     * Receives a list of GrottDevice objects containing potential data for this thing. If the list contains any entry
     * matching the things's deviceId then process it further. Otherwise go offline with a configuration error.
     *
     * @param grottDevices list of GrottDevice objects.
     */
    public void handleGrottDevices(List<GrottDevice> grottDevices) {
        grottDevices.stream().filter(grottDevice -> deviceId.equals(grottDevice.getDeviceId())).findAny()
                .ifPresentOrElse(grottDevice -> {
                    updateStatus(ThingStatus.ONLINE);
                    handleGrottDevice(grottDevice);
                }, () -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                });
    }

    @Override
    public void initialize() {
        GrowattInverterConfiguration config = getConfigAs(GrowattInverterConfiguration.class);
        deviceId = config.deviceId;
        updateStatus(ThingStatus.UNKNOWN);
    }
}
