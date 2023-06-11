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
import org.openhab.binding.knx.internal.client.OutboundSpec;
import org.openhab.core.types.Type;

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
