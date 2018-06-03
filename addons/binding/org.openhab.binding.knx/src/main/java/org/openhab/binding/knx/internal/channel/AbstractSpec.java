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
import org.openhab.binding.knx.client.InboundSpec;
import org.openhab.binding.knx.client.OutboundSpec;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXFormatException;

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

    /**
     * Helper method to convert a {@link GroupAddressConfiguration} into a {@link GroupAddress}.
     *
     * @param ga the group address configuration
     * @return a group address object
     */
    protected final GroupAddress toGroupAddress(GroupAddressConfiguration ga) {
        try {
            return new GroupAddress(ga.getGA());
        } catch (KNXFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Return the data point type.
     * <p>
     * See {@link InboundSpec#getDPT()} and {@link OutboundSpec#getDPT()}.
     *
     * @return the data point type.
     */
    public final String getDPT() {
        return dpt;
    }

}
