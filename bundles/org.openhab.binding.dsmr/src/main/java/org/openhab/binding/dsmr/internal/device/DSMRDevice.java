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
package org.openhab.binding.dsmr.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for classes controlling DSMR devices.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public interface DSMRDevice {

    /**
     * Restart the DSMR device.
     */
    void restart();

    /**
     * Start the DSMR device.
     */
    void start();

    /**
     * Stop the DSMR device.
     */
    void stop();

    /**
     * @param lenientMode the lenientMode to set
     */
    void setLenientMode(boolean lenientMode);
}
