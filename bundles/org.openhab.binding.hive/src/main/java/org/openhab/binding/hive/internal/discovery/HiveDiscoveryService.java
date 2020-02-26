/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.discovery;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.openhab.binding.hive.internal.client.Node;

/**
 * An interface for a {@link DiscoveryService} used by
 * {@link org.openhab.binding.hive.internal.handler.HiveAccountHandler}s.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public interface HiveDiscoveryService extends DiscoveryService {
    /**
     * Provide the discovery service with a new set of known nodes from the
     * Hive API.
     *
     * @param knownNodes
     *      The new {@linkplain Set} of known {@linkplain Node}s.
     */
    void updateKnownNodes(Set<Node> knownNodes);
}
