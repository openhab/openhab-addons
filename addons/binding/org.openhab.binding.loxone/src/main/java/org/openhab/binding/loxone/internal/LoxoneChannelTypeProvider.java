/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * Service responsible for custom channel types defined by the binding.
 * Custom channel types are required to handle Miniserver controls, which provide full information about their
 * configuration in the runtime. This information contains mapping of control's state values to a user-readable text
 * description. This mapping is provided to the channel definition as part of {@link StateDescription}.
 *
 * @author Pawel Pieczul - Initial contribution
 */
public class LoxoneChannelTypeProvider implements ChannelTypeProvider {
    private List<ChannelType> channelTypes = new CopyOnWriteArrayList<>();
    private List<ChannelGroupType> channelGroupTypes = new CopyOnWriteArrayList<>();

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return channelTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        for (ChannelType channelType : channelTypes) {
            if (channelType.getUID().equals(channelTypeUID)) {
                return channelType;
            }
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return channelGroupTypes;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        for (ChannelGroupType channelGroupType : channelGroupTypes) {
            if (channelGroupType.getUID().equals(channelGroupTypeUID)) {
                return channelGroupType;
            }
        }
        return null;
    }

    public void addChannelGroupType(ChannelGroupType type) {
        channelGroupTypes.add(type);
    }

    public void removeChannelGroupType(ChannelGroupType type) {
        channelGroupTypes.remove(type);
    }

    public void addChannelType(ChannelType type) {
        channelTypes.add(type);
    }

    public void removeChannelType(ChannelType type) {
        channelTypes.remove(type);
    }

    public void removeChannelType(ChannelTypeUID id) {
        List<ChannelType> removes = new ArrayList<>();
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().equals(id.getAsString())) {
                removes.add(c);
            }
        }
        channelTypes.removeAll(removes);
    }

    public void removeChannelTypesForThing(ThingUID uid) {
        List<ChannelType> removes = new ArrayList<>();
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().startsWith(uid.getAsString())) {
                removes.add(c);
            }
        }
        channelTypes.removeAll(removes);
    }
}
