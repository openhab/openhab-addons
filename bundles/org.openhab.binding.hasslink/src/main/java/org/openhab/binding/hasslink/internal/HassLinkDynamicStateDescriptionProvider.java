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
package org.openhab.binding.hasslink.internal;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
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
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides dynamic state descriptions and state options for HassLink channels.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, HassLinkDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class HassLinkDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    private final Map<ChannelUID, StateDescriptionRange> channelRangeMap = new ConcurrentHashMap<>();

    @Activate
    public HassLinkDynamicStateDescriptionProvider(final @Reference EventPublisher eventPublisher,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry,
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    /**
     * Sets or updates the min, max, and step range for a given channel.
     * If all bounds are null, the range is removed.
     *
     * @param channelUID the channel identifier
     * @param min the minimum value constraint
     * @param max the maximum value constraint
     * @param step the step increment
     */
    public void setRange(ChannelUID channelUID, @Nullable BigDecimal min, @Nullable BigDecimal max,
            @Nullable BigDecimal step) {
        if (min == null && max == null && step == null) {
            removeRange(channelUID);
            return;
        }

        StateDescriptionRange newRange = new StateDescriptionRange(min, max, step);
        StateDescriptionRange oldRange = channelRangeMap.put(channelUID, newRange);

        if (!Objects.equals(oldRange, newRange)) {
            notifyRangeChanged(channelUID, oldRange, newRange);
        }
    }

    /**
     * Removes the stored dynamic range for a given channel if one exists.
     *
     * @param channelUID the channel identifier
     */
    public void removeRange(ChannelUID channelUID) {
        StateDescriptionRange oldRange = channelRangeMap.remove(channelUID);
        if (oldRange != null) {
            notifyRangeChanged(channelUID, oldRange, null);
        }
    }

    private void notifyRangeChanged(ChannelUID channelUID, @Nullable StateDescriptionRange oldRange,
            @Nullable StateDescriptionRange newRange) {
        ItemChannelLinkRegistry registry = itemChannelLinkRegistry;
        Set<String> linkedItemNames = registry != null ? registry.getLinkedItemNames(channelUID) : Set.of();

        StateDescriptionFragment oldStateDescriptionFragment = buildStateDescriptionFragment(oldRange, null);
        StateDescriptionFragment newStateDescriptionFragment = buildStateDescriptionFragment(newRange, null);

        postEvent(ThingEventFactory.createChannelDescriptionChangedEvent(channelUID, linkedItemNames,
                newStateDescriptionFragment, oldStateDescriptionFragment));
    }

    private StateDescriptionFragment buildStateDescriptionFragment(@Nullable StateDescriptionRange range,
            @Nullable StateDescription baseDescription) {
        StateDescriptionFragmentBuilder builder = baseDescription == null ? StateDescriptionFragmentBuilder.create()
                : StateDescriptionFragmentBuilder.create(baseDescription);

        if (range != null) {
            BigDecimal min = range.min();
            BigDecimal max = range.max();
            BigDecimal step = range.step();
            if (min != null) {
                builder.withMinimum(min);
            }
            if (max != null) {
                builder.withMaximum(max);
            }
            if (step != null) {
                builder.withStep(step);
            }
        }
        return builder.build();
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        StateDescription stateDescription = super.getStateDescription(channel, original, locale);
        StateDescriptionRange range = channelRangeMap.get(channel.getUID());

        if (range != null) {
            return buildStateDescriptionFragment(range, stateDescription).toStateDescription();
        }
        return stateDescription;
    }

    /**
     * Clean up the internal map entries for a channel when it is removed.
     *
     * @param channelUID the UID of the channel that has been removed
     */
    public void removeChannel(ChannelUID channelUID) {
        channelPatternMap.remove(channelUID);
        channelOptionsMap.remove(channelUID);
        channelRangeMap.remove(channelUID);
    }

    public void removeThing(ThingUID thingUID) {
        channelPatternMap.keySet().removeIf(channelUID -> channelUID.getThingUID().equals(thingUID));
        channelOptionsMap.keySet().removeIf(channelUID -> channelUID.getThingUID().equals(thingUID));
        channelRangeMap.keySet().removeIf(channelUID -> channelUID.getThingUID().equals(thingUID));
    }

    @Override
    @Deactivate
    public void deactivate() {
        super.deactivate();
        channelRangeMap.clear();
    }
}
