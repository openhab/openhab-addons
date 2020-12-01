/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents a scalar web channel ID factory. This will create unique channel IDs in the form of
 * "{service}#{id}{-nbr}" where "{-nbr}" is only specified if there is a service/id clash and will simply be an
 * increasing number.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebChannelIdFactory {
    /** The separator for a group */
    public static final char SEPARATOR = '#';

    /** The channel ids that have already been created */
    private final Set<String> channelIds = new HashSet<>();

    /**
     * Creates a unique channel id from the service name and id
     *
     * @param serviceName a non-null, non-empty service name
     * @param id a non-null, non-empty ID
     * @return the non-null, non-empty channel id
     */
    public String createChannelId(final String serviceName, final String id) {
        String channelId = serviceName + SEPARATOR + id;

        int idx = 0;
        while (channelIds.contains(channelId)) {
            channelId = serviceName + SEPARATOR + id + "-" + (++idx);
        }

        channelIds.add(channelId);
        return channelId;
    }
}
