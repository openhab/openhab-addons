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
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a custom channel type for available inputs
 *
 * @author David Graeff
 */
public class ChannelsTypeProviderAvailableInputs implements ChannelTypeProvider {
    private Logger logger = LoggerFactory.getLogger(ChannelsTypeProviderAvailableInputs.class);

    private ChannelType templateType;
    private ChannelType channelType;
    private ChannelTypeUID channelTypeUID;

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

        // Use the INPUT channel type as a template to create a new channel type AVAILABLE_INPUTS.
        final ChannelTypeUID channelTypeInputsUID = new ChannelTypeUID(YamahaReceiverBindingConstants.BINDING_ID,
                YamahaReceiverBindingConstants.CHANNEL_INPUT_TYPE_DEFAULT);
        templateType = TypeResolver.resolve(channelTypeInputsUID);
        if (templateType == null) {
            logger.warn("Couldn't find: {}", channelTypeInputsUID);
        } else {
            channelType = new ChannelType(channelTypeUID, false, "String", templateType.getKind(),
                    templateType.getLabel(), templateType.getDescription(), templateType.getCategory(),
                    templateType.getTags(), templateType.getState(), templateType.getEvent(),
                    templateType.getConfigDescriptionURI());
        }
    }

    public void changeAvailableInputs(Map<String, String> availableInputs) {
        if (templateType == null) {
            logger.warn("{} not initialized correctly", getClass().getName());
            return;
        }

        List<StateOption> options = new ArrayList<StateOption>();
        for (Entry<String, String> inputEntry : availableInputs.entrySet()) {
            options.add(new StateOption(inputEntry.getKey(), inputEntry.getValue()));
        }
        StateDescription state = new StateDescription(null, null, null, "%s", false, options);
        channelType = new ChannelType(channelTypeUID, false, "String", templateType.getKind(), templateType.getLabel(),
                templateType.getDescription(), templateType.getCategory(), templateType.getTags(), state,
                templateType.getEvent(), templateType.getConfigDescriptionURI());
    }

}
