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
package org.openhab.binding.atv.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Supplies runtime state options for Apple TV channels (such as the launchable app list and the
 * available user accounts) while leaving all other state description fields untouched.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, AtvStateDescriptionProvider.class }, immediate = true)
@NonNullByDefault
public class AtvStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private final Map<ChannelUID, List<StateOption>> channelOptionsMap = new ConcurrentHashMap<>();

    /**
     * Sets the state options for a channel, replacing any options previously supplied.
     *
     * @param channelUID channel to provide options for
     * @param options options to expose
     */
    public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
        channelOptionsMap.put(channelUID, options);
    }

    /**
     * Removes any state options previously supplied for a channel.
     *
     * @param channelUID channel to clear options for
     */
    public void removeStateOptions(ChannelUID channelUID) {
        channelOptionsMap.remove(channelUID);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        List<StateOption> options = channelOptionsMap.get(channel.getUID());
        if (options == null) {
            return null;
        }
        if (original != null) {
            return StateDescriptionFragmentBuilder.create(original).withOptions(options).build().toStateDescription();
        }
        return StateDescriptionFragmentBuilder.create().withOptions(options).build().toStateDescription();
    }

    @Deactivate
    public void deactivate() {
        channelOptionsMap.clear();
    }
}
