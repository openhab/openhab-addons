/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

/**
 * Interface for classes controlling DSMR devices.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
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
