/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.internal.model;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Access object for storage information.
 *
 * @author Gerrit Beine
 */
public class StorageRealtimeData {

    private final Logger logger = LoggerFactory.getLogger(StorageRealtimeData.class);
    private final JsonObject json;

    private DecimalType capacity = DecimalType.ZERO;
    private DecimalType current = DecimalType.ZERO;
    private DecimalType voltage = DecimalType.ZERO;
    private DecimalType temperature = DecimalType.ZERO;
    private DecimalType charge = DecimalType.ZERO;
    private DecimalType code = DecimalType.ZERO;
    private DateTimeType timestamp = new DateTimeType();
    private boolean deconstructed = false;

    public StorageRealtimeData(final JsonObject json) {
        this.json = json;
    }

    public boolean isEmpty() {
        return !json.has("Body");
    }

    public DecimalType getCapacity() {
        if (!deconstructed) {
            deconstruct();
        }
        return capacity;
    }

    public DecimalType getCurrent() {
        if (!deconstructed) {
            deconstruct();
        }
        return current;
    }

    public DecimalType getVoltage() {
        if (!deconstructed) {
            deconstruct();
        }
        return voltage;
    }

    public DecimalType getTemperature() {
        if (!deconstructed) {
            deconstruct();
        }
        return temperature;
    }

    public DecimalType getCharge() {
        if (!deconstructed) {
            deconstruct();
        }
        return charge;
    }

    public DecimalType getCode() {
        if (!deconstructed) {
            deconstruct();
        }
        return code;
    }

    public DateTimeType getTimestamp() {
        if (!deconstructed) {
            deconstruct();
        }
        return timestamp;
    }

    private synchronized void deconstruct() {
        try {
            if (json.has("Body")) {
                final JsonObject body = json.get("Body").getAsJsonObject();
                logger.trace("{}", body.toString());
                if (body.has("Data")) {
                    final JsonObject data = body.get("Data").getAsJsonObject();
                    logger.trace("{}", data.toString());
                    if (data.has("Controller")) {
                        final JsonObject controller = data.get("Controller").getAsJsonObject();
                        logger.trace("{}", controller.toString());
                        if (controller.has("Capacity_Maximum")) {
                            capacity = new DecimalType(controller.get("Capacity_Maximum").getAsString());
                            logger.debug("Capacity: {}", capacity);
                        }
                        if (controller.has("Current_DC")) {
                            current = new DecimalType(controller.get("Current_DC").getAsString());
                            logger.debug("Current: {}", current);
                        }
                        if (controller.has("Voltage_DC")) {
                            voltage = new DecimalType(controller.get("Voltage_DC").getAsString());
                            logger.debug("Voltage: {}", voltage);
                        }
                        if (controller.has("Temperature_Cell")) {
                            temperature = new DecimalType(controller.get("Temperature_Cell").getAsString());
                            logger.debug("Temperature: {}", temperature);
                        }
                        if (controller.has("StateOfCharge_Relative")) {
                            charge = new DecimalType(controller.get("StateOfCharge_Relative").getAsString());
                            logger.debug("Charge: {}", charge);
                        }
                    }
                }
            }
            if (json.has("Head")) {
                final JsonObject head = json.get("Head").getAsJsonObject();
                logger.trace("{}", head.toString());
                if (head.has("Status")) {
                    final JsonObject status = head.get("Status").getAsJsonObject();
                    logger.trace("{}", status.toString());
                    if (status.has("Code")) {
                        code = new DecimalType(status.get("Code").getAsString());
                        logger.debug("Status Code: {}", code);
                    }
                }
                if (head.has("Timestamp")) {
                    timestamp = new DateTimeType(head.get("Timestamp").getAsString());
                    logger.debug("Timestamp: {}", timestamp);
                }
            }
        } catch (Exception e) {
            logger.warn("{}", e.toString());
        }
        deconstructed = true;
    }
}
