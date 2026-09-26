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
package org.openhab.binding.vesync.internal;

import static org.openhab.binding.vesync.internal.VeSyncConstants.*;
import static org.openhab.binding.vesync.internal.handlers.VeSyncDeviceAirPurifierHandler.*;

import java.math.BigDecimal;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vesync.internal.handlers.VeSyncDevicePurifierMetadata;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides the supported fan-speed range for each air purifier model.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, VeSyncStateDescriptionProvider.class })
public class VeSyncStateDescriptionProvider implements DynamicStateDescriptionProvider {
    private final ThingRegistry thingRegistry;

    @Activate
    public VeSyncStateDescriptionProvider(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        var channelUID = channel.getUID();
        Thing thing = thingRegistry.get(channelUID.getThingUID());
        if (thing == null || !THING_TYPE_AIR_PURIFIER.equals(thing.getThingTypeUID())
                || !DEVICE_CHANNEL_FAN_SPEED_ENABLED.equals(channelUID.getIdWithoutGroup())) {
            return null;
        }

        String deviceFamily = thing.getProperties().get(DEVICE_PROP_DEVICE_FAMILY);
        VeSyncDevicePurifierMetadata metadata = DEV_FAMILY_PURIFIER_MAP.get(deviceFamily);
        if (metadata == null) {
            return null;
        }

        StateDescriptionFragmentBuilder builder = original == null ? StateDescriptionFragmentBuilder.create()
                : StateDescriptionFragmentBuilder.create(original);
        return builder.withMinimum(BigDecimal.valueOf(metadata.minFanSpeed))
                .withMaximum(BigDecimal.valueOf(metadata.maxFanSpeed)).withStep(BigDecimal.ONE).withReadOnly(false)
                .build().toStateDescription();
    }
}
