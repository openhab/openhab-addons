/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.squeezebox.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a Squeeze Player
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public class SqueezeBoxPlayer {
    public final String macAddress;
    @Nullable
    public final String name;
    @Nullable
    public final String ipAddr;
    @Nullable
    public final String model;
    @Nullable
    public final String uuid;

    public SqueezeBoxPlayer(String mac, @Nullable String name, @Nullable String ip, @Nullable String model,
            @Nullable String uuid) {
        this.macAddress = mac;
        this.name = name;
        this.ipAddr = ip;
        this.model = model;
        this.uuid = uuid;
    }
}
