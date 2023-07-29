/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.modbus.discovery;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Interface for participants of Modbus discovery
 * This is an asynchronous process where a participant can discover
 * multiple things on a Modbus endpoint.
 *
 * Results should be submitted using the ModbusDiscvoeryListener
 * supplied at the begin of the scan.
 *
 * @author Nagy Attila Gabor - initial contribution
 *
 */
@NonNullByDefault
public interface ModbusDiscoveryParticipant {

    /**
     * Defines the list of thing types that this participant can identify
     *
     * @return a set of thing type UIDs for which results can be created
     */
    public Set<ThingTypeUID> getSupportedThingTypeUIDs();

    /**
     * Start an asynchronous discovery process of a Modbus endpoint
     *
     * @param handler the endpoint that should be discovered
     */
    public void startDiscovery(ModbusEndpointThingHandler handler, ModbusDiscoveryListener listener);
}
