/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.renault.internal.renault.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author Doug Culnane - Initial contribution
 */
public class Car {

    private final Logger logger = LoggerFactory.getLogger(Car.class);

    public Integer battery_level;
    public Boolean locked;
    
    public void setBatteryStatus(JsonObject responseJson) {
        try {
            battery_level = responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                    .get("batteryLevel").getAsInt();
        } catch (Exception e) {
        	logger.error("Error parsing Battery Status: {}", responseJson);
        }
    }

	public void seLockStatus(JsonObject responseJson) {
		 try {
            locked = responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                    .get("locked").getAsBoolean();
        } catch (Exception e) {
        	logger.error("Error parsing Lock Status: {}", responseJson);
        }
		
	}
}
