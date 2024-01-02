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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for response payloads when retrieving players
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class Player {
    public String name = "";
    @SerializedName("pid")
    public int playerId;
    @SerializedName("gid")
    public @Nullable Integer playerIdOfGroupLeader;
    public String model = "";
    public String version = "";
    public String ip = "";
    public String network = "";
    public int lineout;
    public @Nullable String serial;

    @Override
    public String toString() {
        return "Player{" + "name='" + name + '\'' + ", playerId=" + playerId + ", playerIdOfGroupLeader="
                + playerIdOfGroupLeader + ", model='" + model + '\'' + ", version='" + version + '\'' + ", ip='" + ip
                + '\'' + ", network='" + network + '\'' + ", lineout=" + lineout + ", serial='" + serial + '\'' + '}';
    }
}
