/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.upb.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * A channel supported by UPB things.
 *
 * @author Marcus Better - Initial contribution
 *
 */
@NonNullByDefault
public class UPBThingChannel {
    public enum DataType {
        OnOffType,
        PercentType
    }

    private final ChannelUID uid;
    private final ChannelTypeUID channelTypeUID;
    private final DataType dataType;

    public UPBThingChannel(final ChannelUID uid, final ChannelTypeUID channelTypeUID, final DataType dataType) {
        this.uid = uid;
        this.channelTypeUID = channelTypeUID;
        this.dataType = dataType;
    }

    public ChannelUID getUid() {
        return uid;
    }

    public ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }

    public DataType getDataType() {
        return dataType;
    }
}
