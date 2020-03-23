/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.DynamicCommandDescriptionProvider;
import org.eclipse.smarthome.core.types.CommandDescription;
import org.eclipse.smarthome.core.types.CommandDescriptionBuilder;
import org.eclipse.smarthome.core.types.CommandOption;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FoxDynamicCommandDescriptionProvider} is responsible for providing commands to channels.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicCommandDescriptionProvider.class, FoxDynamicCommandDescriptionProvider.class })
public class FoxDynamicCommandDescriptionProvider implements DynamicCommandDescriptionProvider {

    private final Map<ChannelUID, @Nullable List<CommandOption>> channelOptionsMap = new ConcurrentHashMap<>();

    /**
     * For a given channel UID, set a {@link List} of {@link CommandOption}s that should be used for the channel,
     * instead of the one defined statically in the {@link ChannelType}.
     *
     * @param channelUID the channel UID of the channel
     * @param options a {@link List} of {@link CommandOption}s
     */
    public void setCommandOptions(ChannelUID channelUID, List<CommandOption> options) {
        channelOptionsMap.put(channelUID, options);
    }

    @Override
    public @Nullable CommandDescription getCommandDescription(Channel channel,
            @Nullable CommandDescription originalCommandDescription, @Nullable Locale locale) {
        List<CommandOption> options = channelOptionsMap.get(channel.getUID());
        if (options == null) {
            return null;
        }

        return CommandDescriptionBuilder.create().withCommandOptions(options).build();
    }
}
