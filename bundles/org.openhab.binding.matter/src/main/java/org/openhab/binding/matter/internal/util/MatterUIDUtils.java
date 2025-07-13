/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.util;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Utility class for creating and manipulating the UIDs for Matter thing types.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterUIDUtils {

    /**
     * Node thing types will have a UUID like matter:node-1234567890
     *
     * @param nodeId the node ID of the Node
     * @return a ThingTypeUID for a Node
     */
    public static ThingTypeUID nodeThingTypeUID(BigInteger nodeId) {
        return new ThingTypeUID(MatterBindingConstants.BINDING_ID,
                MatterBindingConstants.THING_TYPE_NODE.getId() + "-" + nodeId);
    }

    /**
     * Bridge Endpoint thing types will have a UUID like matter matter:endpoint-1234567890-1
     *
     * @param nodeId the node ID of the Node
     * @param endpointNumber the endpoint number of the Bridge Endpoint
     * @return a ThingTypeUID for a Bridge Endpoint
     */
    public static ThingTypeUID endpointThingTypeUID(BigInteger nodeId, Integer endpointNumber) {
        return new ThingTypeUID(MatterBindingConstants.BINDING_ID,
                MatterBindingConstants.THING_TYPE_ENDPOINT.getId() + "-" + nodeId + "-" + endpointNumber);
    }

    /**
     * Returns the base Thing type (node, etc...) for a dynamic thing
     *
     * @param thingTypeUID the ThingTypeUID to get the base type for
     * @return the base ThingTypeUID
     */
    public static @Nullable ThingTypeUID baseTypeForThingType(ThingTypeUID thingTypeUID) {
        if (!MatterBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId())) {
            return null;
        }
        String type = thingTypeUID.getId().split("-", 2)[0];
        switch (type) {
            case "node":
                return MatterBindingConstants.THING_TYPE_NODE;
            case "endpoint":
                return MatterBindingConstants.THING_TYPE_ENDPOINT;
            default:
                return null;
        }
    }
}
