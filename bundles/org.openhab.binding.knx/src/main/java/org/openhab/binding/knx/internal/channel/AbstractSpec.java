/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.channel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.InboundSpec;
import org.openhab.binding.knx.internal.client.OutboundSpec;

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
