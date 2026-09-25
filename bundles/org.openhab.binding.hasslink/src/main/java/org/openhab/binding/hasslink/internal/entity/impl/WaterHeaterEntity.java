/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal.entity.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.CommandMapper;
import org.openhab.binding.hasslink.internal.entity.util.EntityUnitResolver;
import org.openhab.binding.hasslink.internal.entity.util.OptionUtils;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * The {@link WaterHeaterEntity} class represents a water heater entity type in the Home Assistant binding.
 * Maps water heater operation modes and target/current temperatures to openHAB String and Number channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class WaterHeaterEntity implements EntityType {

    // https://github.com/home-assistant/core/blob/89b4a9a46b9fa7fc1ac131c4e074a4bd751a63bf/homeassistant/components/water_heater/__init__.py#L56
    private static final long TARGET_TEMPERATURE = 1L;
    private static final long OPERATION_MODE = 2L;
    private static final long AWAY_MODE = 4L;
    private static final long ON_OFF = 8L;

    @Override
    public String getType() {
        return "water_heater";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING, AutoUpdatePolicy.VETO) //
                .addIfSupported(ON_OFF, "power", ItemType.SWITCH) //
                .addAttrIfSupported(AWAY_MODE, "away_mode", ItemType.SWITCH) //
                .addAttr("current_temperature", ItemType.number("Temperature")) //
                .addAttrIfSupported(TARGET_TEMPERATURE, "temperature", ItemType.number("Temperature")) //
                .addAttrIfSupported(TARGET_TEMPERATURE, "target_temp_high", ItemType.number("Temperature")) //
                .addAttrIfSupported(TARGET_TEMPERATURE, "target_temp_low", ItemType.number("Temperature")) //
                .build();
    }

    @Override
    public @Nullable StateDescriptionFragment getStateDescriptionFragment(EntityState entityState, String attribute,
            EntityContext context) {
        return switch (attribute) {
            case EntityType.PRIMARY_ATTR -> entityState.isSupportedFeature(OPERATION_MODE)
                    ? OptionUtils.extractStateOptions(entityState, "operation_list")
                    : null;
            case "temperature", "target_temp_high", "target_temp_low" ->
                OptionUtils.extractRange(entityState, "min_temp", "max_temp", "target_temp_step");
            default -> null;
        };
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        String tempUnit = EntityUnitResolver.resolveTemperatureUnit(entityState, context.getHaConfig());

        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .putOnOffValue("power", !"off".equals(entityState.state())) //
                .putOnOff("away_mode") //
                .putQuantity("current_temperature", tempUnit) //
                .putQuantity("temperature", tempUnit) //
                .putQuantity("target_temp_high", tempUnit) //
                .putQuantity("target_temp_low", tempUnit) //
                .build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        return switch (attribute) {
            case EntityType.PRIMARY_ATTR ->
                CommandMapper.onString(command, getType(), "set_operation_mode", "operation_mode", entityId);
            case "power" -> CommandMapper.onOff(command, getType(), entityId);
            case "away_mode" ->
                CommandMapper.onOffCustom(command, getType(), "turn_away_mode_on", "turn_away_mode_off", entityId);
            case "temperature" ->
                CommandMapper.onDecimal(command, getType(), "set_temperature", "temperature", entityId);
            case "target_temp_high" ->
                CommandMapper.onDecimal(command, getType(), "set_temperature", "temperature_high", entityId);
            case "target_temp_low" ->
                CommandMapper.onDecimal(command, getType(), "set_temperature", "temperature_low", entityId);
            default -> Optional.empty();
        };
    }
}
