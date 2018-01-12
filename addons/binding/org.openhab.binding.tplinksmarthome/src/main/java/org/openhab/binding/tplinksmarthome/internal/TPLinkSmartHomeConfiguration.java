/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

/**
 * Data class representing the user configurable settings of the device
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class TPLinkSmartHomeConfiguration {

    /**
     * IP Address of the device.
     */
    public String ipAddress;

    /**
     * Refresh rate for the device in seconds.
     */
    public Integer refresh;

    /**
     * Transition period of light bulb state changes in seconds.
     */
    public int transitionPeriod;
}
