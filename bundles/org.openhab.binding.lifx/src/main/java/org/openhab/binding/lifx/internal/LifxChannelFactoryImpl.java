/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal;

import static org.openhab.binding.lifx.internal.LifxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link LifxChannelFactoryImpl} creates dynamic LIFX channels.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
@Component(service = LifxChannelFactory.class)
public class LifxChannelFactoryImpl implements LifxChannelFactory {

    @Override
    public Channel createAbsTemperatureZoneChannel(ThingUID thingUID, int index) {
        return ChannelBuilder
                .create(new ChannelUID(thingUID, CHANNEL_ABS_TEMPERATURE_ZONE + index), CoreItemFactory.NUMBER)
                .withType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_COLOR_TEMPERATURE_ABS).build();
    }

    @Override
    public Channel createColorZoneChannel(ThingUID thingUID, int index) {
        return ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_COLOR_ZONE + index), CoreItemFactory.COLOR)
                .withType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_COLOR).build();
    }

    @Override
    public Channel createTemperatureZoneChannel(ThingUID thingUID, int index) {
        return ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_TEMPERATURE_ZONE + index), CoreItemFactory.DIMMER)
                .withType(DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_COLOR_TEMPERATURE).build();
    }
}
