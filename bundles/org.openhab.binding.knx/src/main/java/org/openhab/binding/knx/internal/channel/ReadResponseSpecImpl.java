/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
public class ReadResponseSpecImpl implements OutboundSpec {
    private final String dpt;
    private final GroupAddress groupAddress;
    private final Type value;

    public ReadResponseSpecImpl(GroupAddressConfiguration groupAddressConfiguration, String defaultDPT, Type state) {
        this.dpt = Objects.requireNonNullElse(groupAddressConfiguration.getDPT(), defaultDPT);
        this.groupAddress = groupAddressConfiguration.getMainGA();
        this.value = state;
    }

    @Override
    public String getDPT() {
        return dpt;
    }

    @Override
    public GroupAddress getGroupAddress() {
        return groupAddress;
    }

    @Override
    public Type getValue() {
        return value;
    }

    @Override
    public boolean matchesDestination(GroupAddress groupAddress) {
        return groupAddress.equals(this.groupAddress);
    }
}
