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
package org.openhab.binding.dirigera.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link Clip2StateDescriptionProvider} provides dynamic state descriptions of alert, effect, scene, and colour
 * temperature channels whose capabilities are dynamically determined at runtime.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, DirigeraStateDescriptionProvider.class })
public class DirigeraStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {
    private Map<ChannelUID, StateDescriptionFragment> stateDescriptionMap = new HashMap<>();

    @Activate
    public DirigeraStateDescriptionProvider(final @Reference EventPublisher eventPublisher,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry,
            final @Reference ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        StateDescription original = null;
        StateDescriptionFragment fragment = stateDescriptionMap.get(channel.getUID());
        if (fragment != null) {
            original = fragment.toStateDescription();
            StateDescription modified = super.getStateDescription(channel, original, locale);
            if (modified == null) {
                modified = original;
            }
            return modified;
        }
        return super.getStateDescription(channel, original, locale);
    }

    public void setStateDescription(ChannelUID channelUid, StateDescriptionFragment stateDescriptionFragment) {
        StateDescription stateDescription = stateDescriptionFragment.toStateDescription();
        if (stateDescription != null) {
            StateDescriptionFragment old = stateDescriptionMap.get(channelUid);
            stateDescriptionMap.put(channelUid, stateDescriptionFragment);
            Set<String> linkedItems = null;
            ItemChannelLinkRegistry compareRegistry = itemChannelLinkRegistry;
            if (compareRegistry != null) {
                linkedItems = compareRegistry.getLinkedItemNames(channelUid);
            }
            postEvent(ThingEventFactory.createChannelDescriptionChangedEvent(channelUid,
                    linkedItems != null ? linkedItems : Set.of(), stateDescriptionFragment, old));
        }
    }
}
