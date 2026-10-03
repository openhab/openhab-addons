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
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.CommandMapper;
import org.openhab.core.types.Command;

/**
 * The {@link ButtonEntity} class represents a button entity type in the Home Assistant binding.
 * It provides methods to build channels and update states specific to buttons.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ButtonEntity implements EntityType {

    public static final String BUTTON_PRESS_COMMAND = "PRESS";

    @Override
    public String getType() {
        return "button";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryActionChannel("Send PRESS command to activate this button") //
                .build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        // Button entities are stateless: we don't report any state back to openHAB
        return Map.of();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        return switch (attribute) {
            case PRIMARY_ATTR -> CommandMapper.onStringMapped(command, "button", entityId,
                    cmd -> BUTTON_PRESS_COMMAND.equalsIgnoreCase(cmd) ? "press" : null);
            default -> Optional.empty();
        };
    }
}
