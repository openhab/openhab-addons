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
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link DeviceTrackerEntity} class represents a device tracker entity type in the Home Assistant binding.
 * Maps presence state (home/not_home) to openHAB Switch and String channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class DeviceTrackerEntity implements EntityType {

    @Override
    public String getType() {
        return "device_tracker";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        ChannelSpecsBuilder builder = ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING);

        // GPS Tracker Attributes
        if (entityState.hasAttribute("latitude") && entityState.hasAttribute("longitude")) {
            builder.add("location", ItemType.LOCATION) //
                    .add("latitude", ItemType.NUMBER) //
                    .add("longitude", ItemType.NUMBER);
        }

        builder.addAttr("gps_accuracy", ItemType.number("Length")) //
                .addAttr("battery", ItemType.DIMENSIONLESS) //

                // Network Scanner Attributes
                .addAttr("mac", ItemType.STRING) //
                .addAttr("ip", ItemType.STRING);

        if (entityState.hasAttribute("host_name") || entityState.hasAttribute("hostname")) {
            builder.add("host_name", ItemType.STRING);
        }

        builder.addAttr("source_type", ItemType.STRING);
        return builder.build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        // Hostname fallback check (host_name -> hostname)
        String hostName = entityState.getAttributeAsString("host_name");
        if (hostName == null) {
            hostName = entityState.getAttributeAsString("hostname");
        }

        return StateMapBuilder.create(entityState, context) //
                .putPrimaryStringState() //

                // GPS Location Parsing
                .putDecimal("latitude") //
                .putDecimal("longitude") //
                .putLocation("location") //

                .putQuantity("gps_accuracy", SIUnits.METRE) //
                .putQuantity("battery", Units.PERCENT) //

                // Network Scanner Parsing
                .putString("mac") //
                .putString("ip") //
                .putStringValue("host_name", hostName) //
                .putString("source_type") //
                .build();
    }
}
