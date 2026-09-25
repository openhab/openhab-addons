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
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.library.unit.Units;

/**
 * The {@link GeolocationEntity} class represents a {@code geo_location} entity type in Home Assistant.
 * It provides channels for event state, location coordinates, geographical distance, and event source metadata.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class GeolocationEntity implements EntityType {

    @Override
    public String getType() {
        return "geo_location";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        ChannelSpecsBuilder builder = ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.number("Distance"));

        builder.addAttr("source", ItemType.STRING);

        if (entityState.hasAttribute("latitude") && entityState.hasAttribute("longitude")) {
            builder.add("location", ItemType.LOCATION);
        } else {
            builder.addAttr("latitude", ItemType.number("Angle")) //
                    .addAttr("longitude", ItemType.number("Angle"));
        }

        return builder.build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        StateMapBuilder builder = StateMapBuilder.create(entityState, context) //
                .putPrimaryNumeric() //
                .putString("source");

        if (entityState.hasAttribute("latitude") && entityState.hasAttribute("longitude")) {
            builder.putLocation("location");
        } else {
            builder.putQuantity("latitude", Units.DEGREE_ANGLE) //
                    .putQuantity("longitude", Units.DEGREE_ANGLE);
        }
        return builder.build();
    }
}
