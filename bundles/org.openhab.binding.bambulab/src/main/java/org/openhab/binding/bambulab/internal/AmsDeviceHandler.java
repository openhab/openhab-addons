/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.BRIDGE_UNINITIALIZED;
import static org.openhab.core.types.UnDefType.UNDEF;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class AmsDeviceHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(AmsDeviceHandler.class);
    private @Nullable AmsDeviceConfiguration config;
    private @Nullable PrinterHandler printer;

    public AmsDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            internalInitialize();
        } catch (InitializationException e) {
            logger.debug("Error during initialization", e);
            updateStatus(OFFLINE, e.getThingStatusDetail(), e.getDescription());
        }
    }

    private void internalInitialize() throws InitializationException {
        printer = validateBridge();
        var config = this.config = getConfigAs(AmsDeviceConfiguration.class);
        config.validateNumber();
        logger = LoggerFactory.getLogger(
                "%s.%s.%d".formatted(AmsDeviceHandler.class.getName(), printer.getSerialNumber(), config.number));
        updateStatus(ONLINE);
    }

    private PrinterHandler validateBridge() throws InitializationException {
        var bridge = getBridge();
        if (bridge == null) {
            throw new InitializationException(BRIDGE_UNINITIALIZED,
                    "@text/thing-type.config.bambulab.ams-device.init.no-bridge");
        }
        if (!(bridge.getHandler() instanceof PrinterHandler printer)) {
            throw new InitializationException(BRIDGE_UNINITIALIZED,
                    "@text/thing-type.config.bambulab.ams-device.init.bridge-wrong-type");
        }
        return printer;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            var amsChannel = AmsChannel.findAmsChannel(channelUID);
            if (amsChannel.isEmpty()) {
                logger.warn("Could not find AmsChannel for channel UUID: [{}]!", channelUID);
                return;
            }
            Optional.ofNullable(printer)//
                    .flatMap(p -> p.findLatestAms(getAmsNumber()))//
                    .ifPresent(ams -> updateAms(amsChannel.get(), ams));
        }
    }

    public void updateAms(Map<String, Object> ams) {
        if (logger.isDebugEnabled()) {
            var number = Optional.ofNullable(config)//
                    .map(c -> c.number)//
                    .map(Objects::toString)//
                    .orElse("?!");
            logger.debug("Updating AMS #{}", number);
        }
        stream(AmsChannel.values()).forEach(channel -> updateAms(channel, ams));
    }

    private void updateAms(AmsChannel channel, Map<String, Object> ams) {
        Optional.of(ams)//
                .map(map -> map.get("tray"))//
                .filter(obj -> obj instanceof Collection<?>)//
                .map(obj -> (Collection<?>) obj)//
                .stream()//
                .flatMap(Collection::stream)//
                .filter(obj -> obj instanceof Map<?, ?>)//
                .map(obj -> (Map<?, ?>) obj)//
                .forEach(map -> updateAmsTray(channel, map));
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    private void updateAmsTray(AmsChannel channel, Map<?, ?> map) {
        var someId = findKey(map, "id")//
                .map(Object::toString)//
                .map(Integer::parseInt)//
                .flatMap(AmsChannel.TrayId::parseFromApi);
        if (someId.isEmpty()) {
            logger.warn("There is no tray ID in {}", map);
            return;
        }
        var trayId = someId.get();
        var key = findKey(map, channel.getJsonKey()).map(Object::toString);
        var state = switch (channel) {
            case CHANNEL_TRAY_TYPE -> //
                key.flatMap(name -> {
                    if (name.isBlank()) {
                        logger.debug("Tray type is blank");
                        return Optional.empty();
                    }
                    var trayType = AmsChannel.TrayType.findTrayType(name);
                    if (trayType.isEmpty()) {
                        var msg = "Cannot parse tray type from [{}]! Please report this on https://github.com/openhab/openhab-addons .";
                        if (logger.isDebugEnabled()) {
                            logger.debug(msg + " Full map: {}", name, map);
                        } else {
                            logger.warn(msg, name);
                        }
                    }
                    return trayType;
                })//
                        .map(Enum::name)//
                        .map(value -> (State) StringType.valueOf(value));
            case CHANNEL_TRAY_COLOR -> key.map(StateParserHelper::parseColor);
            case CHANNEL_NOZZLE_TEMPERATURE_MAX -> key.flatMap(StateParserHelper::parseTemperatureType);
            case CHANNEL_NOZZLE_TEMPERATURE_MIN -> key.flatMap(StateParserHelper::parseTemperatureType);
            case CHANNEL_REMAIN -> key.flatMap(StateParserHelper::parsePercentType);
            case CHANNEL_K -> key.flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_N -> key.flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_TAG_UUID -> key.flatMap(StateParserHelper::parseStringType);
            case CHANNEL_TRAY_ID_NAME -> key.flatMap(StateParserHelper::parseStringType);
            case CHANNEL_TRAY_INFO_IDX -> key.flatMap(StateParserHelper::parseStringType);
            case CHANNEL_TRAY_SUB_BRANDS -> key.flatMap(StateParserHelper::parseStringType);
            case CHANNEL_TRAY_WEIGHT -> key.flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_TRAY_DIAMETER -> key.flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_TRAY_TEMPERATURE -> key.flatMap(StateParserHelper::parseTemperatureType);
            case CHANNEL_TRAY_TIME -> key.flatMap(StateParserHelper::parseDecimalType);
            case CHANNEL_BED_TEMPERATURE_TYPE -> key.flatMap(StateParserHelper::parseStringType);
            case CHANNEL_BED_TEMPERATURE -> key.flatMap(StateParserHelper::parseTemperatureType);
            case CHANNEL_CTYPE -> key.flatMap(StateParserHelper::parseDecimalType);
        };
        updateState(channel.findType(trayId), requireNonNull(state.orElse(UNDEF)));
    }

    private static Optional<?> findKey(Map<?, ?> map, String key) {
        return Optional.of(map).map(m -> m.get(key));
    }

    public int getAmsNumber() {
        return requireNonNull(config, "At this point config should be initialized").number;
    }

    @Override
    public void dispose() {
        printer = null;
        config = null;
        logger = LoggerFactory.getLogger(AmsDeviceHandler.class);
    }
}
