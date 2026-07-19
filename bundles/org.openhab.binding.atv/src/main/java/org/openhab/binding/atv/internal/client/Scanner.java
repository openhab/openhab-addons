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
package org.openhab.binding.atv.internal.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.dto.ScanOptions;

/**
 * Device discovery contract used by {@link Atv#scan(ScanOptions)}.
 *
 * <p>
 * Implementations live in the {@code .scan} package (unicast scanner when
 * {@link ScanOptions#hosts()} is given, multicast/jmDNS otherwise) and are selected by
 * {@link Atv}; {@link ScanOptions#scanner()} can inject a specific instance. A scanner
 * returns the raw discovered configurations for the requested protocols — filtering on
 * readiness/identifier and applying stored settings is done by {@link Atv#scan(ScanOptions)}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface Scanner {

    /**
     * Discovers devices on the network.
     *
     * @param options scan options (timeout, protocols, hosts, runtime)
     * @return future completing with all discovered device configurations
     */
    CompletableFuture<List<AtvConfig>> discover(ScanOptions options);
}
