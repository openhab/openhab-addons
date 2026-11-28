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
package org.openhab.binding.atv.internal.client.dto;

import org.openhab.binding.atv.internal.client.core.AtvRuntime;
import org.openhab.binding.atv.internal.client.settings.Storage;

/**
 * Options for {@link Atv#connect(org.openhab.binding.atv.internal.client.conf.AtvConfig, ConnectOptions)}.
 *
 * <p>
 * Instances are immutable; use the {@code withX} methods to derive updated copies from
 * {@link #defaults()}.
 *
 * @param runtime runtime providing the scheduler, clock and device loop, or {@code null}
 *            for the default runtime
 * @param storage storage from which settings are loaded and applied, or {@code null} for a
 *            fresh in-memory storage
 * @param protocol restrict setup to this protocol only, or {@code null} to set up every
 *            configured protocol
 *
 * @author Dan Cunningham - Initial contribution
 */
public record ConnectOptions(AtvRuntime runtime, Storage storage, Protocol protocol) {

    /**
     * Creates options with all defaults (default runtime, in-memory storage, all
     * protocols).
     *
     * @return default options
     */
    public static ConnectOptions defaults() {
        return new ConnectOptions(null, null, null);
    }

    /**
     * Returns a copy with another runtime.
     *
     * @param runtime runtime to use
     * @return updated copy
     */
    public ConnectOptions withRuntime(AtvRuntime runtime) {
        return new ConnectOptions(runtime, storage, protocol);
    }

    /**
     * Returns a copy with another storage.
     *
     * @param storage storage to use
     * @return updated copy
     */
    public ConnectOptions withStorage(Storage storage) {
        return new ConnectOptions(runtime, storage, protocol);
    }

    /**
     * Returns a copy restricted to a single protocol.
     *
     * @param protocol protocol to set up
     * @return updated copy
     */
    public ConnectOptions withProtocol(Protocol protocol) {
        return new ConnectOptions(runtime, storage, protocol);
    }
}
