/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.yamahareceiver.internal.handler.YamahaZoneThingHandler;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;

/**
 * Provide a custom channel type for the preset channel
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - RX-V3900 compatibility improvements
 */
@NonNullByDefault
public class ChannelsTypeProviderPreset implements ChannelTypeProvider, ThingHandlerService {
    private @NonNullByDefault({}) ChannelType channelType;
    private @NonNullByDefault({}) ChannelTypeUID channelTypeUID;
    private @NonNullByDefault({}) YamahaZoneThingHandler handler;

    @Override
    public @Nullable Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return Collections.singleton(channelType);
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (this.channelTypeUID.equals(channelTypeUID)) {
            return channelType;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        return null;
    }

    @Override
    public @Nullable Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return null;
    }

    public ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }

    private StateDescription getDefaultStateDescription() {
        List<StateOption> options = IntStream.rangeClosed(1, 40)
                .mapToObj(i -> new StateOption(Integer.toString(i), "Item_" + i)).collect(toList());

        StateDescription state = new StateDescription(null, null, null, "%s", false, options);
        return state;
    }

    public void changePresetNames(List<PresetInfoState.Preset> presets) {
        List<StateOption> options = presets.stream()
                .map(preset -> new StateOption(String.valueOf(preset.getValue()), preset.getName())).collect(toList());

        StateDescription state = new StateDescription(null, null, null, "%s", false, options);
        createChannelType(state);
    }

    private void createChannelType(StateDescription state) {
        channelType = ChannelTypeBuilder.state(channelTypeUID, "Preset", "Number")
                .withDescription("Select a saved channel by its preset number").withStateDescription(state).build();
    }

    @NonNullByDefault({})
    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (YamahaZoneThingHandler) handler;
        this.handler.channelsTypeProviderPreset = this;
        /**
         * We generate a thing specific channelTypeUID, because presets absolutely depends on the thing.
         */
        channelTypeUID = new ChannelTypeUID(BINDING_ID,
                CHANNEL_PLAYBACK_PRESET_TYPE_NAMED + handler.getThing().getUID().getId());

        StateDescription state = getDefaultStateDescription();
        createChannelType(state);
    }

    @Override
    public ThingHandler getThingHandler() {
        return this.handler;
    }

}
