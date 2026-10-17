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
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * The {@link VacuumEntity} class represents a vacuum entity type in the Home Assistant binding.
 * Maps robotic vacuum states to openHAB String and Switch channels for operational control and monitoring.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class VacuumEntity implements EntityType {

    // https://github.com/home-assistant/core/blob/a09f39b521671fd4f71316dae7600b47625c3bd6/homeassistant/components/vacuum/const.py#L40
    private static final long PAUSE = 4L;
    private static final long STOP = 8L;
    private static final long RETURN_HOME = 16L;
    private static final long FAN_SPEED = 32L;
    private static final long LOCATE = 512L;
    private static final long CLEAN_SPOT = 1024L;
    private static final long START = 8192L;

    // Accepted command strings for mapping to Home Assistant service calls
    private static final String START_CMD = "start";
    private static final String STOP_CMD = "stop";
    private static final String PAUSE_CMD = "pause";
    private static final String DOCK_CMD = "dock";
    private static final String SPOT_CMD = "spot";
    private static final String LOCATE_CMD = "locate";

    private static final Map<Long, CommandOption> COMMAND_MAP;
    static {
        Map<Long, CommandOption> map = new LinkedHashMap<>();
        map.put(START, new CommandOption(START_CMD, "Start Cleaning"));
        map.put(STOP, new CommandOption(STOP_CMD, "Stop Cleaning"));
        map.put(PAUSE, new CommandOption(PAUSE_CMD, "Pause Cleaning"));
        map.put(RETURN_HOME, new CommandOption(DOCK_CMD, "Return to Base"));
        map.put(CLEAN_SPOT, new CommandOption(SPOT_CMD, "Spot Clean"));
        map.put(LOCATE, new CommandOption(LOCATE_CMD, "Locate Vacuum"));
        COMMAND_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public String getType() {
        return "vacuum";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING, AutoUpdatePolicy.VETO) //
                .addAttrIfSupported(FAN_SPEED, "fan_speed", ItemType.STRING) //
                .addAttr("cleaned_area", ItemType.NUMBER) //
                .addGenericAttributes() //
                .build();
    }

    @Override
    public @Nullable StateDescriptionFragment getStateDescriptionFragment(EntityState entityState, String attribute,
            EntityContext context) {
        return switch (attribute) {
            case "fan_speed" -> OptionUtils.extractStateOptions(entityState, "fan_speed_list");
            default -> null;
        };
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
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .putString("fan_speed") //
                .putDecimal("cleaned_area") //
                .putGenericAttributes() //
                .build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        switch (attribute) {
            case EntityType.PRIMARY_ATTR -> {
                return CommandMapper.onParameterizedStringMapped(command, getType(), entityId,
                        (action, arg, params) -> switch (action) {
                            case START_CMD, "on" -> "start";
                            case STOP_CMD, "off" -> "stop";
                            case PAUSE_CMD -> "pause";
                            case DOCK_CMD, "return_to_base", "return", "home" -> "return_to_base";
                            case SPOT_CMD, "clean_spot", "spot_clean" -> "clean_spot";
                            case LOCATE_CMD -> "locate";
                            case "clean_segments", "segment" -> {
                                if (arg != null) {
                                    params.put("segments", CommandMapper.csvToList(arg));
                                }
                                yield "clean_segments";
                            }
                            default -> action;
                        });
            }
            case "fan_speed" -> {
                return CommandMapper.onString(command, getType(), "set_fan_speed", "fan_speed", entityId);
            }
        }
        return Optional.empty();
    }
}
