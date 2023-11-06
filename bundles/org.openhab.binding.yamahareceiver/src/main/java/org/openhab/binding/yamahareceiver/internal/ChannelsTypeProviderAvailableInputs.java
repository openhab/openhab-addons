/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yamahareceiver.internal.handler.YamahaZoneThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;

/**
 * Provide a custom channel type for available inputs
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Refactoring the input source names.
 */
@NonNullByDefault
public class ChannelsTypeProviderAvailableInputs implements ChannelTypeProvider, ThingHandlerService {
    private @NonNullByDefault({}) ChannelType channelType;
    private @NonNullByDefault({}) ChannelTypeUID channelTypeUID;
    private @NonNullByDefault({}) YamahaZoneThingHandler handler;

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return Set.of(channelType);
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (this.channelTypeUID.equals(channelTypeUID)) {
            return channelType;
        } else {
            return null;
        }
    }

    public ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }

    private void createChannelType(StateDescriptionFragment state) {
        channelType = ChannelTypeBuilder.state(channelTypeUID, "Input source", "String")
                .withDescription("Select the input source of the AVR").withStateDescriptionFragment(state).build();
    }

    private StateDescriptionFragment getDefaultStateDescription() {
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption(INPUT_NET_RADIO, "Net Radio"));
        options.add(new StateOption(INPUT_PC, "PC"));
        options.add(new StateOption(INPUT_USB, "USB"));
        options.add(new StateOption(INPUT_TUNER, "Tuner"));
        options.add(new StateOption("MULTI_CH", "Multi Channel"));
        // Note: this might need review in the future, it should be 'HDMI 1', the 'HDMI_1' are XML node names, not
        // source names.
        options.add(new StateOption("HDMI_1", "HDMI 1"));
        options.add(new StateOption("HDMI_2", "HDMI 2"));
        options.add(new StateOption("HDMI_3", "HDMI 3"));
        options.add(new StateOption("HDMI_4", "HDMI 4"));
        options.add(new StateOption("HDMI_5", "HDMI 5"));
        options.add(new StateOption("HDMI_6", "HDMI 6"));
        options.add(new StateOption("HDMI_7", "HDMI 7"));
        options.add(new StateOption("AV_1", "AV 1"));
        options.add(new StateOption("AV_2", "AV 2"));
        options.add(new StateOption("AV_3", "AV 3"));
        options.add(new StateOption("AV_4", "AV 4"));
        options.add(new StateOption("AV_5", "AV 5"));
        options.add(new StateOption("AV_6", "AV 6"));
        options.add(new StateOption("AV_7", "AV 7"));
        options.add(new StateOption("PHONO", "Phono"));
        options.add(new StateOption("V_AUX", "Aux"));
        options.add(new StateOption("AUDIO_1", "Audio 1"));
        options.add(new StateOption("AUDIO_2", "Audio 2"));
        options.add(new StateOption("AUDIO_3", "Audio 3"));
        options.add(new StateOption("AUDIO_4", "Audio 4"));
        options.add(new StateOption(INPUT_DOCK, "DOCK"));
        options.add(new StateOption(INPUT_IPOD, "iPod"));
        options.add(new StateOption(INPUT_IPOD_USB, "iPod/USB"));
        options.add(new StateOption(INPUT_BLUETOOTH, "Bluetooth"));
        options.add(new StateOption("UAW", "UAW"));
        options.add(new StateOption("NET", "NET"));
        options.add(new StateOption(INPUT_SIRIUS, "Sirius"));
        options.add(new StateOption(INPUT_RHAPSODY, "Rhapsody"));
        options.add(new StateOption("SIRIUS_IR", "SIRIUS IR"));
        options.add(new StateOption(INPUT_PANDORA, "Pandora"));
        options.add(new StateOption(INPUT_NAPSTER, "Napster"));
        options.add(new StateOption(INPUT_SPOTIFY, "Spotify"));
        return StateDescriptionFragmentBuilder.create().withPattern("%s").withReadOnly(false).withOptions(options)
                .build();
    }

    public void changeAvailableInputs(Map<String, String> availableInputs) {
        List<StateOption> options = new ArrayList<>();
        for (Entry<String, String> inputEntry : availableInputs.entrySet()) {
            options.add(new StateOption(inputEntry.getKey(), inputEntry.getValue()));
        }
        createChannelType(StateDescriptionFragmentBuilder.create().withPattern("%s").withReadOnly(false)
                .withOptions(options).build());
    }

    @NonNullByDefault({})
    @Override
    public void setThingHandler(ThingHandler handler) {
        this.handler = (YamahaZoneThingHandler) handler;
        this.handler.channelsTypeProviderAvailableInputs = this;
        channelTypeUID = new ChannelTypeUID(YamahaReceiverBindingConstants.BINDING_ID,
                YamahaReceiverBindingConstants.CHANNEL_INPUT_TYPE_AVAILABLE + handler.getThing().getUID().getId());
        createChannelType(getDefaultStateDescription());
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
