/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.type;

import java.util.List;

import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.openhab.binding.neeo.internal.models.NeeoDevice;

/**
 * The interface for NeeoTypeGenerator to generate channel types for a give {@link NeeoDevice}
 *
 * @author Tim Roberts - Initial Contribution
 */
public interface NeeoChannelTypeProvider extends ChannelTypeProvider {
    /**
     * Adds a list of {@link ChannelType} to the provider
     *
     * @param channelTypes a non-null, possibly empty list of {@link ChannelType}
     */
    public void addChannelTypes(List<ChannelType> channelTypes);

    /**
     * Adds a list of {@link ChannelGroupType} to the provider
     *
     * @param groupTypes a non-null, possibly empty list of {@link ChannelGroupType}
     */
    public void addChannelGroupTypes(List<ChannelGroupType> groupTypes);
}
