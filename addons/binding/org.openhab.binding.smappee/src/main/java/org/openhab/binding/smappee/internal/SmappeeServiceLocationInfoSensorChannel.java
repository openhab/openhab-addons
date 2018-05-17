/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

/**
 * The result of a smappee service location info reading
 * Each sensor has multiple channels, 'type' will show the use of the sensor
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeServiceLocationInfoSensorChannel {

    public String channel;
    public String name;
    public String type;
    public String ppu;
    public String uom;
    public boolean enabled;
}
