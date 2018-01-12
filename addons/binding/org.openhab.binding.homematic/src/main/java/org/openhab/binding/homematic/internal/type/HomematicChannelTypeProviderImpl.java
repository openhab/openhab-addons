/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * Provides all ChannelTypes and ChannelGroupTypes from all Homematic bridges.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(service = { HomematicChannelTypeProvider.class, ChannelTypeProvider.class }, immediate = true)
public class HomematicChannelTypeProviderImpl implements HomematicChannelTypeProvider {
    private Map<ChannelTypeUID, ChannelType> channelTypesByUID = new HashMap<ChannelTypeUID, ChannelType>();
    private Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypesByUID = new HashMap<ChannelGroupTypeUID, ChannelGroupType>();

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return channelTypesByUID.values();
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return channelTypesByUID.get(channelTypeUID);
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return channelGroupTypesByUID.get(channelGroupTypeUID);
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return channelGroupTypesByUID.values();
    }

    @Override
    public void addChannelType(ChannelType channelType) {
        channelTypesByUID.put(channelType.getUID(), channelType);
    }

    @Override
    public void addChannelGroupType(ChannelGroupType channelGroupType) {
        channelGroupTypesByUID.put(channelGroupType.getUID(), channelGroupType);
    }

}
