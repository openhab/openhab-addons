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
package org.openhab.binding.hive.internal.client.repository;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.NodeId;
import org.openhab.binding.hive.internal.client.exception.HiveException;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public interface NodeRepository {
    Set<Node> getAllNodes() throws HiveException;

    /**
     * Get the raw JSON returned by the Hive API for a get all nodes request.
     *
     * <p>This is meant to be used for debugging purposes only</p>
     *
     * @return
     *      The raw JSON string returned by the Hive API.
     */
    String getAllNodesJson() throws HiveException;

    @Nullable Node getNode(NodeId nodeId) throws HiveException;

    @Nullable Node updateNode(Node node) throws HiveException;
}
