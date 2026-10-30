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

import java.util.Collections;
import java.util.LinkedHashMap;
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
import org.openhab.core.types.CommandOption;

/**
 * The {@link LawnMowerEntity} class represents a {@code lawn_mower} entity type in the Home Assistant binding.
 * It provides channels, state parsing, and dynamic command options based on supported features.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class LawnMowerEntity implements EntityType {

    private static final long FEATURE_START_MOWING = 1L;
    private static final long FEATURE_PAUSE = 2L;
    private static final long FEATURE_DOCK = 4L;
    private static final long FEATURE_STOP = 8L;

    private static final Map<Long, CommandOption> COMMAND_MAP;

    static {
        Map<Long, CommandOption> map = new LinkedHashMap<>();
        map.put(FEATURE_START_MOWING, new CommandOption("start", "Start Mowing"));
        map.put(FEATURE_PAUSE, new CommandOption("pause", "Pause"));
        map.put(FEATURE_DOCK, new CommandOption("dock", "Return to Base"));
        map.put(FEATURE_STOP, new CommandOption("stop", "Stop"));
        COMMAND_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public String getType() {
        return "lawn_mower";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING) //
                .build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .build();
    }

    @Override
    public @Nullable List<CommandOption> getCommandOptions(EntityState entityState, String attribute,
            EntityContext context) {
        if (EntityType.isPrimary(attribute)) {
            return OptionUtils.extractCommandBitmaskOptions(entityState, "supported_features", List.of(), COMMAND_MAP);
        }
        return null;
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String channelSuffix, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        if (EntityType.isPrimary(channelSuffix)) {
            return CommandMapper.onStringMapped(command, "lawn_mower", entityId,
                    action -> switch (action.toLowerCase()) {
                        case "start" -> "start_mowing";
                        case "pause" -> "pause";
                        case "stop" -> "stop";
                        case "dock", "return_to_base" -> "dock";
                        default -> null;
                    });
        }
        return Optional.empty();
    }
}
