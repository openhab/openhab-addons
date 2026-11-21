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
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * The {@link ValveEntity} class represents a valve entity type in the Home Assistant binding.
 * Maps motorized valves to openHAB Switch and Rollershutter channels and executes open/close/position service calls.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ValveEntity implements EntityType {

    @Override
    public String getType() {
        return "valve";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING) //
                .add("current_position", ItemType.DIMMER) //
                .add("open", ItemType.SWITCH) //
                .add("close", ItemType.SWITCH) //
                .add("stop", ItemType.SWITCH) //
                .build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .putPercent("current_position") //

                // These are command switches, always turn them off when we receive a state update
                .put("open", OnOffType.OFF) //
                .put("close", OnOffType.OFF) //
                .put("stop", OnOffType.OFF) //
                .build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        return switch (attribute) {
            case "open" -> CommandMapper.onOffCustom(command, "valve", "open_valve", null, entityId);
            case "close" -> CommandMapper.onOffCustom(command, "valve", "close_valve", null, entityId);
            case "stop" -> CommandMapper.onOffCustom(command, "valve", "stop_valve", null, entityId);
            case "current_position" ->
                CommandMapper.onPercent(command, "valve", "set_valve_position", "position", entityId);
            default -> Optional.empty();
        };
    }
}
