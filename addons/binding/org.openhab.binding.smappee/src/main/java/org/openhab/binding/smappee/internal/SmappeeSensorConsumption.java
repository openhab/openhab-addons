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
public class SmappeeSensorConsumption {
    public String serviceLocationId;
    public String sensorId;
    public SmappeeSensorConsumptionRecord[] records;
}

// Example JSON received from the Smappee API :
//
// {
// "serviceLocationId": 1,
// "sensorId": 4,
// "records": [
// {
// "timestamp": 1457597400000,
// "value1": 11.0,
// "value2": 2.0,
// "temperature": 226.0,
// "humidity": 41.0,
// "battery": 100.0
// },
// {
// "timestamp": 1457597700000,
// "value1": 9.0,
// "value2": 3.0,
// "temperature": 220.0,
// "humidity": 39.0,
// "battery": 100.0
// },
// … ]
// }
