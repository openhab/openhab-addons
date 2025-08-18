/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;

/**
 * A class encapsulating the roller shutter capability of a roller shutter endpoint
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class RollerShutterCapability {
    public Integer endpoint;
    public ChannelUID dimmerChannel;
    public ChannelUID upChannel;
    public ChannelUID downChannel;

    public RollerShutterCapability(Integer endpoint, ChannelUID dimmerChannel, ChannelUID upChannel,
            ChannelUID downChannel) {
        this.endpoint = endpoint;
        this.dimmerChannel = dimmerChannel;
        this.upChannel = upChannel;
        this.downChannel = downChannel;
    }

    @Override
    public String toString() {
        return "RollerShutterCapability [dimmerChannel=" + dimmerChannel + ", upChannel=" + upChannel + ", downChannel="
                + downChannel + "]";
    }
}
