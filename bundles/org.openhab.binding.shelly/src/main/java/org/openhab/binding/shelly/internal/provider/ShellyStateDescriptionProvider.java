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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.handler.ShellyLightHandler;
import org.openhab.binding.shelly.internal.handler.ShellyLightModel;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.events.ThingEventFactory;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragment;
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
        StateDescriptionFragment fragment = getStateDescriptionFragment(channel, originalStateDescription, locale);
        return fragment != null ? fragment.toStateDescription() : null;
    }

    private @Nullable StateDescriptionFragment getStateDescriptionFragment(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        ChannelTypeUID uid = channel.getChannelTypeUID();
        if (uid == null || originalStateDescription == null) {
            return null;
        }

        ChannelUID channelUID = channel.getUID();
        ThingUID thingUID = channelUID.getThingUID();
        if (!BINDING_ID.equals(thingUID.getBindingId())) {
            return null;
        }

        Thing thing = thingRegistry.get(thingUID);
        if (thing == null) {
            return null;
        }

        ShellyThingInterface handler = (ShellyThingInterface) thing.getHandler();
        if (handler == null) {
            return null;
        }

        StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create(originalStateDescription);

        boolean hasOptions = false;
        List<StateOption> stateOptions = handler.getStateOptions(uid);
        if (stateOptions != null && !stateOptions.isEmpty()) {
            builder.withOptions(stateOptions);
            hasOptions = true;
        }

        boolean hasColorTempRange = false;
        if (CHANNEL_COLOR_TEMP.equals(channelUID.getIdWithoutGroup()) && isColorTempLightChannel(channelUID)
                && handler instanceof ShellyLightHandler lightHandler) {
            ShellyLightModel model = lightHandler.getLightModelByChannelUID(channelUID);
            if (model != null && model.supportsColorTempChannel()) {
                builder.withMinimum(model.getColorTemperatureMinimumKelvin());
                builder.withMaximum(model.getColorTemperatureMaximumKelvin());
                builder.withPattern("%.0f K");
                hasColorTempRange = true;
            }
        }

        return (hasOptions || hasColorTempRange) ? builder.build() : null;
    }

    private boolean isColorTempLightChannel(ChannelUID channelUID) {
        String groupId = channelUID.getGroupId();
        if (groupId == null) {
            return false;
        }
        if (CHANNEL_GROUP_WHITE_CONTROL.equals(groupId)) {
            return true;
        }
        if (groupId.startsWith(CHANNEL_GROUP_LIGHT_INDEX)) {
            return true;
        }
        if (groupId.startsWith(CHANNEL_GROUP_LIGHT_CHANNEL)) {
            return true;
        }
        return false;
    }

    /**
     * Notifies the system that the state description of a channel has changed. This method should be
     * called whenever the state description of a channel is updated, so that the system can refresh
     * the state description and notify any listeners.
     *
     * @param channel The channel whose state description has changed.
     */
    public void notifyStateDescriptionUpdated(Channel channel) {
        ChannelUID channelUID = channel.getUID();
        StateDescriptionFragment fragment = getStateDescriptionFragment(channel, null, null);
        if (fragment != null) {
            ItemChannelLinkRegistry registry = itemChannelLinkRegistry;
            postEvent(ThingEventFactory.createChannelDescriptionChangedEvent(channelUID,
                    registry != null ? registry.getLinkedItemNames(channelUID) : Set.of(), fragment, null));
        }
    }
}
