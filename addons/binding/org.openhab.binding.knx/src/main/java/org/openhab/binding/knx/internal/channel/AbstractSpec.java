/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Base class for telegram meta-data
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public abstract class AbstractSpec {

    private String dpt;

    protected AbstractSpec(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT) {
        if (channelConfiguration != null) {
            String configuredDPT = channelConfiguration.getDPT();
            this.dpt = configuredDPT != null ? configuredDPT : defaultDPT;
        } else {
            this.dpt = defaultDPT;
        }

    }

    protected final @Nullable GroupAddress toGroupAddress(GroupAddressConfiguration ga) {
        try {
            return new GroupAddress(ga.getGA());
        } catch (KNXFormatException e) {
            return null;
        }
    }

    public final String getDPT() {
        return dpt;
    }

}
