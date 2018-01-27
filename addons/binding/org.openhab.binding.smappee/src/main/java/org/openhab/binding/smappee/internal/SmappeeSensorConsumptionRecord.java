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
 * What is the active power consumption of a specific appliance ?
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeSensorConsumptionRecord {
    public String timestamp;
    public String value1;
    public String value2;
    public String temperature;
    public String humidity;
    public String battery;
}

// Example JSON received from the Smappee API :
// {
// "timestamp": 1457597400000,
// "value1": 11.0,
// "value2": 2.0,
// "temperature": 226.0,
// "humidity": 41.0,
// "battery": 100.0
// }