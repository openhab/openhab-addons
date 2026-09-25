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
 * The {@link RemoteEntity} class represents a remote control entity type in the Home Assistant binding.
 * Maps remote state to openHAB Switch channels and accepts String commands for send_command calls.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class RemoteEntity implements EntityType {

    private static final long LEARN_COMMAND = 1L;
    private static final long DELETE_COMMAND = 2L;

    @Override
    public String getType() {
        return "remote";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.SWITCH) //
                .add("send_command", ItemType.STRING) //
                .addIfSupported(LEARN_COMMAND, "learn_command", ItemType.SWITCH) //
                .addIfSupported(DELETE_COMMAND, "delete_command", ItemType.SWITCH) //
                .build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryOnOffState().build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String channelSuffix, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        return switch (channelSuffix) {
            case EntityType.PRIMARY_ATTR -> CommandMapper.onOff(command, "remote", entityId);
            case "send_command" -> CommandMapper.onString(command, "remote", "send_command", "command", entityId);
            case "learn_command" -> CommandMapper.onOffCustom(command, "remote", "learn_command", null, entityId);
            case "delete_command" -> CommandMapper.onOffCustom(command, "remote", "delete_command", null, entityId);
            default -> Optional.empty();
        };
    }
}
