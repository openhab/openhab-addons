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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link AlarmControlPanelEntity} class represents an alarm control panel entity type in the Home Assistant
 * binding.
 * Maps security system states to openHAB String channels and handles arming and disarming actions.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class AlarmControlPanelEntity implements EntityType {

    private static final CommandOption OPTION_DISARM = new CommandOption("disarm", "Disarm");

    private static final List<CommandOption> DEFAULT_OPTIONS = List.of(OPTION_DISARM);

    private static final Map<Long, CommandOption> COMMAND_MAP;

    static {
        Map<Long, CommandOption> map = new LinkedHashMap<>();
        map.put(2L, new CommandOption("arm", "Arm"));
        map.put(1L, new CommandOption("arm_home", "Arm Home"));
        map.put(4L, new CommandOption("arm_night", "Arm Night"));
        // 8L, new CommandOption("trigger", "Trigger"), // This could cause accidental triggering of the alarm, so
        // it's not included by default
        map.put(16L, new CommandOption("arm_custom_bypass", "Arm Custom Bypass"));
        map.put(32L, new CommandOption("arm_vacation", "Arm Vacation"));
        COMMAND_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public String getType() {
        return "alarm_control_panel";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryActionChannel("Send supported alarm control commands (e.g. arm, disarm, trigger, arm_*)",
                        Set.of("Control", "Mode")) //
                .build();
    }

    @Override
    public @Nullable List<CommandOption> getCommandOptions(EntityState entityState, String attribute,
            EntityContext context) {
        if (EntityType.isPrimary(attribute)) {
            return OptionUtils.extractCommandBitmaskOptions(entityState, "supported_features", DEFAULT_OPTIONS,
                    COMMAND_MAP);
        }
        return null;
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .build();
    }

    /**
     * Converts an openHAB command received on a channel into a Home Assistant {@link ServiceCall}.
     * <p>
     * Commands received on the {@code command} channel are mapped to discrete {@code alarm_control_panel}
     * domain services.
     * <p>
     * <b>Inline Code/PIN Support:</b><br>
     * Commands may optionally append a PIN or code separated by a colon (e.g., {@code "DISARM:1234"},
     * {@code "arm:5678"}, or {@code "arm_home:1234"}). When present, the code is extracted
     * and attached as the {@code "code"} parameter in the resulting {@link ServiceCall} payload.
     * If no code is provided, an empty service call payload is sent.
     *
     * @param entityId the Home Assistant entity ID (e.g., {@code "alarm_control_panel.home_alarm"})
     * @param channelSuffix the channel identifier suffix receiving the command
     * @param command the openHAB {@link Command} being dispatched
     * @param entityState the optional current state of the entity, which may be used for context-sensitive command
     *            mapping
     * @return an {@link Optional} containing the target {@link ServiceCall} if mapped, or {@link Optional#empty()}
     *         if the command or channel suffix is unhandled
     */
    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        if (EntityType.isPrimary(attribute)) {
            return CommandMapper.onParameterizedStringMapped(command, "alarm_control_panel", entityId,
                    (action, arg, params) -> {
                        if (arg != null) {
                            params.put("code", arg);
                        }
                        return switch (action) {
                            case "arm_away", "arm" -> "alarm_arm_away";
                            case "arm_home" -> "alarm_arm_home";
                            case "arm_night" -> "alarm_arm_night";
                            case "arm_vacation" -> "alarm_arm_vacation";
                            case "arm_custom_bypass" -> "alarm_arm_custom_bypass";
                            case "disarm" -> "alarm_disarm";
                            case "trigger" -> "alarm_trigger";
                            default -> null;
                        };
                    });
        }
        return Optional.empty();
    }
}
