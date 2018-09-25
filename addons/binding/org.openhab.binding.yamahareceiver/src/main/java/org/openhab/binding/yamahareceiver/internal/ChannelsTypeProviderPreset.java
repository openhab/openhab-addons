/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal;

import static java.util.stream.Collectors.toList;
import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;

/**
 * Provide a custom channel type for the preset channel
 *
 * @author David Graeff - Initial controbution
 * @author Tomasz Maruszak - RX-V3900 compatibility improvements
 */
public class ChannelsTypeProviderPreset implements ChannelTypeProvider {
    private ChannelType channelType;
    private final ChannelTypeUID channelTypeUID;

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return Collections.singleton(channelType);
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        if (this.channelTypeUID.equals(channelTypeUID)) {
            return channelType;
        } else {
            return null;
        }
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return null;
    }

    public ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }

    public ChannelsTypeProviderPreset(ThingUID thing) {
        /**
         * We generate a thing specific channelTypeUID, because presets absolutely depends on the thing.
         */
        channelTypeUID = new ChannelTypeUID(BINDING_ID, CHANNEL_PLAYBACK_PRESET_TYPE_NAMED + thing.getId());

        StateDescription state = getDefaultStateDescription();
        createChannelType(state);
    }

    private StateDescription getDefaultStateDescription() {
        List<StateOption> options = IntStream.rangeClosed(1, 40)
                .mapToObj(i -> new StateOption(Integer.toString(i), "Item_" + i))
                .collect(toList());

        StateDescription state = new StateDescription(null, null, null, "%s", false, options);
        return state;
    }

    public void changePresetNames(List<PresetInfoState.Preset> presets) {
        List<StateOption> options = presets.stream()
                .map(preset -> new StateOption(String.valueOf(preset.getValue()), preset.getName()))
                .collect(toList());

        StateDescription state = new StateDescription(null, null, null, "%s", false, options);
        createChannelType(state);
    }

    private void createChannelType(StateDescription state) {
        channelType = ChannelTypeBuilder.state(channelTypeUID, "Preset", "Number")
                .withDescription("Select a saved channel by its preset number").withStateDescription(state).build();
    }

}
