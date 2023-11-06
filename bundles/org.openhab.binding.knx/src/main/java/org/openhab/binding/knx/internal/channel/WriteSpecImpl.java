/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Command meta-data
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class WriteSpecImpl implements OutboundSpec {
    private final String dpt;
    private final Type value;
    private final GroupAddress groupAddress;

    public WriteSpecImpl(GroupAddressConfiguration groupAddressConfiguration, String defaultDPT, Type value) {
        this.dpt = Objects.requireNonNullElse(groupAddressConfiguration.getDPT(), defaultDPT);
        this.groupAddress = groupAddressConfiguration.getMainGA();
        this.value = value;
    }

    @Override
    public String getDPT() {
        return dpt;
    }

    @Override
    public Type getValue() {
        return value;
    }

    @Override
    public GroupAddress getGroupAddress() {
        return groupAddress;
    }

    @Override
    public boolean matchesDestination(GroupAddress groupAddress) {
        return groupAddress.equals(this.groupAddress);
    }
}
