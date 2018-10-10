/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.discovery;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;

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
public interface ModbusDiscoveryListener {

    /**
     * Discovery participant should call this method when a new
     * thing has been discovered
     */
    public void thingDiscovered(DiscoveryResult result);

    /**
     * This method should be called once the discovery has been finished
     * or aborted by any error.
     * It is important to call this even when there were no things discovered.
     */
    public void discoveryFinished();
}
