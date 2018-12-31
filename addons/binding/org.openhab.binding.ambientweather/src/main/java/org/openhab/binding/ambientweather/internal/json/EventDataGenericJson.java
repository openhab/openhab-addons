/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.json;

/**
 * The {@link EventDataGenericJson} is the JSOn object
 * returned by the Ambient Weather real-time API.
 * The contents of the object varies by weather station, and
 * all we care about here is getting the MAC address. The MAC
 * address is used to decide which Station Thing Handler
 * instance gets the weather data object.
 *
 * @author Mark Hilbush - Initial Contribution
 */
public class EventDataGenericJson {
    /*
     * The weather station's MAC address
     */
    public String macAddress;

    /*
     * Placeholder for the rest of the JSON object.
     */
    public Object data;
}
