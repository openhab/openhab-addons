/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
