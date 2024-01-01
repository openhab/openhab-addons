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
package org.openhab.binding.heos.internal.json.payload;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for response payloads when retrieving group (information)
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class Group {
    @SerializedName("gid")
    public String id = "";
    public String name = "";
    public List<Player> players = Collections.emptyList();

    public String getGroupMemberIds() {
        return players.stream().map(p -> p.id).collect(Collectors.joining(";"));
    }

    public String getLeaderId() {
        return players.stream().filter(p -> p.role == GroupPlayerRole.LEADER).map(p -> p.id).findFirst()
                .orElseThrow(() -> new IllegalStateException("Every group should have a leader"));
    }

    public static class Player {
        @SerializedName("pid")
        public String id = "";
        public String name = "";
        public GroupPlayerRole role = GroupPlayerRole.MEMBER;
    }
}
