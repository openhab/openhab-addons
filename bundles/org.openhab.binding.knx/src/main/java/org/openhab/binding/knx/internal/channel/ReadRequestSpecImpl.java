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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.client.InboundSpec;

import tuwien.auto.calimero.GroupAddress;

/**
 * Read meta-data.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class ReadRequestSpecImpl implements InboundSpec {
    private final String dpt;
    private final Set<GroupAddress> readAddresses;

    public ReadRequestSpecImpl(GroupAddressConfiguration groupAddressConfiguration, String defaultDPT) {
        this.dpt = Objects.requireNonNullElse(groupAddressConfiguration.getDPT(), defaultDPT);
        this.readAddresses = groupAddressConfiguration.getReadGAs();
    }

    @Override
    public String getDPT() {
        return dpt;
    }

    @Override
    public Set<GroupAddress> getGroupAddresses() {
        return readAddresses;
    }
}
