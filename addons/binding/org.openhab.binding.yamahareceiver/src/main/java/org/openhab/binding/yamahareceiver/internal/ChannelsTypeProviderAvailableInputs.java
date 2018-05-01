/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;

import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Inputs.*;

/**
 * Provide a custom channel type for available inputs
 *
 * @author David Graeff
 * @author Tomasz Maruszak - Refactoring the input source names.
 */
public class ChannelsTypeProviderAvailableInputs implements ChannelTypeProvider {

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

    public ChannelsTypeProviderAvailableInputs(ThingUID thing) {
        channelTypeUID = new ChannelTypeUID(YamahaReceiverBindingConstants.BINDING_ID,
                YamahaReceiverBindingConstants.CHANNEL_INPUT_TYPE_AVAILABLE + thing.getId());
        createChannelType(getDefaultStateDescription());
    }

    private void createChannelType(StateDescription state) {
        channelType = new ChannelType(channelTypeUID, false, "String", ChannelKind.STATE, "Input source",
                "Select the input source of the AVR", null, null, state, null, null);
    }

    private StateDescription getDefaultStateDescription() {
        List<StateOption> options = new ArrayList<StateOption>();
        options.add(new StateOption(INPUT_NET_RADIO, "Net Radio"));
        options.add(new StateOption(INPUT_PC, "PC"));
        options.add(new StateOption(INPUT_USB, "USB"));
        options.add(new StateOption(INPUT_TUNER, "Tuner"));
        options.add(new StateOption("MULTI_CH", "Multi Channel"));
        // Note: this might need review in the future, it should be 'HDMI 1', the 'HDMI_1' are XML node names, not source names.
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
        StateDescription state = new StateDescription(null, null, null, "%s", false, options);
        return state;
    }

    public void changeAvailableInputs(Map<String, String> availableInputs) {
        List<StateOption> options = new ArrayList<StateOption>();
        for (Entry<String, String> inputEntry : availableInputs.entrySet()) {
            options.add(new StateOption(inputEntry.getKey(), inputEntry.getValue()));
        }
        createChannelType(new StateDescription(null, null, null, "%s", false, options));
    }

}
