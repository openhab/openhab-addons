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

import java.util.LinkedHashMap;
import java.util.Map;

import org.openhab.binding.atv.internal.client.core.AtvRuntime;
import org.openhab.binding.atv.internal.client.settings.Storage;

/**
 * Options for {@link Atv#pair(org.openhab.binding.atv.internal.client.conf.AtvConfig, Protocol, PairOptions)}:
 * storage plus protocol-specific options passed through to the pairing handler (e.g. {@code name} for
 * Companion/AirPlay/RAOP selecting the display name registered on the device).
 *
 * <p>
 * Instances are immutable; use the {@code withX} methods to derive updated copies from
 * {@link #defaults()}.
 *
 * @param runtime runtime providing the scheduler and clock, or {@code null} for the
 *            default runtime
 * @param storage storage from which settings are loaded, or {@code null} for a fresh
 *            in-memory storage
 * @param pairingOptions protocol-specific options passed to the pairing handler; never {@code null}
 *
 * @author Dan Cunningham - Initial contribution
 */
public record PairOptions(AtvRuntime runtime, Storage storage, Map<String, Object> pairingOptions) {

    /**
     * Canonical constructor defaulting the options map to empty.
     */
    public PairOptions {
        pairingOptions = pairingOptions == null ? Map.of() : Map.copyOf(pairingOptions);
    }

    /**
     * Creates options with all defaults (default runtime, in-memory storage, no pairing
     * options).
     *
     * @return default options
     */
    public static PairOptions defaults() {
        return new PairOptions(null, null, null);
    }

    /**
     * Returns a copy with another runtime.
     *
     * @param runtime runtime to use
     * @return updated copy
     */
    public PairOptions withRuntime(AtvRuntime runtime) {
        return new PairOptions(runtime, storage, pairingOptions);
    }

    /**
     * Returns a copy with another storage.
     *
     * @param storage storage to use
     * @return updated copy
     */
    public PairOptions withStorage(Storage storage) {
        return new PairOptions(runtime, storage, pairingOptions);
    }

    /**
     * Returns a copy with an additional pairing option.
     *
     * @param key option name, e.g. {@code "name"}
     * @param value option value
     * @return updated copy
     */
    public PairOptions withOption(String key, Object value) {
        Map<String, Object> updated = new LinkedHashMap<>(pairingOptions);
        updated.put(key, value);
        return new PairOptions(runtime, storage, updated);
    }

    /**
     * Returns a copy with the {@code name} option, the display name our end registers on
     * the device during pairing.
     *
     * @param name display name
     * @return updated copy
     */
    public PairOptions withName(String name) {
        return withOption("name", name);
    }
}
