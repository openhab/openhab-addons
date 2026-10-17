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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * The {@link EventEntity} class represents a stateless event entity type in the Home Assistant binding.
 * Maps event types (e.g. button presses, doorbell clicks) to openHAB String channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class EventEntity implements EntityType {

    @Override
    public String getType() {
        return "event";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryEventChannel() //
                .build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        String eventType = entityState.getAttributeAsString("event_type");
        String event = eventType != null ? eventType : entityState.state();

        State eventState = new StringType(event);

        return StateMapBuilder.create(entityState, context) //
                .putPrimaryState(eventState) //
                .build();
    }
}
