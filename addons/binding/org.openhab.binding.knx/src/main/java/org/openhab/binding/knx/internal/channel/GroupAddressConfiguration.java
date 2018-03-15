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
