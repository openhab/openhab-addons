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
 * Access object for inverter data.
 *
 * @author Gerrit Beine
 */
public class InverterRealtimeData {

    private final Logger logger = LoggerFactory.getLogger(InverterRealtimeData.class);

    private DecimalType dayEnergy = DecimalType.ZERO;
    private DecimalType yearEnergy = DecimalType.ZERO;
    private DecimalType totalEnergy = DecimalType.ZERO;
    private DecimalType pac = DecimalType.ZERO;
    private DecimalType iac = DecimalType.ZERO;
    private DecimalType uac = DecimalType.ZERO;
    private DecimalType fac = DecimalType.ZERO;
    private DecimalType idc = DecimalType.ZERO;
    private DecimalType udc = DecimalType.ZERO;
    private DecimalType code = DecimalType.ZERO;
    private DateTimeType timestamp = new DateTimeType();
    private boolean empty = false;

    public static InverterRealtimeData createInverterRealtimeData(final JsonObject json) {
        final InverterRealtimeData ird = new InverterRealtimeData();
        ird.deconstruct(json);
        return ird;
    }

    private InverterRealtimeData() {
        super();
    }

    public boolean isEmpty() {
        return empty;
    }

    public DecimalType getDayEnergy() {
        return dayEnergy;
    }

    public DecimalType getYearEnergy() {
        return yearEnergy;
    }

    public DecimalType getTotalEnergy() {
        return totalEnergy;
    }

    public DecimalType getPac() {
        return pac;
    }

    public DecimalType getIac() {
        return iac;
    }

    public DecimalType getUac() {
        return uac;
    }

    public DecimalType getFac() {
        return fac;
    }

    public DecimalType getIdc() {
        return idc;
    }

    public DecimalType getUdc() {
        return udc;
    }

    public DecimalType getCode() {
        return code;
    }

    public DateTimeType getTimestamp() {
        return timestamp;
    }

    private synchronized void deconstruct(final JsonObject json) {
        try {
            if (json.has("Body")) {
                final JsonObject body = json.get("Body").getAsJsonObject();
                logger.trace("{}", body.toString());
                if (body.has("Data")) {
                    final JsonObject data = body.get("Data").getAsJsonObject();
                    logger.trace("{}", data.toString());
                    if (data.has("DAY_ENERGY")) {
                        dayEnergy = new DecimalType(
                                data.get("DAY_ENERGY").getAsJsonObject().get("Value").getAsString());
                        logger.debug("Day energy: {}", dayEnergy);
                    }
                    if (data.has("YEAR_ENERGY")) {
                        yearEnergy = new DecimalType(
                                data.get("YEAR_ENERGY").getAsJsonObject().get("Value").getAsString());
                        logger.debug("Year energy: {}", yearEnergy);
                    }
                    if (data.has("TOTAL_ENERGY")) {
                        totalEnergy = new DecimalType(
                                data.get("TOTAL_ENERGY").getAsJsonObject().get("Value").getAsString());
                        logger.debug("Total energy: {}", totalEnergy);
                    }
                    if (data.has("PAC")) {
                        pac = new DecimalType(data.get("PAC").getAsJsonObject().get("Value").getAsString());
                        logger.debug("PAC: {}", pac);
                    }
                    if (data.has("IAC")) {
                        iac = new DecimalType(data.get("IAC").getAsJsonObject().get("Value").getAsString());
                        logger.debug("IAC: {}", iac);
                    }
                    if (data.has("UAC")) {
                        uac = new DecimalType(data.get("UAC").getAsJsonObject().get("Value").getAsString());
                        logger.debug("UAC: {}", uac);
                    }
                    if (data.has("FAC")) {
                        fac = new DecimalType(data.get("FAC").getAsJsonObject().get("Value").getAsString());
                        logger.debug("FAC: {}", fac);
                    }
                    if (data.has("IDC")) {
                        idc = new DecimalType(data.get("IDC").getAsJsonObject().get("Value").getAsString());
                        logger.debug("IDC: {}", idc);
                    }
                    if (data.has("UDC")) {
                        udc = new DecimalType(data.get("UDC").getAsJsonObject().get("Value").getAsString());
                        logger.debug("UDC: {}", udc);
                    }
                    empty = false;
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
    }
}
