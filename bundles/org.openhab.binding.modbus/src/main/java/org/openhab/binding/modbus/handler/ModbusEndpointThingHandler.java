/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.modbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.Identifiable;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.thing.ThingUID;

/**
 * Base interface for thing handlers of endpoint things
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ModbusEndpointThingHandler extends Identifiable<ThingUID> {

    /**
     * Gets the {@link ModbusCommunicationInterface} represented by the thing
     *
     * Note that this can be <code>null</code> in case of incomplete initialization
     *
     * @return communication interface represented by this thing handler
     */
    @Nullable
    ModbusCommunicationInterface getCommunicationInterface();

    /**
     * Get Slave ID, also called as unit id, represented by the thing
     *
     * @return slave id represented by this thing handler
     * @throws EndpointNotInitializedException in case the initialization is not complete
     */
    int getSlaveId() throws EndpointNotInitializedException;

    /**
     * Return true if auto discovery is enabled for this endpoint
     *
     * @return boolean true if the discovery is enabled
     */
    boolean isDiscoveryEnabled();
}
