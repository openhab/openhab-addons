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
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Supplies runtime options for Apple TV channels (such as the launchable app list and the available
 * user accounts) while leaving all other description fields untouched.
 *
 * <p>
 * Both a command description and a state description are provided for the same channels: the command
 * options let a UI offer the list as sendable commands (launch an app, switch an account), while the
 * matching state options give the current value a friendly display label.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, DynamicCommandDescriptionProvider.class,
        AtvStateDescriptionProvider.class }, immediate = true)
@NonNullByDefault
public class AtvStateDescriptionProvider implements DynamicStateDescriptionProvider, DynamicCommandDescriptionProvider {

    private final Map<ChannelUID, List<StateOption>> channelStateOptionsMap = new ConcurrentHashMap<>();
    private final Map<ChannelUID, List<CommandOption>> channelCommandOptionsMap = new ConcurrentHashMap<>();

    /**
     * Sets the state options for a channel, replacing any options previously supplied.
     *
     * @param channelUID channel to provide options for
     * @param options options to expose
     */
    public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
        channelStateOptionsMap.put(channelUID, options);
    }

    /**
     * Removes any state options previously supplied for a channel.
     *
     * @param channelUID channel to clear options for
     */
    public void removeStateOptions(ChannelUID channelUID) {
        channelStateOptionsMap.remove(channelUID);
    }

    /**
     * Sets the command options for a channel, replacing any options previously supplied.
     *
     * @param channelUID channel to provide options for
     * @param options options a UI may send as commands
     */
    public void setCommandOptions(ChannelUID channelUID, List<CommandOption> options) {
        channelCommandOptionsMap.put(channelUID, options);
    }

    /**
     * Removes any command options previously supplied for a channel.
     *
     * @param channelUID channel to clear options for
     */
    public void removeCommandOptions(ChannelUID channelUID) {
        channelCommandOptionsMap.remove(channelUID);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        List<StateOption> options = channelStateOptionsMap.get(channel.getUID());
        if (options == null) {
            return null;
        }
        if (original != null) {
            return StateDescriptionFragmentBuilder.create(original).withOptions(options).build().toStateDescription();
        }
        return StateDescriptionFragmentBuilder.create().withOptions(options).build().toStateDescription();
    }

    @Override
    public @Nullable CommandDescription getCommandDescription(Channel channel, @Nullable CommandDescription original,
            @Nullable Locale locale) {
        List<CommandOption> options = channelCommandOptionsMap.get(channel.getUID());
        if (options == null) {
            return null;
        }
        return CommandDescriptionBuilder.create().withCommandOptions(options).build();
    }

    @Deactivate
    public void deactivate() {
        channelStateOptionsMap.clear();
        channelCommandOptionsMap.clear();
    }
}
