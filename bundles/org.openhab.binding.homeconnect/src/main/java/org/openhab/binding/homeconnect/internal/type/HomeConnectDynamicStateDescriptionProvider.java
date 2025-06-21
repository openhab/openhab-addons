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
package org.openhab.binding.homeconnect.internal.type;

import java.util.List;
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
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HomeConnectDynamicStateDescriptionProvider} is responsible for handling dynamic values.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, HomeConnectDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class HomeConnectDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    protected final Map<ChannelUID, Boolean> channelReadOnlyMap = new ConcurrentHashMap<>();

    @Activate
    public HomeConnectDynamicStateDescriptionProvider(final @Reference EventPublisher eventPublisher, //
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry, //
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    /**
     * For a given {@link ChannelUID}, set a readyOnly flag that should be used for the channel, instead of the one
     * defined statically in the {@link ChannelType}.
     *
     * @param channelUID the {@link ChannelUID} of the channel
     * @param readOnly readOnly flag
     */
    public void withReadOnly(ChannelUID channelUID, boolean readOnly) {
        Boolean oldReadOnly = channelReadOnlyMap.get(channelUID);
        if (oldReadOnly == null || oldReadOnly != readOnly) {
            channelReadOnlyMap.put(channelUID, readOnly);
            postEvent(ThingEventFactory.createChannelDescriptionChangedEvent(channelUID,
                    itemChannelLinkRegistry != null ? itemChannelLinkRegistry.getLinkedItemNames(channelUID) : Set.of(),
                    StateDescriptionFragmentBuilder.create().withReadOnly(readOnly).build(), null));
        }
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        // can be overridden by subclasses
        ChannelUID channelUID = channel.getUID();
        String pattern = channelPatternMap.get(channelUID);
        List<StateOption> options = channelOptionsMap.get(channelUID);
        Boolean readOnly = channelReadOnlyMap.get(channelUID);
        if (pattern == null && options == null && readOnly == null) {
            return null;
        }

        StateDescriptionFragmentBuilder builder = (original == null) ? StateDescriptionFragmentBuilder.create()
                : StateDescriptionFragmentBuilder.create(original);

        if (pattern != null) {
            String localizedPattern = localizeStatePattern(pattern, channel, locale);
            if (localizedPattern != null) {
                builder.withPattern(localizedPattern);
            }
        }

        if (options != null) {
            builder.withOptions(localizedStateOptions(options, channel, locale));
        }

        if (readOnly != null) {
            builder.withReadOnly(readOnly);
        }

        return builder.build().toStateDescription();
    }
}
