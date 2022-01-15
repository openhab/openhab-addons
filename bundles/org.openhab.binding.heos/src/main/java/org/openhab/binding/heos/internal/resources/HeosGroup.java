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
package org.openhab.binding.heos.internal.resources;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.heos.internal.json.payload.Group;

/**
 * The {@link HeosGroup} represents the group within the
 * HEOS network
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosGroup {
    public static String calculateGroupMemberHash(Group group) {
        List<String> sortedPlayerIds = group.players.stream().map(player -> player.id).sorted()
                .collect(Collectors.toList());

        return sortedToString(sortedPlayerIds);
    }

    private static String sortedToString(List<String> sortedPlayerIds) {
        return Integer.toUnsignedString(sortedPlayerIds.hashCode());
    }

    public static String calculateGroupMemberHash(String members) {
        List<String> sortedPlayerIds = Arrays.stream(members.split(";")).sorted().collect(Collectors.toList());

        return sortedToString(sortedPlayerIds);
    }
}
