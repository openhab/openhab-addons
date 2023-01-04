/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.modbus.discovery.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * Implementation of this interface is responsible for discovery over
 * a Modbus endpoint. Each time a supporting endpoint handler is created
 * an instance of this service will be created as well and attached to the
 * thing handler.
 *
 * @author Nagy Attila Gabor - initial contribution
 */
@NonNullByDefault
public interface ModbusThingHandlerDiscoveryService extends ThingHandlerService {

    /**
     * Implementation should start a discovery when this method gets called
     *
     * @param service the discovery service that should be called when the discovery is finished
     * @return returns true if discovery is enabled, false otherwise
     */
    public boolean startScan(ModbusDiscoveryService service);

    /**
     * This method should return true, if an async scan is in progress
     *
     * @return true if a scan is in progress false otherwise
     */
    public boolean scanInProgress();
}
