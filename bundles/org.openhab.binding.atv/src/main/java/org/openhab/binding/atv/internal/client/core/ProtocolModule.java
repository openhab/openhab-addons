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
package org.openhab.binding.atv.internal.client.core;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.PairingHandler;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * Unified protocol-module entry contract: each protocol package exposes one implementation describing how to
 * set up, pair and gather scan metadata for that protocol.
 *
 * <p>
 * Implementations are exposed as {@code MODULE} singletons on each protocol's static entry class (e.g.
 * {@code CompanionProtocolModule.MODULE}, {@code Mrp.MODULE}); the static methods remain the canonical
 * per-protocol API and the singleton delegates to them.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface ProtocolModule {

    /**
     * Protocol implemented by this module.
     *
     * @return the protocol
     */
    Protocol protocol();

    /**
     * Zeroconf service types handled by this protocol.
     *
     * @return the service types
     */
    Set<String> scanServiceTypes();

    /**
     * Returns device information from zeroconf properties.
     *
     * @param serviceType zeroconf service type the properties were announced under
     * @param properties zeroconf service properties
     * @return protocol-specific device information fields
     */
    Map<String, Object> deviceInfo(String serviceType, Map<String, String> properties);

    /**
     * Updates a discovered service with additional information such as the pairing requirement.
     *
     * @param service the service to update
     */
    void serviceInfo(Service service);

    /**
     * Sets up a new instance of this protocol; one {@link SetupData} per connection the protocol contributes,
     * or an empty set when the service cannot be set up (e.g. missing credentials).
     *
     * @param core protocol context
     * @return setup data for the relay
     */
    Set<SetupData> setup(Core core);

    /**
     * Returns a pairing handler for this protocol.
     *
     * @param core protocol context
     * @param options protocol-specific options (e.g. {@code "name"} for Companion); may be empty, never
     *            {@code null}
     * @return pairing handler
     */
    PairingHandler pair(Core core, Map<String, Object> options);
}
