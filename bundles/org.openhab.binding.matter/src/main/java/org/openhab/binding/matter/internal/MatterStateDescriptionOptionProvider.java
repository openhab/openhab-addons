/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.events.ThingEventFactory;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Dynamic provider of state options
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, MatterStateDescriptionOptionProvider.class })
@NonNullByDefault
public class MatterStateDescriptionOptionProvider extends BaseDynamicStateDescriptionProvider {

    private final Map<ChannelUID, StateDescriptionFragment> stateDescriptionFragments = new ConcurrentHashMap<>();

    @Activate
    public MatterStateDescriptionOptionProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        StateDescriptionFragment stateDescriptionFragment = stateDescriptionFragments.get(channel.getUID());
        return stateDescriptionFragment != null ? stateDescriptionFragment.toStateDescription()
                : super.getStateDescription(channel, original, locale);
    }

    public void setMinMax(ChannelUID channelUID, BigDecimal min, BigDecimal max, @Nullable BigDecimal step,
            @Nullable String pattern) {
        StateDescriptionFragment oldStateDescriptionFragment = stateDescriptionFragments.get(channelUID);
        StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create().withMinimum(min)
                .withMaximum(max);
        if (step != null) {
            builder = builder.withStep(step);
        }
        if (pattern != null) {
            builder = builder.withPattern(pattern);
        }
        StateDescriptionFragment newStateDescriptionFragment = builder.build();
        if (!newStateDescriptionFragment.equals(oldStateDescriptionFragment)) {
            stateDescriptionFragments.put(channelUID, newStateDescriptionFragment);
            ItemChannelLinkRegistry itemChannelLinkRegistry = this.itemChannelLinkRegistry;
            postEvent(ThingEventFactory.createChannelDescriptionChangedEvent(channelUID,
                    itemChannelLinkRegistry != null ? itemChannelLinkRegistry.getLinkedItemNames(channelUID) : Set.of(),
                    newStateDescriptionFragment, oldStateDescriptionFragment));
        }
    }
}
