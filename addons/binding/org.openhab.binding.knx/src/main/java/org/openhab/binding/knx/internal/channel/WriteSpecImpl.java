/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.knx.client.OutboundSpec;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXFormatException;

/**
 * Command meta-data
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class WriteSpecImpl extends AbstractSpec implements OutboundSpec {

    private final Type type;
    private final @Nullable GroupAddress groupAddress;

    public WriteSpecImpl(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT, Type type)
            throws KNXFormatException {
        super(channelConfiguration, defaultDPT);
        if (channelConfiguration != null) {
            this.groupAddress = new GroupAddress(channelConfiguration.getMainGA().getGA());

        } else {
            this.groupAddress = null;
        }
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public @Nullable GroupAddress getGroupAddress() {
        return groupAddress;
    }

}
