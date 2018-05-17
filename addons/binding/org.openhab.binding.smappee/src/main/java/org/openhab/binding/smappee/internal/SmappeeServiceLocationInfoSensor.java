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
 * This contains info about the sensors (water / gas) you might have installed in your net
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeServiceLocationInfoSensor {

    public String id;
    public String name;
    public SmappeeServiceLocationInfoSensorChannel[] channels;
}
