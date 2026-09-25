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
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * The {@link ClimateEntity} class represents a climate entity type in the Home Assistant binding.
 * It provides methods to build channels and update states specific to climate devices.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ClimateEntity implements EntityType {

    // https://github.com/home-assistant/core/blob/32ad04c584bafda784d1a19357142d7b3af776b8/homeassistant/components/climate/const.py#L185
    public static final long TARGET_TEMPERATURE = 1L;
    public static final long TARGET_TEMPERATURE_RANGE = 2L;
    public static final long TARGET_HUMIDITY = 4L;
    public static final long FAN_MODE = 8L;
    public static final long PRESET_MODE = 16L;
    public static final long SWING_MODE = 32L;
    public static final long TURN_OFF = 128L;
    public static final long TURN_ON = 256L;
    public static final long SWING_HORIZONTAL_MODE = 512L;

    @Override
    public String getType() {
        return "climate";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING, AutoUpdatePolicy.VETO) //
                .add("power", ItemType.SWITCH) // A custom (non-attribute) channel for convenience

                // Read-only sensor channels (attributeName, suffix, itemType, label)
                .addAttr("current_temperature", ItemType.number("Temperature"))
                .addAttr("current_humidity", ItemType.DIMENSIONLESS)

                // Feature-gated channels (featureFlag, attributeName, suffix, itemType, label)
                .addAttrIfSupported(TARGET_TEMPERATURE, "temperature", ItemType.number("Temperature"))
                .addAttrIfSupported(TARGET_TEMPERATURE_RANGE, "target_temp_high", ItemType.number("Temperature"))
                .addAttrIfSupported(TARGET_TEMPERATURE_RANGE, "target_temp_low", ItemType.number("Temperature"))
                .addAttrIfSupported(TARGET_HUMIDITY, "humidity", ItemType.DIMENSIONLESS)

                .addAttrIfSupported(FAN_MODE, "fan_mode", ItemType.STRING)
                .addAttrIfSupported(PRESET_MODE, "preset_mode", ItemType.STRING)
                .addAttrIfSupported(SWING_MODE, "swing_mode", ItemType.STRING)
                .addAttrIfSupported(SWING_HORIZONTAL_MODE, "swing_horizontal_mode", ItemType.STRING) //
                .build();
    }

    @Override
    public @Nullable StateDescriptionFragment getStateDescriptionFragment(EntityState entityState, String property,
            EntityContext context) {
        return switch (property) {
            case EntityType.PRIMARY_ATTR -> OptionUtils.extractStateOptions(entityState, "hvac_modes");

            case "preset_mode" -> OptionUtils.extractStateOptions(entityState, "preset_modes");
            case "fan_mode" -> OptionUtils.extractStateOptions(entityState, "fan_modes");
            case "swing_mode" -> OptionUtils.extractStateOptions(entityState, "swing_modes");
            case "swing_horizontal_mode" -> OptionUtils.extractStateOptions(entityState, "swing_horizontal_modes");
            case "temperature", "target_temp_high", "target_temp_low" ->
                OptionUtils.extractRange(entityState, "min_temp", "max_temp", "target_temp_step");
            case "humidity" -> OptionUtils.extractRange(entityState, "min_humidity", "max_humidity", null);
            default -> null;
        };
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {

        String tempUnit = EntityUnitResolver.resolveTemperatureUnit(entityState, context.getHaConfig());
        boolean powerState = !entityState.isUnavailableOrUnknown() && !"off".equalsIgnoreCase(entityState.state());

        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .putOnOffValue("power", powerState) //

                // Temperatures
                .putQuantity("current_temperature", tempUnit) //
                .putQuantity("temperature", tempUnit) //
                .putQuantity("target_temp_high", tempUnit) //
                .putQuantity("target_temp_low", tempUnit) //

                // Humidity
                .putQuantity("current_humidity", Units.PERCENT) //
                .putQuantity("humidity", Units.PERCENT) //

                // Modes
                .putString("fan_mode") //
                .putString("preset_mode") //
                .putString("swing_mode") //
                .build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall( //
            String entityId, //
            String attribute, //
            Command command, //
            @Nullable EntityState entityState, //
            EntityContext context) {

        String tempUnit = EntityUnitResolver.resolveTemperatureUnit(entityState, context.getHaConfig());

        return switch (attribute) {
            case EntityType.PRIMARY_ATTR ->
                CommandMapper.onString(command, getType(), "set_hvac_mode", "hvac_mode", entityId);

            case "power" -> CommandMapper.onOff(command, getType(), entityId);

            case "temperature" -> CommandMapper.onDecimalOrQuantity(command, tempUnit, getType(), "set_temperature",
                    "temperature", entityId);

            case "target_temp_high" -> CommandMapper.onDecimalOrQuantity(command, tempUnit, getType(),
                    "set_temperature", "target_temp_high", entityId);

            case "target_temp_low" -> CommandMapper.onDecimalOrQuantity(command, tempUnit, getType(), "set_temperature",
                    "target_temp_low", entityId);

            case "humidity" ->
                CommandMapper.onDecimalOrQuantity(command, "%", getType(), "set_humidity", "humidity", entityId)
                        .or(() -> CommandMapper.onPercent(command, getType(), "set_humidity", "humidity", entityId));

            case "fan_mode" -> CommandMapper.onString(command, getType(), "set_fan_mode", "fan_mode", entityId);

            case "preset_mode" ->
                CommandMapper.onString(command, getType(), "set_preset_mode", "preset_mode", entityId);

            case "swing_mode" -> CommandMapper.onString(command, getType(), "set_swing_mode", "swing_mode", entityId);

            default -> Optional.empty();
        };
    }
}
