/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The REST interface and websocket connection are using the same fields.
 * The REST data contains more descriptive info like the manufacturer and name.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorMessage {
    // For websocket change events
    public String e = ""; // "changed"
    public String r = ""; // "sensors"
    public String t = ""; // "event"
    public String id = ""; // "3"

    // for rest API
    public String manufacturername = "";
    public String modelid = "";
    public String name = "";
    public String swversion = "";
    public String type = "";
    /** the API endpoint **/
    public String ep = "";
    public SensorConfig config = new SensorConfig();

    // websocket and rest api
    public String uniqueid = ""; // "00:0b:57:ff:fe:94:6b:dd-01-1000"
    public SensorState state = new SensorState();
}
