/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * Provides all ChannelTypes and ChannelGroupTypes from all Homematic bridges.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicChannelTypeProviderImpl implements HomematicChannelTypeProvider {
    private Map<ChannelTypeUID, ChannelType> channelTypesByUID = new HashMap<ChannelTypeUID, ChannelType>();
    private Map<ChannelGroupTypeUID, ChannelGroupType> channelGroupTypesByUID = new HashMap<ChannelGroupTypeUID, ChannelGroupType>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return channelTypesByUID.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        return channelTypesByUID.get(channelTypeUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        return channelGroupTypesByUID.get(channelGroupTypeUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return channelGroupTypesByUID.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChannelType(ChannelType channelType) {
        channelTypesByUID.put(channelType.getUID(), channelType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChannelGroupType(ChannelGroupType channelGroupType) {
        channelGroupTypesByUID.put(channelGroupType.getUID(), channelGroupType);
    }

}
