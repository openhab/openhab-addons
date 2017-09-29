/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mystrom.internal;

/**
 * Mystrom device.
 *
 * @author Stéphane Raemy - Initial contribution
 */
public class Device {
    /**
     * The id of the device on the mystrom server.
     */
    public String id;

    /**
     * The name of the device on the mystrom server.
     */
    public String name;

    /**
     * The state of the device, can be: on, off or offline.
     */
    public String state;

    /**
     * The power the device is consuming in Watt.
     */
    public String power;

    /**
     * Device type. mst: master, eth: ethernet, sw:switch, wsw:wifi switch
     */
    public String type;
}
