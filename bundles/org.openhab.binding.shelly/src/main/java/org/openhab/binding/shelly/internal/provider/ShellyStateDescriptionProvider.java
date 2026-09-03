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
package org.openhab.binding.shelly.internal.provider;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.handler.ShellyLightHandler;
import org.openhab.binding.shelly.internal.handler.ShellyLightModel;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class provides the list of valid inputs for the input channel of a source.
 *
 * @author Markus Michels - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, ShellyStateDescriptionProvider.class })
public class ShellyStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {
    private final ThingRegistry thingRegistry;

    @Activate
    public ShellyStateDescriptionProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService,
            @Reference ThingRegistry thingRegistry) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
        this.thingRegistry = thingRegistry;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        ChannelTypeUID uid = channel.getChannelTypeUID();
        if (uid == null || !BINDING_ID.equals(uid.getBindingId()) || originalStateDescription == null) {
            return null;
        }

        Thing thing = thingRegistry.get(channel.getUID().getThingUID());
        if (thing == null) {
            return null;
        }

        ShellyThingInterface handler = (ShellyThingInterface) thing.getHandler();
        if (handler == null) {
            return null;
        }

        List<StateOption> stateOptions = handler.getStateOptions(uid);
        boolean hasOptions = stateOptions != null && !stateOptions.isEmpty();

        boolean hasColorTempRange = false;
        BigDecimal minKelvin = null;
        BigDecimal maxKelvin = null;

        if (CHANNEL_COLOR_TEMP_ABS.equals(channel.getUID().getIdWithoutGroup())
                && handler instanceof ShellyLightHandler lightHandler
                && lightHandler.getLightModelByChannel(channel) instanceof ShellyLightModel model
                && model.supportsColorTempChannel(true)) {
            minKelvin = model.getColorTemperatureMinimumKelvin();
            maxKelvin = model.getColorTemperatureMaximumKelvin();
            hasColorTempRange = true;
        }

        if (!hasOptions && !hasColorTempRange) {
            return null;
        }

        StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create(originalStateDescription);

        if (hasOptions) {
            builder = builder.withOptions(Objects.requireNonNull(stateOptions));
        }

        if (hasColorTempRange) {
            builder = builder.withMinimum(Objects.requireNonNull(minKelvin));
            builder = builder.withMaximum(Objects.requireNonNull(maxKelvin));
            builder = builder.withPattern("%.0f K");
        }

        return builder.build().toStateDescription();
    }
}
