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
package org.openhab.binding.homeconnectdirect.internal.provider;

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
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.events.ThingEventFactory;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragment;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HomeConnectDirectDynamicStateDescriptionProvider} is responsible for handling dynamic values.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, HomeConnectDirectDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class HomeConnectDirectDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    protected final Map<ChannelUID, StateDescriptionFragment> stateDescriptionFragmentMap;

    @Activate
    public HomeConnectDirectDynamicStateDescriptionProvider(@Reference EventPublisher eventPublisher,
            @Reference ItemChannelLinkRegistry itemChannelLinkRegistry,
            @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
        this.stateDescriptionFragmentMap = new ConcurrentHashMap<>();
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        if (stateDescriptionFragmentMap.containsKey(channel.getUID())) {
            return Objects.requireNonNull(stateDescriptionFragmentMap.get(channel.getUID())).toStateDescription();
        } else {
            return super.getStateDescription(channel, original, locale);
        }
    }

    public void setStateDescriptionFragment(ChannelUID channelUID, StateDescriptionFragment stateDescriptionFragment) {
        var oldStateDescriptionFragment = stateDescriptionFragmentMap.get(channelUID);
        var itemChannelLinkRegistry = this.itemChannelLinkRegistry;

        if (!stateDescriptionFragment.equals(oldStateDescriptionFragment)) {
            stateDescriptionFragmentMap.put(channelUID, stateDescriptionFragment);
            postEvent(ThingEventFactory.createChannelDescriptionChangedEvent(channelUID,
                    itemChannelLinkRegistry != null ? itemChannelLinkRegistry.getLinkedItemNames(channelUID) : Set.of(),
                    stateDescriptionFragment, oldStateDescriptionFragment));
        }
    }

    @Override
    @Deactivate
    public void deactivate() {
        stateDescriptionFragmentMap.clear();
        super.deactivate();
    }
}
