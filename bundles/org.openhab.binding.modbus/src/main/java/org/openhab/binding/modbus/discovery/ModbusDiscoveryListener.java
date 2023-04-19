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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.DiscoveryResult;

/**
 * Listener for discovery results
 *
 * Each discovered thing should be supplied to the thingDiscovered
 * method.
 *
 * When the discovery process has been finished then the discoveryFinished
 * method should be called.
 *
 * @author Nagy Attila Gabor - initial contribution
 *
 */
@NonNullByDefault
public interface ModbusDiscoveryListener {

    /**
     * Discovery participant should call this method when a new
     * thing has been discovered
     */
    void thingDiscovered(DiscoveryResult result);

    /**
     * This method should be called once the discovery has been finished
     * or aborted by any error.
     * It is important to call this even when there were no things discovered.
     */
    void discoveryFinished();
}
