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
package org.openhab.binding.hive.internal.client;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.exception.HiveException;

/**
 * A class for interacting with the Hive API as a specific user.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public interface HiveClient extends AutoCloseable {
    /**
     * Get the {@link UserId} of the user this client is authenticated as.
     */
    UserId getUserId();

    /**
     * Gets all the {@link Node}s associated with the authenticated user.
     */
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

    /**
     * Get a node with a given {@link NodeId}.
     *
     * @param nodeId
     *      The ID of the {@linkplain Node} to get.
     *
     * @return
     *      {@code null} if no {@linkplain Node} exists with the id
     *      {@code nodeId}.
     */
    @Nullable Node getNode(NodeId nodeId) throws HiveException;

    /**
     * Push an updated version of a {@link Node} to the Hive API.
     *
     * @param node
     *      The locally updated {@linkplain Node} that you want to push
     *      to the Hive API.
     *
     * @return
     *      The updated version of the {@linkplain Node} returned by the
     *      Hive API.
     */
    @Nullable Node updateNode(Node node) throws HiveException;
}
