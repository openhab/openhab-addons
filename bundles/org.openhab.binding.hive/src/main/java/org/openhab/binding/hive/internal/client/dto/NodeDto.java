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
package org.openhab.binding.hive.internal.client.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.NodeId;
import org.openhab.binding.hive.internal.client.NodeType;
import org.openhab.binding.hive.internal.client.Protocol;
import org.openhab.binding.hive.internal.client.UserId;

/**
 * A model of a "Node"
 *
 * Based on the Hive API Swagger model.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class NodeDto {
    /**
     * address (string, optional): Internal node ID, read-only.
     * The first part of node ID can be hub ID or node address.
     * Hub ID conforms following pattern: ([0-9]{19}).
     * Node address conforms following pattern: ::([a-f0-9]{1,4}):([a-f0-9]{1,4}):([a-f0-9]{1,4}):([a-f0-9]{1,4}).
     * Admin access required.
     */
    public @Nullable String address;

    /*
     * brokers (inline_model_26, optional): Brokers address map
     * e.g.: {"greengrass": {"url": "mqtt://10.0.0.7:8883", "topic": "hub/deviceid/userid"}}
     */

    /**
     * createdOn (integer, optional): Timestamp of when the node state was created.
     * UTC Unix timestamp (in milliseconds)
     */
    public @Nullable HiveApiInstant createdOn;

    public @Nullable FeaturesDto features;

    /**
     * firstInstall (integer, optional): Timestamp of when the device was discovered.
     * UTC Unix timestamp (in milliseconds)
     */
    public @Nullable HiveApiInstant firstInstall;

    /**
     * homeId (string, optional): Home ID, read-only
     */
    public @Nullable String homeId;

    /**
     * href (string, optional, read only): URL of the API call for retrieving this object
     */
    public @Nullable String href;

    /**
     * id (string, optional): Object identifier
     */
    public @Nullable NodeId id;

    /**
     * lastSeen (integer, optional): Timestamp of when the node was last seen.
     * UTC Unix timestamp (in milliseconds)
     */
    public @Nullable HiveApiInstant lastSeen;

    /**
     * lastUpgradeSucceeded (boolean, optional): Last upgrade succeeded
     */
    public @Nullable Boolean lastUpgradeSucceeded;

    /*
     * links (inline_model_27, optional): URL Templates for links to other entities
     * e.g.: "users.nodes": "https://api.example.com/nodes/{users.nodes}"
     */

    /**
     * name (string, optional): Node name
     */
    public @Nullable String name;

    /**
     * nodeType (string, optional): Node type
     */
    public @Nullable NodeType nodeType;

    /**
     * ownerId (string, optional): Original owner UUID
     */
    public @Nullable UserId ownerId;

    /**
     * parentNodeId (string, optional): Parent node UUID
     */
    public @Nullable NodeId parentNodeId;

    /**
     * protocol (string, optional): Node protocol, optional, read-only
     * ['SYNTHETIC', 'ZIGBEE', 'PROXIED', 'MQTT', 'XMPP', 'VIRTUAL', 'any other provided by hub']
     */
    public @Nullable Protocol protocol;

    /*
     * relationships (inline_model_28, optional): Node relationships
     * e.g.: "boundNodes" : [{ "type" : "node", "id" : "1b3b3c30-740a-11e5-8a3b-f46d042e952c"}] ,
     */

    /**
     * upgradeAvailable (boolean, optional): Is upgrade available
     */
    public @Nullable Boolean upgradeAvailable;

    /**
     * upgradeProgress (number, optional): Upgrade progress in percentages
     */
    public @Nullable Double upgradeProgress;

    /**
     * upgradeStatus (string, optional): Upgrade status, read-only
     * ['PENDING', 'DEFERRED', 'STARTING', 'IN_PROGRESS', 'COMPLETE', 'FAILED', 'UNSUPPORTED', 'INVALID']
     */
    public @Nullable String upgradeStatus;

    /**
     * userId (string, optional): User UUID
     */
    public @Nullable UserId userId;
}
