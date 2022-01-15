/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.events.ThingEventFactory;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragment;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic channel state description provider.
 * Overrides the state description for the controls, which receive its configuration in the runtime.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, DeconzDynamicStateDescriptionProvider.class })
public class DeconzDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {
    private final Logger logger = LoggerFactory.getLogger(DeconzDynamicStateDescriptionProvider.class);

    private final Map<ChannelUID, StateDescriptionFragment> stateDescriptionFragments = new ConcurrentHashMap<>();

    /**
     * Set a state description for a channel. This description will be used when preparing the channel state by
     * the framework for presentation. A previous description, if existed, will be replaced.
     *
     * @param channelUID
     *            channel UID
     * @param stateDescriptionFragment
     *            state description for the channel
     */
    public void setDescriptionFragment(ChannelUID channelUID, StateDescriptionFragment stateDescriptionFragment) {
        StateDescriptionFragment oldStateDescriptionFragment = stateDescriptionFragments.get(channelUID);
        if (!stateDescriptionFragment.equals(oldStateDescriptionFragment)) {
            logger.trace("adding state description for channel {}", channelUID);
            stateDescriptionFragments.put(channelUID, stateDescriptionFragment);
            postEvent(ThingEventFactory.createChannelDescriptionChangedEvent(channelUID,
                    itemChannelLinkRegistry != null ? itemChannelLinkRegistry.getLinkedItemNames(channelUID) : Set.of(),
                    stateDescriptionFragment, oldStateDescriptionFragment));
        }
    }

    /**
     * remove all descriptions for a given thing
     *
     * @param thingUID the thing's UID
     */
    public void removeDescriptionsForThing(ThingUID thingUID) {
        logger.trace("removing state description for thing {}", thingUID);
        stateDescriptionFragments.entrySet().removeIf(entry -> entry.getKey().getThingUID().equals(thingUID));
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        StateDescriptionFragment stateDescriptionFragment = stateDescriptionFragments.get(channel.getUID());
        if (stateDescriptionFragment != null) {
            logger.trace("returning new stateDescription for {}", channel.getUID());
            return stateDescriptionFragment.toStateDescription();
        } else {
            return super.getStateDescription(channel, originalStateDescription, locale);
        }
    }
}
