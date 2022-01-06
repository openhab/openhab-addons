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

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Data structure representing the content of a channel's group address configuration.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class ChannelConfiguration {

    private final @Nullable String dpt;
    private final GroupAddressConfiguration mainGA;
    private final List<GroupAddressConfiguration> listenGAs;

    public ChannelConfiguration(@Nullable String dpt, GroupAddressConfiguration mainGA,
            List<GroupAddressConfiguration> listenGAs) {
        this.dpt = dpt;
        this.mainGA = mainGA;
        this.listenGAs = listenGAs;
    }

    public @Nullable String getDPT() {
        return dpt;
    }

    public GroupAddressConfiguration getMainGA() {
        return mainGA;
    }

    public List<GroupAddressConfiguration> getListenGAs() {
        return Stream.concat(Stream.of(mainGA), listenGAs.stream()).collect(toList());
    }

    public List<GroupAddressConfiguration> getReadGAs() {
        return getListenGAs().stream().filter(ga -> ga.isRead()).collect(toList());
    }
}
