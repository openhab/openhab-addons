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
package org.openhab.binding.bmwcardata.internal.stream;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A source of CarData stream messages: the direct connection to the BMW broker, or a local broker fed by the
 * bmw-mqtt-bridge.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
public interface StreamSource {

    /**
     * Starts connecting. Returns immediately, the progress is reported to the {@link StreamListener}.
     */
    void start();

    /**
     * Disconnects and releases all resources. No state callbacks are made after this returns; a data callback that is
     * being delivered may still complete.
     *
     * @return a future completed when the connection is closed
     */
    CompletableFuture<Boolean> stop();
}
