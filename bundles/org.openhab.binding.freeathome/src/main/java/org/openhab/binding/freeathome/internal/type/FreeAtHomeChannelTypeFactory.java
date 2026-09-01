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
package org.openhab.binding.freeathome.internal.type;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapointGroup;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeGeneralException;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * Builds the channel type a datapoint group is represented by, without a configuration description URI so that the type
 * stays resolvable after a restore from storage.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public final class FreeAtHomeChannelTypeFactory {

    private FreeAtHomeChannelTypeFactory() {
    }

    public static ChannelType createChannelType(FreeAtHomeDatapointGroup dpg, ChannelTypeUID channelTypeUID)
            throws FreeAtHomeGeneralException {
        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        stateFragment.withReadOnly(dpg.isReadOnly());
        stateFragment.withPattern(dpg.getTypePattern());

        if (dpg.isDecimal() || dpg.isInteger()) {
            BigDecimal min = new BigDecimal(dpg.getMin());
            BigDecimal max = new BigDecimal(dpg.getMax());
            stateFragment.withMinimum(min).withMaximum(max);
        }

        ChannelTypeBuilder<?> channelTypeBuilder = ChannelTypeBuilder
                .state(channelTypeUID,
                        String.format("%s-%s-%s-%s", dpg.getLabel(), dpg.getOpenHabItemType(), dpg.getOpenHabCategory(),
                                "type"),
                        dpg.getOpenHabItemType())
                .withCategory(dpg.getOpenHabCategory()).withStateDescriptionFragment(stateFragment.build());

        return channelTypeBuilder.isAdvanced(false)
                .withDescription(String.format("Type for channel - %s ", dpg.getLabel())).build();
    }
}
