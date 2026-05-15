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
package org.openhab.binding.roborock.internal.transport;

import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Strategy interface for command transport selection.
 *
 * @author Maciej Pham - Initial contribution
 */
@NonNullByDefault
public interface RoborockCommandTransport {

    int sendCommand(String method, String params, int requestId) throws UnsupportedEncodingException;

    default void updateContext(@Nullable String localKey, @Nullable String localHost, int localPort,
            @Nullable String endpointPrefix) {
    }

    default void setMessageConsumer(@Nullable Consumer<byte[]> messageConsumer) {
    }

    default void dispose() {
    }
}
