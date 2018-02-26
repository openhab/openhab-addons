/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.client.OutboundSpec;

import tuwien.auto.calimero.GroupAddress;

/**
 * Response meta-data
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class ReadResponseSpecImpl extends AbstractSpec implements OutboundSpec {

    private final @Nullable GroupAddress groupAddress;
    private final Type type;

    public ReadResponseSpecImpl(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT, Type state) {
        super(channelConfiguration, defaultDPT);
        if (channelConfiguration != null) {
            this.groupAddress = toGroupAddress(channelConfiguration.getMainGA());
        } else {
            this.groupAddress = null;
        }
        this.type = state;
    }

    @Override
    public @Nullable GroupAddress getGroupAddress() {
        return groupAddress;
    }

    @Override
    public Type getType() {
        return type;
    }

}
