/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.handler.YamahaZoneThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a custom channel type for available inputs and the preset channel
 *
 * @author David Graeff
 */
public class YamahaChannelsTypeProviderImpl implements YamahaChannelsTypeProvider {
    private Logger logger = LoggerFactory.getLogger(YamahaZoneThingHandler.class);

    private ChannelType xmlInputType, xmlPresetType;
    private Map<ChannelTypeUID, ChannelType> channelTypes = new HashMap<ChannelTypeUID, ChannelType>();

    private static final ChannelTypeUID channelAvailableInputsTypeUID = new ChannelTypeUID(
            YamahaReceiverBindingConstants.BINDING_ID, YamahaReceiverBindingConstants.CHANNEL_INPUT_TYPE_AVAILABLE);
    private static final ChannelTypeUID channelNamedPresetTypeUID = new ChannelTypeUID(
            YamahaReceiverBindingConstants.BINDING_ID,
            YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET_TYPE_NAMED);

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return channelTypes.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return channelTypes.get(channelTypeUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        if (xmlInputType == null) {
            // Use the INPUT channel type as a template to create a new channel type AVAILABLE_INPUTS.
            final ChannelTypeUID channelTypeInputsUID = new ChannelTypeUID(YamahaReceiverBindingConstants.BINDING_ID,
                    YamahaReceiverBindingConstants.CHANNEL_INPUT_TYPE_DEFAULT);
            xmlInputType = TypeResolver.resolve(channelTypeInputsUID);
            if (xmlInputType == null) {
                logger.error("Expected 'input' channel type but couldn't find it. ID: {}", channelTypeInputsUID);
            } else {
                channelTypes.put(channelAvailableInputsTypeUID,
                        new ChannelType(channelAvailableInputsTypeUID, false, "String", xmlInputType.getKind(),
                                xmlInputType.getLabel(), xmlInputType.getDescription(), xmlInputType.getCategory(),
                                xmlInputType.getTags(), xmlInputType.getState(), xmlInputType.getEvent(),
                                xmlInputType.getConfigDescriptionURI()));
            }
        }

        if (xmlPresetType == null) {
            // Use the INPUT channel type as a template to create a new channel type AVAILABLE_INPUTS.
            final ChannelTypeUID channelTypePresetUID = new ChannelTypeUID(YamahaReceiverBindingConstants.BINDING_ID,
                    YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET_TYPE_DEFAULT);
            xmlPresetType = TypeResolver.resolve(channelTypePresetUID);
            if (xmlPresetType == null) {
                logger.error("Expected 'preset' channel type but couldn't find it. ID: {}", channelTypePresetUID);
            } else {
                channelTypes.put(channelNamedPresetTypeUID,
                        new ChannelType(channelNamedPresetTypeUID, false, "Number", xmlPresetType.getKind(),
                                xmlPresetType.getLabel(), xmlPresetType.getDescription(), xmlPresetType.getCategory(),
                                xmlPresetType.getTags(), xmlPresetType.getState(), xmlPresetType.getEvent(),
                                xmlPresetType.getConfigDescriptionURI()));
            }
        }
    }

    @Override
    public void changeAvailableInputs(Map<String, String> availableInputs) {
        List<StateOption> options = new ArrayList<StateOption>();
        for (Entry<String, String> inputEntry : availableInputs.entrySet()) {
            options.add(new StateOption(inputEntry.getKey(), inputEntry.getValue()));
        }
        StateDescription state = new StateDescription(null, null, null, "%s", false, options);
        channelTypes.put(channelAvailableInputsTypeUID,
                new ChannelType(channelAvailableInputsTypeUID, false, "String", xmlInputType.getKind(),
                        xmlInputType.getLabel(), xmlInputType.getDescription(), xmlInputType.getCategory(),
                        xmlInputType.getTags(), state, xmlInputType.getEvent(),
                        xmlInputType.getConfigDescriptionURI()));
    }

    @Override
    public void changePresetNames(String presetNames[]) {
        List<StateOption> options = new ArrayList<StateOption>();
        for (int i = 1; i <= presetNames.length; ++i) {
            options.add(new StateOption(String.valueOf(i), presetNames[i - 1]));
        }
        StateDescription state = new StateDescription(null, null, null, "%s", false, options);
        channelTypes.put(channelNamedPresetTypeUID,
                new ChannelType(channelNamedPresetTypeUID, false, "Number", xmlPresetType.getKind(),
                        xmlPresetType.getLabel(), xmlPresetType.getDescription(), xmlPresetType.getCategory(),
                        xmlPresetType.getTags(), state, xmlPresetType.getEvent(),
                        xmlPresetType.getConfigDescriptionURI()));
    }

}
