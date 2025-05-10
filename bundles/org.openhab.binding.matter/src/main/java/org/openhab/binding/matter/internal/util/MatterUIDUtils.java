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
 * The {@link MatterUIDUtils}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterUIDUtils {

    /**
     * Node thing types will have a UUID like matter:node_1234567890
     *
     * @param nodeId
     * @return
     */
    public static ThingTypeUID nodeThingTypeUID(BigInteger nodeId) {
        return new ThingTypeUID(MatterBindingConstants.BINDING_ID,
                MatterBindingConstants.THING_TYPE_NODE.getId() + "_" + nodeId);
    }

    /**
     * Bridge Endpoint thing types will have a UUID like matter matter:endpoint_1234567890_1
     *
     * @param nodeId
     * @param endpointNumber
     * @return
     */
    public static ThingTypeUID endpointThingTypeUID(BigInteger nodeId, Integer endpointNumber) {
        return new ThingTypeUID(MatterBindingConstants.BINDING_ID,
                MatterBindingConstants.THING_TYPE_ENDPOINT.getId() + "_" + nodeId + "_" + endpointNumber);
    }

    /**
     * Returns the base Thing type (node, etc...) for a dynamic thing
     *
     * @param thingTypeUID
     * @return
     */
    public static @Nullable ThingTypeUID baseTypeForThingType(ThingTypeUID thingTypeUID) {
        String type = thingTypeUID.getId().split("_", 2)[0];
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
