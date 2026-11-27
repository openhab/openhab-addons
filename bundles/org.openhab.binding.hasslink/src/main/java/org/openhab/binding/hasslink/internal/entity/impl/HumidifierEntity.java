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
import org.openhab.binding.hasslink.internal.entity.util.OptionUtils;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * Entity type handler for Home Assistant {@code humidifier} entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HumidifierEntity implements EntityType {

    /** Bitmask feature flag for supported modes. */
    private static final int FEATURE_MODES = 1;

    @Override
    public String getType() {
        return "humidifier";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.SWITCH) //
                .addAttr("humidity", ItemType.DIMENSIONLESS) //
                .addAttr("current_humidity", ItemType.DIMENSIONLESS) //
                .addAttrIfSupported(FEATURE_MODES, "mode", ItemType.STRING) //
                .build();
    }

    @Override
    public @Nullable StateDescriptionFragment getStateDescriptionFragment( //
            EntityState entityState, //
            String attribute, //
            EntityContext context) {

        return switch (attribute) {
            case "humidity" ->
                OptionUtils.extractRange(entityState, "min_humidity", "max_humidity", "target_humidity_step");
            case "mode" -> OptionUtils.extractStateOptions(entityState, "available_modes");
            default -> null;
        };
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryOnOffState() //
                .putQuantity("humidity", Units.PERCENT) //
                .putQuantity("current_humidity", Units.PERCENT) //
                .putString("mode") //
                .build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {

        return switch (attribute) {
            case PRIMARY_ATTR -> CommandMapper.onOff(command, "humidifier", entityId);

            case "humidity" ->
                CommandMapper.onQuantity(command, "%", "humidifier", "set_humidity", "humidity", entityId)
                        .or(() -> CommandMapper.onDecimal(command, "humidifier", "set_humidity", "humidity", entityId));

            case "mode" -> CommandMapper.onString(command, "humidifier", "set_mode", "mode", entityId);

            default -> Optional.empty();
        };
    }
}
