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
package org.openhab.binding.yamahareceiver.internal;

import static java.util.stream.Collectors.toList;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yamahareceiver.internal.handler.YamahaZoneThingHandler;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Provide a custom channel type for the preset channel
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - RX-V3900 compatibility improvements
 */
@Component(scope = ServiceScope.PROTOTYPE, service = { ChannelsTypeProviderPreset.class, ChannelTypeProvider.class })
@NonNullByDefault
public class ChannelsTypeProviderPreset implements ChannelTypeProvider, ThingHandlerService {
    private @Nullable ChannelType channelType;
    private @Nullable ChannelTypeUID channelTypeUID;
    private @Nullable YamahaZoneThingHandler handler;

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        ChannelType channelType = this.channelType;
        return channelType == null ? Set.of() : Set.of(channelType);
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypeUID.equals(this.channelTypeUID)) {
            return channelType;
        } else {
            return null;
        }
    }

    public @Nullable ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }

    private StateDescriptionFragment getDefaultStateDescription() {
        List<StateOption> options = IntStream.rangeClosed(1, 40)
                .mapToObj(i -> new StateOption(Integer.toString(i), "Item_" + i)).collect(toList());
        return StateDescriptionFragmentBuilder.create().withPattern("%s").withReadOnly(false).withOptions(options)
                .build();
    }

    public void changePresetNames(List<PresetInfoState.Preset> presets) {
        List<StateOption> options = presets.stream()
                .map(preset -> new StateOption(String.valueOf(preset.getValue()), preset.getName())).collect(toList());
        createChannelType(StateDescriptionFragmentBuilder.create().withPattern("%s").withReadOnly(false)
                .withOptions(options).build());
    }

    private void createChannelType(StateDescriptionFragment state) {
        ChannelTypeUID channelTypeUID = this.channelTypeUID;
        if (channelTypeUID != null) {
            channelType = ChannelTypeBuilder.state(channelTypeUID, "Preset", "Number")
                    .withDescription("Select a saved channel by its preset number").withStateDescriptionFragment(state)
                    .build();
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (YamahaZoneThingHandler) handler;
        this.handler.channelsTypeProviderPreset = this;
        /**
         * We generate a thing specific channelTypeUID, because presets absolutely depends on the thing.
         */
        channelTypeUID = new ChannelTypeUID(BINDING_ID,
                CHANNEL_PLAYBACK_PRESET_TYPE_NAMED + handler.getThing().getUID().getId());

        createChannelType(getDefaultStateDescription());
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }
}
