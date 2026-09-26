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
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link DateTimeEntity} class represents a datetime entity type in the Home Assistant binding.
 * Maps combined date and time controls to openHAB String channels and handles setting datetime values.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class DateTimeEntity implements EntityType {

    @Override
    public String getType() {
        return "datetime";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.DATETIME) //
                .build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        State dateState = UnDefType.UNDEF;
        String rawDate = entityState.state();
        if (!rawDate.isBlank()) {
            try {
                dateState = DateTimeType.valueOf(rawDate);
            } catch (IllegalArgumentException e) {
                // Update the item to UNDEF on parse error
            }
        }
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryState(dateState) //
                .build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {
        if (EntityType.isPrimary(attribute)) {
            return CommandMapper.onDateTime(command, getType(), "set_value", "value", entityId);
        }
        return Optional.empty();
    }
}
