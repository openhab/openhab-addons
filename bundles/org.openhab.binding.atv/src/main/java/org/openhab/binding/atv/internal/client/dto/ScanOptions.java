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

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openhab.binding.atv.internal.client.Scanner;
import org.openhab.binding.atv.internal.client.core.AtvRuntime;
import org.openhab.binding.atv.internal.client.settings.Storage;

/**
 * Options for {@link Atv#scan(ScanOptions)}.
 *
 * <p>
 * Instances are immutable; use the {@code withX} methods to derive updated copies from
 * {@link #defaults()}.
 *
 * @param timeout how long to scan
 * @param identifiers restrict search to devices with one of these identifiers; empty for
 *            no restriction
 * @param protocols restrict search to these protocols; empty for all
 * @param hosts scan these hosts only, using unicast scanning
 * @param runtime runtime whose scheduler drives scan timeouts, or {@code null} for the
 *            default runtime
 * @param storage storage from which stored settings are applied to results, or
 *            {@code null} for a fresh in-memory storage
 * @param scanner explicit scanner implementation, or {@code null} to let {@link Atv}
 *            select one (unicast when {@code hosts} is given, multicast otherwise)
 * @param knock whether a unicast scan knocks the well-known service ports to wake a device
 *            sleeping behind a Bonjour sleep proxy
 *
 * @author Dan Cunningham - Initial contribution
 */
public record ScanOptions(Duration timeout, Set<String> identifiers, Set<Protocol> protocols, List<String> hosts,
        AtvRuntime runtime, Storage storage, Scanner scanner, boolean knock) {

    /** Default scan timeout. */
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Canonical constructor applying defaults for unset components.
     */
    public ScanOptions {
        timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
        identifiers = identifiers == null ? Set.of() : Set.copyOf(identifiers);
        protocols = protocols == null ? Set.of() : Set.copyOf(protocols);
        hosts = hosts == null ? List.of() : List.copyOf(hosts);
    }

    /**
     * Creates options with all defaults (5 second multicast scan for all protocols).
     *
     * @return default options
     */
    public static ScanOptions defaults() {
        return new ScanOptions(null, null, null, null, null, null, null, true);
    }

    /**
     * Returns a copy with another timeout.
     *
     * @param timeout new timeout
     * @return updated copy
     */
    public ScanOptions withTimeout(Duration timeout) {
        return new ScanOptions(timeout, identifiers, protocols, hosts, runtime, storage, scanner, knock);
    }

    /**
     * Returns a copy restricted to the given identifiers.
     *
     * @param identifiers identifiers to search for
     * @return updated copy
     */
    public ScanOptions withIdentifiers(String... identifiers) {
        return new ScanOptions(timeout, new LinkedHashSet<>(Arrays.asList(identifiers)), protocols, hosts, runtime,
                storage, scanner, knock);
    }

    /**
     * Returns a copy restricted to the given protocols.
     *
     * @param protocols protocols to scan for
     * @return updated copy
     */
    public ScanOptions withProtocols(Protocol... protocols) {
        return new ScanOptions(timeout, identifiers, new LinkedHashSet<>(Arrays.asList(protocols)), hosts, runtime,
                storage, scanner, knock);
    }

    /**
     * Returns a copy scanning only the given hosts (unicast).
     *
     * @param hosts host addresses to scan
     * @return updated copy
     */
    public ScanOptions withHosts(List<String> hosts) {
        return new ScanOptions(timeout, identifiers, protocols, hosts, runtime, storage, scanner, knock);
    }

    /**
     * Returns a copy with another runtime.
     *
     * @param runtime runtime to use
     * @return updated copy
     */
    public ScanOptions withRuntime(AtvRuntime runtime) {
        return new ScanOptions(timeout, identifiers, protocols, hosts, runtime, storage, scanner, knock);
    }

    /**
     * Returns a copy with another storage.
     *
     * @param storage storage to use
     * @return updated copy
     */
    public ScanOptions withStorage(Storage storage) {
        return new ScanOptions(timeout, identifiers, protocols, hosts, runtime, storage, scanner, knock);
    }

    /**
     * Returns a copy with an explicit scanner implementation.
     *
     * @param scanner scanner to use
     * @return updated copy
     */
    public ScanOptions withScanner(Scanner scanner) {
        return new ScanOptions(timeout, identifiers, protocols, hosts, runtime, storage, scanner, knock);
    }

    /**
     * Returns a copy that does or does not knock the well-known service ports during a unicast scan.
     * Knocking wakes a device sleeping behind a Bonjour sleep proxy, so it should only be enabled when
     * waking the device is actually wanted.
     *
     * @param knock whether to knock
     * @return updated copy
     */
    public ScanOptions withKnock(boolean knock) {
        return new ScanOptions(timeout, identifiers, protocols, hosts, runtime, storage, scanner, knock);
    }
}
