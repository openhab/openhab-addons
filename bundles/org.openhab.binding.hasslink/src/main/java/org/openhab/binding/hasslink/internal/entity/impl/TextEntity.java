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
 * The {@link TextEntity} class represents a text input entity type in the Home Assistant binding.
 * Maps Home Assistant text input fields to openHAB String channels and handles updating text values.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class TextEntity implements EntityType {

    @Override
    public String getType() {
        return "text";
    }

    @Override
    public boolean appendsDomainToLabel() {
        return false;
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
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        if (EntityType.isPrimary(attribute)) {
            return CommandMapper.onString(command, getType(), "set_value", "value", entityId);
        }
        return Optional.empty();
    }
}
