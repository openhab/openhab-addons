/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal.handler.helpers;

import java.util.Arrays;
import java.util.Objects;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairControllerStatus } class contain all status values from the controller.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairControllerStatus { // 29 byte packet format
    private final Logger logger = LoggerFactory.getLogger(PentairControllerStatus.class);

    public static final int NUMCIRCUITS = 18;

    private static final int HOUR = 0 + PentairStandardPacket.STARTOFDATA;
    private static final int MIN = 1 + PentairStandardPacket.STARTOFDATA;
    private static final int EQUIP1 = 2 + PentairStandardPacket.STARTOFDATA;
    private static final int EQUIP2 = 3 + PentairStandardPacket.STARTOFDATA;
    private static final int EQUIP3 = 4 + PentairStandardPacket.STARTOFDATA;
    private static final int STATUS = 9 + PentairStandardPacket.STARTOFDATA; // Celsius (0x04) or Farenheit, Service
                                                                             // Mode (0x01)
    private static final int HEAT_ACTIVE = 10 + PentairStandardPacket.STARTOFDATA;
    private static final int HEATER_DELAY = 12 + PentairStandardPacket.STARTOFDATA; // Something to do with heat?
    private static final int POOL_TEMP = 14 + PentairStandardPacket.STARTOFDATA;
    private static final int SPA_TEMP = 15 + PentairStandardPacket.STARTOFDATA;
    private static final int AIR_TEMP = 18 + PentairStandardPacket.STARTOFDATA;
    private static final int SOLAR_TEMP = 19 + PentairStandardPacket.STARTOFDATA;

    public int hour;
    public int min;

    /** Individual boolean values representing whether a particular ciruit is on or off */
    public int equip;
    public boolean pool, spa;
    public boolean[] circuits = new boolean[NUMCIRCUITS];

    public Unit<Temperature> uom = SIUnits.CELSIUS;
    public boolean serviceMode;
    public boolean heaterOn;
    public boolean solarOn;
    public boolean heaterDelay;
    public int poolTemp;
    /** spa temperature */
    public int spaTemp;
    /** air temperature */
    public int airTemp;
    /** solar temperature */
    public int solarTemp;

    /** spa heat mode - 0 = Off, 1 = Heater, 2 = Solar Pref, 3 = Solar */
    public int spaHeatMode;
    /** pool heat mode - 0 = Off, 1 = Heater, 2 = Solar Pref, 3 = Solar */
    public int poolHeatMode;

    /** used to store packet value for reverse engineering, not used in normal operation */
    public int diag;

    public void parsePacket(PentairStandardPacket p) {
        if (p.getPacketLengthHeader() != 29) {
            logger.debug("Controller status packet not 29 bytes long");
            return;
        }

        hour = p.getByte(HOUR);
        min = p.getByte(MIN);

        pool = (p.getByte(EQUIP1) & 0x20) != 0;
        spa = (p.getByte(EQUIP1) & 0x01) != 0;

        equip = p.getByte(EQUIP3) << 16 | p.getByte(EQUIP2) << 8 | p.getByte(EQUIP1);

        for (int i = 0; i < NUMCIRCUITS; i++) {
            circuits[i] = ((equip >> i) & 0x0001) == 1;
        }

        uom = ((p.getByte(STATUS) & 0x04) == 0) ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;
        serviceMode = (p.getByte(STATUS) & 0x01) != 0;

        heaterDelay = (p.getByte(HEATER_DELAY) & 0x02) != 0;

        diag = p.getByte(HEAT_ACTIVE);

        poolTemp = p.getByte(POOL_TEMP);
        spaTemp = p.getByte(SPA_TEMP);
        airTemp = p.getByte(AIR_TEMP);
        solarTemp = p.getByte(SOLAR_TEMP);

        solarOn = (p.getByte(HEAT_ACTIVE) & 0x30) != 0;
        heaterOn = (p.getByte(HEAT_ACTIVE) & 0x0C) != 0;
    }

    @Override
    public String toString() {
        String str = String.format(
                "%02d:%02d equip:%s pooltemp:%d spatemp:%d airtemp:%d solarttemp:%d uom:%s, service:%b, heaterDelay:%b",
                hour, min, String.format("%18s", Integer.toBinaryString(equip)).replace(' ', '0'), poolTemp, spaTemp,
                airTemp, solarTemp, uom.toString(), serviceMode, heaterDelay);

        return str;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof PentairControllerStatus controllerStatus)) {
            return false;
        }

        PentairControllerStatus p = controllerStatus;

        return Arrays.equals(circuits, p.circuits) && poolTemp == p.poolTemp && spaTemp == p.spaTemp
                && airTemp == p.airTemp && solarTemp == p.solarTemp && uom.equals(p.uom) && serviceMode == p.serviceMode
                && solarOn == p.solarOn && heaterOn == p.heaterOn && heaterDelay == p.heaterDelay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(circuits, poolTemp, spaTemp, airTemp, solarTemp, uom, serviceMode, solarOn, heaterOn,
                heaterDelay);
    }
}
