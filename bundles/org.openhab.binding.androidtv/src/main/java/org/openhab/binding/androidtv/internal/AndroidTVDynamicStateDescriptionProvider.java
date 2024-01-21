/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
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
 * Dynamic provider of state options while leaving other state description fields as original.
 *
 * @author Benjamin Meyer - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class,
        AndroidTVDynamicStateDescriptionProvider.class }, immediate = true)
@NonNullByDefault
public class AndroidTVDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {
    private final Map<ChannelUID, List<@NonNull StateOption>> channelOptionsMap = new ConcurrentHashMap<>();

    public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
        channelOptionsMap.put(channelUID, options);
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
