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
package org.openhab.binding.teleinfo.internal.reader.io.serialport;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

/**
 * The {@link Label} enum defines all Teleinfo labels and their format.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public enum Label {

    ADCO(ValueType.STRING, NOT_A_CHANNEL, Units.ONE),
    OPTARIF(ValueType.STRING, NOT_A_CHANNEL, Units.ONE),
    BASE(ValueType.INTEGER, CHANNEL_BASE_FRAME_BASE, Units.WATT_HOUR),
    HCHC(ValueType.INTEGER, CHANNEL_HC_FRAME_HCHC, Units.WATT_HOUR),
    HCHP(ValueType.INTEGER, CHANNEL_HC_FRAME_HCHP, Units.WATT_HOUR),
    EJPHN(ValueType.INTEGER, CHANNEL_EJP_FRAME_EJPHN, Units.WATT_HOUR),
    EJPHPM(ValueType.INTEGER, CHANNEL_EJP_FRAME_EJPHN, Units.WATT_HOUR),
    PTEC(ValueType.STRING, CHANNEL_PTEC, Units.ONE),
    MOTDETAT(ValueType.STRING, CHANNEL_MOTDETAT, Units.AMPERE),
    ISOUSC(ValueType.INTEGER, CHANNEL_ISOUSC, Units.AMPERE),
    IINST(ValueType.INTEGER, CHANNEL_CBEMM_IINST, Units.AMPERE),
    IINST1(ValueType.INTEGER, CHANNEL_CBETM_IINST1, Units.AMPERE),
    IINST2(ValueType.INTEGER, CHANNEL_CBETM_IINST2, Units.AMPERE),
    IINST3(ValueType.INTEGER, CHANNEL_CBETM_IINST3, Units.AMPERE),
    ADIR1(ValueType.INTEGER, CHANNEL_CBETM_SHORT_ADIR1, Units.AMPERE),
    ADIR2(ValueType.INTEGER, CHANNEL_CBETM_SHORT_ADIR2, Units.AMPERE),
    ADIR3(ValueType.INTEGER, CHANNEL_CBETM_SHORT_ADIR3, Units.AMPERE),
    ADPS(ValueType.INTEGER, CHANNEL_CBEMM_ADPS, Units.AMPERE),
    IMAX(ValueType.INTEGER, CHANNEL_CBEMM_IMAX, Units.AMPERE),
    IMAX1(ValueType.INTEGER, CHANNEL_CBETM_LONG_IMAX1, Units.AMPERE),
    IMAX2(ValueType.INTEGER, CHANNEL_CBETM_LONG_IMAX2, Units.AMPERE),
    IMAX3(ValueType.INTEGER, CHANNEL_CBETM_LONG_IMAX3, Units.AMPERE),
    PMAX(ValueType.INTEGER, CHANNEL_CBETM_LONG_PMAX, Units.WATT),
    HHPHC(ValueType.STRING, CHANNEL_HHPHC, Units.ONE),
    PPOT(ValueType.STRING, CHANNEL_CBETM_LONG_PPOT, Units.ONE),
    PAPP(ValueType.INTEGER, CHANNEL_PAPP, Units.VOLT_AMPERE),
    BBRHCJB(ValueType.INTEGER, CHANNEL_TEMPO_FRAME_BBRHCJB, Units.WATT_HOUR),
    BBRHPJB(ValueType.INTEGER, CHANNEL_TEMPO_FRAME_BBRHPJB, Units.WATT_HOUR),
    BBRHCJW(ValueType.INTEGER, CHANNEL_TEMPO_FRAME_BBRHCJW, Units.WATT_HOUR),
    BBRHPJW(ValueType.INTEGER, CHANNEL_TEMPO_FRAME_BBRHPJW, Units.WATT_HOUR),
    BBRHCJR(ValueType.INTEGER, CHANNEL_TEMPO_FRAME_BBRHCJR, Units.WATT_HOUR),
    BBRHPJR(ValueType.INTEGER, CHANNEL_TEMPO_FRAME_BBRHPJR, Units.WATT_HOUR),
    PEJP(ValueType.INTEGER, CHANNEL_EJP_FRAME_PEJP, Units.MINUTE),
    DEMAIN(ValueType.STRING, CHANNEL_TEMPO_FRAME_DEMAIN, Units.ONE);

    private ValueType type;
    private String channelName;
    private Unit<?> unit;

    Label(ValueType type, String channelName, Unit<?> unit) {
        this.type = type;
        this.channelName = channelName;
        this.unit = unit;
    }

    public ValueType getType() {
        return type;
    }

    public String getChannelName() {
        return channelName;
    }

    public Unit<?> getUnit() {
        return unit;
    }
}
