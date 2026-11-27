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
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.UnDefType;

/**
 * The {@link LockEntity} class represents a lock entity type in the Home Assistant binding.
 * Maps Home Assistant lock entities to openHAB Switch and String channels and handles lock/unlock service calls.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class LockEntity implements EntityType {

    public static final long OPEN = 1L; // feature flag

    @Override
    public String getType() {
        return "lock";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING) //
                .add("locked", ItemType.SWITCH) //
                .build();
    }

    @Override
    public @Nullable List<CommandOption> getCommandOptions(EntityState entityState, String attribute,
            EntityContext context) {
        if (EntityType.isPrimary(attribute)) {
            CommandOption lockOption = new CommandOption("lock", "Lock");
            CommandOption unlockOption = new CommandOption("unlock", "Unlock");
            if (entityState.isSupportedFeature(OPEN)) {
                return List.of(lockOption, unlockOption, new CommandOption("open", "Open"));
            }
            return List.of(lockOption, unlockOption);
        }
        return null;
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        if (entityState.isUnavailableOrUnknown()) {
            return StateMapBuilder.create(entityState, context) //
                    .putPrimaryState(UnDefType.UNDEF) //
                    .put("locked", UnDefType.UNDEF) //
                    .build();
        }

        OnOffType lockState = OnOffType.from("locked".equalsIgnoreCase(entityState.state()));
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //
                .put("locked", lockState) //
                .build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        return switch (attribute) {
            case EntityType.PRIMARY_ATTR ->
                CommandMapper.onParameterizedStringMapped(command, getType(), entityId, (action, arg, params) -> {
                    if (arg != null) {
                        params.put("code", arg);
                    }
                    return switch (action) {
                        case "lock", "locked" -> "lock";
                        case "unlock", "unlocked" -> "unlock";
                        case "open" -> "open";
                        default -> null;
                    };
                });
            case "locked" -> CommandMapper.onOffCustom(command, getType(), "lock", "unlock", entityId);
            default -> Optional.empty();
        };
    }
}
