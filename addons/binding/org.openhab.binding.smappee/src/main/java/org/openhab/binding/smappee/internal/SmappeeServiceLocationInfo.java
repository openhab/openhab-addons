/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smappee.internal;

/**
 * The result of a service location reading
 * This will list the detected appliances and actuators (plugs)
 *
 * @author Niko Tanghe - Initial contribution
 */

public class SmappeeServiceLocationInfo {
    public int serviceLocationId;
    public String name;
    public String timezone;
    public String lon;
    public String lat;
    public String electricityCost;
    public String electricityCurrency;
    public SmappeeServiceLocationInfoAppliance[] appliances;
    public SmappeeServiceLocationInfoActuator[] actuators;
    public SmappeeServiceLocationInfoSensor[] sensors;
}

// sample response :

// {
// "serviceLocationId": 1,
// "name": "My Place",
// "timezone": "Europe/Brussels",
// "lon":0.0,
// "lat":0.0,
// "electricityCost": 0.0,
// "electricityCurrency": "EUR",
// "appliances": [
// {"id": 1, "name": "Coffeemaker", type: "cooking" },
// {"id": 2, "name": "Refrigerator", type: "refridgeration"}, ...
// ],
// "actuators": [
// {"id": 1, "name": "TV plug"},
// {"id": 2, "name": "Office plug"}
// ],
// "sensors": [
// {
// "id": 2,
// "name": "3003000078",
// "channels": [
// {
// "name": “Garage",
// "ppu": 100.0,
// "uom": "m3",
// "enabled": false,
// "type": "gas",
// "channel": 1
// },
// {
// "name": “Outdoor",
// "ppu": 100.0,
// "uom": "m3",
// "enabled": false,
// "type": “water",
// "channel": 2
// }
// ]
// }
// ]
// }