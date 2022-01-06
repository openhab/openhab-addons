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

/**
 * Data structure representing a single group address configuration within a channel configuration parameter.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class GroupAddressConfiguration {

    private final String ga;
    private final boolean read;

    public GroupAddressConfiguration(String ga, boolean read) {
        super();
        this.ga = ga;
        this.read = read;
    }

    /**
     * The group address.
     *
     * @return the group address.
     */
    public String getGA() {
        return ga;
    }

    /**
     * Denotes whether the group address is marked to be actively read from.
     *
     * @return {@code true} if read requests should be issued to this address
     */
    public boolean isRead() {
        return read;
    }
}
