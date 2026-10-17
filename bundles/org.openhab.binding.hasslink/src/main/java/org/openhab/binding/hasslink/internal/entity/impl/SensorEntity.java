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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.OptionUtils;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

/**
 * The {@link SensorEntity} class represents a sensor entity type in the Home Assistant binding.
 * It provides methods to build channels and update states specific to sensor devices.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class SensorEntity implements EntityType {

    @Override
    public String getType() {
        return "sensor";
    }

    @Override
    public boolean appendsDomainToLabel() {
        return false;
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryInferredChannel(AutoUpdatePolicy.VETO) //
                .addGenericAttributes() //
                .build();
    }

    @Override
    public @Nullable StateDescriptionFragment getStateDescriptionFragment(EntityState entityState, String attribute,
            EntityContext context) {

        StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create().withReadOnly(true);

        if (EntityType.isPrimary(attribute) && entityState.hasAttribute("options")) {
            StateDescriptionFragment optionsFragment = OptionUtils.extractStateOptions(entityState, "options");
            List<StateOption> options = optionsFragment != null ? optionsFragment.getOptions() : null;

            if (options != null && !options.isEmpty()) {
                builder.withOptions(options);
            }
        }

        return builder.build();
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        return StateMapBuilder.create(entityState, context) //
                .putPrimaryInferredState() //
                .putGenericAttributes() //
                .build();
    }
}
