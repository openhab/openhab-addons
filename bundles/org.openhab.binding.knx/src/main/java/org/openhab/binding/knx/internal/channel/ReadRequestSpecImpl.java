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

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.InboundSpec;

import tuwien.auto.calimero.GroupAddress;

/**
 * Read meta-data.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class ReadRequestSpecImpl extends AbstractSpec implements InboundSpec {

    private final List<GroupAddress> readAddresses;

    public ReadRequestSpecImpl(@Nullable ChannelConfiguration channelConfiguration, String defaultDPT) {
        super(channelConfiguration, defaultDPT);
        if (channelConfiguration != null) {
            this.readAddresses = channelConfiguration.getReadGAs().stream().map(this::toGroupAddress).collect(toList());
        } else {
            this.readAddresses = Collections.emptyList();
        }
    }

    @Override
    public List<GroupAddress> getGroupAddresses() {
        return readAddresses;
    }
}
