/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.internal.handler;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;

/**
 * An immutable, typed write request for one configured poller2 channel.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public record Poller2WriteRequest(String channelId, Command command, Type type, boolean writeMultiple) {
    public enum Type {
        HOLDING,
        COIL
    }

    public Poller2WriteRequest {
        Objects.requireNonNull(channelId, "channelId");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(type, "type");
        if (channelId.isBlank()) {
            throw new IllegalArgumentException("channelId must not be blank");
        }
    }

    public static Poller2WriteRequest holding(String channelId, Command command) {
        return holding(channelId, command, false);
    }

    public static Poller2WriteRequest holding(String channelId, Command command, boolean writeMultiple) {
        return new Poller2WriteRequest(channelId, command, Type.HOLDING, writeMultiple);
    }

    public static Poller2WriteRequest coil(String channelId, Command command) {
        return coil(channelId, command, false);
    }

    public static Poller2WriteRequest coil(String channelId, Command command, boolean writeMultiple) {
        return new Poller2WriteRequest(channelId, command, Type.COIL, writeMultiple);
    }
}
