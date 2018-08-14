/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class ChannelDescription {
    public final ChannelTypeUID ChannelTypeUID;
    public final String ItemType;
    @NonNull
    public final String Label;
    public final boolean IsStateChannel;

    public ChannelDescription(ChannelTypeUID channelTypeUID, String itemType) {
        this(channelTypeUID, itemType, "", true);
    }

    public ChannelDescription(ChannelTypeUID channelTypeUID, String itemType, String label, boolean isStateChannel) {
        ChannelTypeUID = channelTypeUID;
        ItemType = itemType;
        if (label != null) {
            Label = label;
        } else {
            Label = "";
        }

        IsStateChannel = isStateChannel;
    }
}
