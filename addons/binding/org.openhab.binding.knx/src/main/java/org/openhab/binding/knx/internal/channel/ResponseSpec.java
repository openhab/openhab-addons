/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.Nullable;

import tuwien.auto.calimero.GroupAddress;

/**
 * Response meta-data
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
public class ResponseSpec extends AbstractSpec {

    private final @Nullable GroupAddress groupAddress;

    public ResponseSpec(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT) {
        super(channelConfiguration, defaultDPT);
        if (channelConfiguration != null) {
            this.groupAddress = toGroupAddress(channelConfiguration.getMainGA());
        } else {
            this.groupAddress = null;
        }
    }

    public @Nullable GroupAddress getGroupAddress() {
        return groupAddress;
    }

}
