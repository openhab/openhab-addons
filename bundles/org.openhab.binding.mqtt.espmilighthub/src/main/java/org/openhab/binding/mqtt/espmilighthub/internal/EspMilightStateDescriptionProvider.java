/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.espmilighthub.internal;

import static org.openhab.binding.mqtt.espmilighthub.internal.EspMilightHubBindingConstants.*;

import java.math.BigDecimal;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

/**
 * Provides custom state descriptions for system.color-temperature-absolute channels.
 *
 * @author Cody Cutrer - Initial contribution
 */
@Component(service = DynamicStateDescriptionProvider.class)
@NonNullByDefault
public class EspMilightStateDescriptionProvider implements DynamicStateDescriptionProvider {
    private final ThingRegistry thingRegistry;

    @Activate
    public EspMilightStateDescriptionProvider(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        var logger = LoggerFactory.getLogger(EspMilightStateDescriptionProvider.class);
        var channelUID = channel.getUID();
        var thing = thingRegistry.get(channelUID.getThingUID());
        if (thing == null || !SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())
                || !channelUID.getId().equals(CHANNEL_COLOURTEMP_ABS)) {
            return null;
        }

        StateDescriptionFragmentBuilder builder;
        if (original != null) {
            builder = StateDescriptionFragmentBuilder.create(original);
        } else {
            builder = StateDescriptionFragmentBuilder.create();
        }
        builder.withMinimum(BIG_DECIMAL_153).withMaximum(BIG_DECIMAL_370).withStep(BigDecimal.ONE)
                .withPattern("%d mired");
        return builder.build().toStateDescription();
    }
}
