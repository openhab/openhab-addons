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
package org.openhab.binding.amplipi.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.model.Preset;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.BaseDynamicCommandDescriptionProvider;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.CommandOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * This class provides the list of valid commands for the preset channel.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@Component(scope = ServiceScope.PROTOTYPE, service = { PresetCommandOptionProvider.class,
        DynamicCommandDescriptionProvider.class })
@NonNullByDefault
public class PresetCommandOptionProvider extends BaseDynamicCommandDescriptionProvider implements ThingHandlerService {

    private @Nullable AmpliPiHandler handler;

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (AmpliPiHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public @Nullable CommandDescription getCommandDescription(Channel channel,
            @Nullable CommandDescription originalCommandDescription, @Nullable Locale locale) {
        ChannelTypeUID typeUID = channel.getChannelTypeUID();
        List<CommandOption> options = new ArrayList<>();
        if (typeUID != null && AmpliPiBindingConstants.CHANNEL_PRESET.equals(typeUID.getId()) && handler != null) {
            List<Preset> presets = handler.getPresets();
            for (Preset preset : presets) {
                options.add(new CommandOption(preset.getId().toString(), preset.getName()));
            }
            setCommandOptions(channel.getUID(), options);
        }
        return super.getCommandDescription(channel, originalCommandDescription, locale);
    }
}
