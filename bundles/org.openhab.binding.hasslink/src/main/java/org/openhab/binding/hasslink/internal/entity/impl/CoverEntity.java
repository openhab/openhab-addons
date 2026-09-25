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
import org.openhab.core.types.Command;

/**
 * The {@link CoverEntity} class represents a cover entity type in the Home Assistant binding.
 * It provides methods to build channels and update states specific to cover devices.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class CoverEntity implements EntityType {

    // https://github.com/home-assistant/core/blob/10aebaf4233e8b2701a30df0bb9b08ed2dcecc92/homeassistant/components/cover/const.py#L34
    public static final long OPEN = 1L;
    public static final long CLOSE = 2L;
    public static final long SET_POSITION = 4L;
    public static final long STOP = 8L;
    public static final long OPEN_TILT = 16L;
    public static final long CLOSE_TILT = 32L;
    public static final long STOP_TILT = 64L;
    public static final long SET_TILT_POSITION = 128L;
    public static final long SPEED = 256L;

    public static final long TILT = OPEN_TILT | CLOSE_TILT | STOP_TILT | SET_TILT_POSITION;

    @Override
    public String getType() {
        return "cover";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING) //
                .addAttr("current_position", ItemType.ROLLERSHUTTER) //
                .addAttrIfSupported(TILT, "current_tilt_position", ItemType.ROLLERSHUTTER) //
                .build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .putInvertedPercent("current_position") //
                .putInvertedPercent("current_tilt_position").build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        return switch (attribute) {
            case "current_position" -> //
                CommandMapper.onUpDown(command, getType(), "open_cover", "close_cover", entityId)
                        .or(() -> CommandMapper.onStopMove(command, getType(), "stop_cover", null, entityId))
                        .or(() -> CommandMapper.onPercentInverted(command, getType(), "set_cover_position", "position",
                                entityId));

            case "current_tilt_position" ->
                CommandMapper.onUpDown(command, getType(), "open_cover_tilt", "close_cover_tilt", entityId)
                        .or(() -> CommandMapper.onStopMove(command, getType(), "stop_cover_tilt", null, entityId))
                        .or(() -> CommandMapper.onPercentInverted(command, getType(), "set_cover_tilt_position",
                                "position", entityId));

            default -> Optional.empty();
        };
    }
}
