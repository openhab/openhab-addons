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
package org.openhab.binding.smartthings.internal.ocf;

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
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.events.ThingEventFactory;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragment;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Publishes appliance-specific channel choices and limits from authenticated capabilities.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, DynamicCommandDescriptionProvider.class,
        ApplianceDescriptionProvider.class })
public class ApplianceDescriptionProvider extends BaseDynamicCommandDescriptionProvider
        implements DynamicStateDescriptionProvider {
    private final Map<ChannelUID, StateDescriptionFragment> descriptions = new ConcurrentHashMap<>();

    @Activate
    public ApplianceDescriptionProvider(@Reference EventPublisher eventPublisher,
            @Reference ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.eventPublisher = eventPublisher;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    void setDescription(ChannelUID channelUID, StateDescriptionFragment description, List<CommandOption> commands) {
        StateDescriptionFragment previous = descriptions.put(channelUID, description);
        if (!description.equals(previous)) {
            ItemChannelLinkRegistry registry = itemChannelLinkRegistry;
            postEvent(ThingEventFactory.createChannelDescriptionChangedEvent(channelUID,
                    registry == null ? Set.of() : registry.getLinkedItemNames(channelUID), description, previous));
        }
        setCommandOptions(channelUID, commands);
    }

    void retainDescriptions(ThingUID thingUID, Set<ChannelUID> channels) {
        descriptions.keySet().removeIf(uid -> uid.getThingUID().equals(thingUID) && !channels.contains(uid));
        channelOptionsMap.keySet().removeIf(uid -> uid.getThingUID().equals(thingUID) && !channels.contains(uid));
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        StateDescriptionFragment description = descriptions.get(channel.getUID());
        return description == null ? null : description.toStateDescription();
    }

    @Override
    @Deactivate
    public void deactivate() {
        descriptions.clear();
        super.deactivate();
    }
}
