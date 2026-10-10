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
import org.openhab.binding.hasslink.internal.entity.util.OptionUtils;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * The {@link FanEntity} class represents a fan entity type in the Home Assistant binding.
 * It provides methods to build channels and update states specific to fan devices.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class FanEntity implements EntityType {

    // https://github.com/home-assistant/core/blob/e299eca19c7cd282a24573da37921ffab1229e49/homeassistant/components/fan/__init__.py#L44
    private static final long SET_SPEED = 1L;
    private static final long OSCILLATE = 2L;
    private static final long DIRECTION = 4L;
    private static final long PRESET_MODE = 8L;

    @Override
    public String getType() {
        return "fan";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.SWITCH) //
                .addAttrIfSupported(SET_SPEED, "percentage", ItemType.DIMMER) //
                .addAttrIfSupported(OSCILLATE, "oscillating", ItemType.SWITCH) //
                .addAttrIfSupported(DIRECTION, "direction", ItemType.STRING) //
                .addAttrIfSupported(PRESET_MODE, "preset_mode", ItemType.STRING) //
                .build();
    }

    @Override
    public @Nullable StateDescriptionFragment getStateDescriptionFragment(EntityState entityState, String attribute,
            EntityContext context) {
        return switch (attribute) {
            case "preset_mode" -> OptionUtils.extractStateOptions(entityState, "preset_modes");
            default -> null;
        };
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryOnOffState() //
                .putPercent("percentage") //
                .putOnOff("oscillating") //
                .putString("direction") //
                .putString("preset_mode") //
                .build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {

        return switch (attribute) {
            case PRIMARY_ATTR -> CommandMapper.onOff(command, getType(), entityId);
            case "percentage" -> CommandMapper.onPercent(command, getType(), "set_percentage", "percentage", entityId);
            case "oscillating" -> CommandMapper.onOffBoolean(command, getType(), "oscillate", "oscillating", entityId);
            case "direction" -> CommandMapper.onString(command, getType(), "set_direction", "direction", entityId);
            case "preset_mode" ->
                CommandMapper.onString(command, getType(), "set_preset_mode", "preset_mode", entityId);
            default -> Optional.empty();
        };
    }
}
