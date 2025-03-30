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

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel.MAX_AMS_TRAYS;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.BRIDGE_UNINITIALIZED;

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
            Optional.ofNullable(printer)//
                    .flatMap(p -> p.findLatestAms(getAmsNumber()))//
                    .ifPresent(this::updateAms);
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
        Optional.of(ams)//
                .map(map -> map.get("tray"))//
                .filter(obj -> obj instanceof Collection<?>)//
                .map(obj -> (Collection<?>) obj)//
                .stream()//
                .flatMap(Collection::stream)//
                .filter(obj -> obj instanceof Map<?, ?>)//
                .map(obj -> (Map<?, ?>) obj)//
                .forEach(this::updateAmsTray);
    }

    private void updateAmsTray(Map<?, ?> map) {
        var someId = findKey(map, "id")//
                .map(Object::toString)//
                .map(Integer::parseInt)
                // tray ID in api starts from 0 and for channels it starts for 1
                .map(t -> t + 1);
        if (someId.isEmpty()) {
            logger.warn("There is no tray ID in {}", map);
            return;
        }
        int trayId = someId.get();
        if (trayId > MAX_AMS_TRAYS) {
            logger.warn("Tray ID needs to be lower that {}. Was {}", MAX_AMS_TRAYS, trayId);
            return;
        }

        findKey(map, "tray_type")//
                .map(Object::toString)//
                .flatMap(AmsChannel.TrayType::findTrayType)//
                .map(Enum::name)//
                .map(value -> (State) StringType.valueOf(value))//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTrayTypeChannel(trayId), value));
        findKey(map, "tray_color")//
                .map(Object::toString)//
                .map(StateParserHelper::parseColor)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTrayColorChannel(trayId), value));
        findKey(map, "nozzle_temp_max")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseTemperatureType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getNozzleTemperatureMaxChannel(trayId), value));
        findKey(map, "nozzle_temp_min")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseTemperatureType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getNozzleTemperatureMinChannel(trayId), value));
        findKey(map, "remain")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parsePercentType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getRemainChannel(trayId), value));
        findKey(map, "k")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseDecimalType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getKChannel(trayId), value));
        findKey(map, "n")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseDecimalType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getNChannel(trayId), value));
        findKey(map, "tag_uuid")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseStringType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTagUuidChannel(trayId), value));
        findKey(map, "tray_id_name")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseStringType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTrayIdNameChannel(trayId), value));
        findKey(map, "tray_info_idx")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseStringType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTrayInfoIdxChannel(trayId), value));
        findKey(map, "tray_sub_brands")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseStringType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTraySubBrandsChannel(trayId), value));
        findKey(map, "tray_weight")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseDecimalType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTrayWeightChannel(trayId), value));
        findKey(map, "tray_diameter")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseDecimalType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTrayDiameterChannel(trayId), value));
        findKey(map, "tray_temp")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseTemperatureType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTrayTemperatureChannel(trayId), value));
        findKey(map, "tray_time")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseDecimalType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getTrayTimeChannel(trayId), value));
        findKey(map, "bed_temp_type")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseStringType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getBedTemperatureTypeChannel(trayId), value));
        findKey(map, "bed_temp")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseTemperatureType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getBedTemperatureChannel(trayId), value));
        findKey(map, "ctype")//
                .map(Object::toString)//
                .flatMap(StateParserHelper::parseDecimalType)//
                .or(StateParserHelper::undef)//
                .ifPresent(value -> updateState(AmsChannel.getCtypeChannel(trayId), value));
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
