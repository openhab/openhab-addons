/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal;

import static org.openhab.binding.linky.internal.LinkyBindingConstants.*;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

/**
 * The {@link LinkyChannelRegistry} enum defines all Teleinfo labels and their format.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public enum LinkyChannelRegistry {

    // Standard TIC mode labels
    _ID_D2L(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_ID_D2L, Units.ONE),
    _TYPE_TRAME(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_TYPE_TRAME, Units.ONE),
    _DATE_FIRMWARE(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_DATE_FIRMWARE, Units.ONE),

    ADSC(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_ADSC, Units.ONE),
    VTIC(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_VTIC, Units.ONE),
    DATE(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_DATE, Units.ONE),
    NGTF(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_NGTF, Units.ONE),
    LTARF(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_LTARF, Units.ONE),

    EAST(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EAST, Units.WATT_HOUR),

    EASF01(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF01, Units.WATT_HOUR),
    EASF02(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF02, Units.WATT_HOUR),
    EASF03(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF03, Units.WATT_HOUR),
    EASF04(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF04, Units.WATT_HOUR),
    EASF05(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF05, Units.WATT_HOUR),
    EASF06(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF06, Units.WATT_HOUR),
    EASF07(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF07, Units.WATT_HOUR),
    EASF08(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF08, Units.WATT_HOUR),
    EASF09(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF09, Units.WATT_HOUR),
    EASF10(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASF10, Units.WATT_HOUR),

    EASD01(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASD01, Units.WATT_HOUR),
    EASD02(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASD02, Units.WATT_HOUR),
    EASD03(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASD03, Units.WATT_HOUR),
    EASD04(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_EASD04, Units.WATT_HOUR),

    EAIT(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_EAIT, Units.WATT_HOUR),

    ERQ1(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_ERQ1, Units.VOLT_AMPERE_HOUR),
    ERQ2(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_ERQ2, Units.VOLT_AMPERE_HOUR),
    ERQ3(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_ERQ3, Units.VOLT_AMPERE_HOUR),
    ERQ4(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_ERQ4, Units.VOLT_AMPERE_HOUR),

    IRMS1(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_IRMS1, Units.AMPERE),
    IRMS2(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_IRMS2, Units.AMPERE),
    IRMS3(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_IRMS3, Units.AMPERE),

    IRMS1F(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_IRMS1, Units.AMPERE),

    URMS1(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_URMS1, Units.VOLT),
    URMS2(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_URMS2, Units.VOLT),
    URMS3(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_URMS3, Units.VOLT),

    PREF(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_PREF, Units.VOLT_AMPERE, 1000),
    PCOUP(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_PCOUP, Units.VOLT_AMPERE, 1000),

    SINSTS(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_SINSTS, Units.VOLT_AMPERE),
    SINSTS1(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SINSTS1, Units.VOLT_AMPERE),
    SINSTS2(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SINSTS2, Units.VOLT_AMPERE),
    SINSTS3(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SINSTS3, Units.VOLT_AMPERE),

    SMAXSN(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_SMAXSN, CHANNEL_SMAXSN_DATE, Units.VOLT_AMPERE),
    SMAXSN1(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SMAXSN1, CHANNEL_SMAXSN1_DATE, Units.VOLT_AMPERE),
    SMAXSN2(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SMAXSN2, CHANNEL_SMAXSN2_DATE, Units.VOLT_AMPERE),
    SMAXSN3(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SMAXSN3, CHANNEL_SMAXSN3_DATE, Units.VOLT_AMPERE),

    SMAXSN_MINUS_1(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_SMAXSN_MINUS_1, CHANNEL_SMAXSN_MINUS_1_DATE,
            Units.VOLT_AMPERE),
    SMAXSN1_MINUS_1(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SMAXSN1_MINUS_1, CHANNEL_SMAXSN1_MINUS_1_DATE,
            Units.VOLT_AMPERE),
    SMAXSN2_MINUS_1(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SMAXSN2_MINUS_1, CHANNEL_SMAXSN2_MINUS_1_DATE,
            Units.VOLT_AMPERE),
    SMAXSN3_MINUS_1(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_SMAXSN3_MINUS_1, CHANNEL_SMAXSN3_MINUS_1_DATE,
            Units.VOLT_AMPERE),

    SINSTI(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_SINSTI, Units.VOLT_AMPERE),

    SMAXIN(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_SMAXIN, CHANNEL_SMAXIN_DATE, Units.VOLT_AMPERE),
    SMAXIN_MINUS_1(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_SMAXIN_MINUS_1, CHANNEL_SMAXIN_MINUS_1_DATE,
            Units.VOLT_AMPERE),

    CCASN(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_CCASN, CHANNEL_CCASN_DATE, Units.WATT),
    CCASN_MINUS_1(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_CCASN_MINUS_1, CHANNEL_CCASN_MINUS_1_DATE,
            Units.WATT),

    CCAIN(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_CCAIN, CHANNEL_CCAIN_DATE, Units.WATT),
    CCAIN_MINUS_1(ValueType.INTEGER, LINKY_LOCAL_PRODUCER_GROUP, CHANNEL_CCAIN_MINUS_1, CHANNEL_CCAIN_MINUS_1_DATE,
            Units.WATT),

    UMOY1(ValueType.INTEGER, LINKY_LOCAL_MAIN_GROUP, CHANNEL_UMOY1, CHANNEL_UMOY1_DATE, Units.VOLT),
    UMOY2(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_UMOY2, CHANNEL_UMOY2_DATE, Units.VOLT),
    UMOY3(ValueType.INTEGER, LINKY_LOCAL_3PHASE_GROUP, CHANNEL_UMOY3, CHANNEL_UMOY3_DATE, Units.VOLT),

    STGE(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_STGE, Units.ONE),

    DPM1(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_DPM1, CHANNEL_DPM1_DATE, Units.ONE),
    FPM1(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_FPM1, CHANNEL_FPM1_DATE, Units.ONE),
    DPM2(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_DPM2, CHANNEL_DPM2_DATE, Units.ONE),
    FPM2(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_FPM2, CHANNEL_FPM2_DATE, Units.ONE),
    DPM3(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_DPM3, CHANNEL_DPM3_DATE, Units.ONE),
    FPM3(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_FPM3, CHANNEL_FPM3_DATE, Units.ONE),

    MSG1(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_MSG1, Units.ONE),
    MSG2(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_MSG2, Units.ONE),

    PRM(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_PRM, Units.ONE),

    RELAIS(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_NONE, Units.ONE),

    NTARF(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_NTARF, Units.ONE),
    NJOURF(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_NJOURF, Units.ONE),
    NJOURF_PLUS_1(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_NJOURF_PLUS_1, Units.ONE),
    PJOURF_PLUS_1(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_PJOURF_PLUS_1, Units.ONE),
    PPOINTE(ValueType.STRING, LINKY_LOCAL_MAIN_GROUP, CHANNEL_PPOINTE, Units.ONE);

    private final ValueType type;
    private final String groupName;
    private final String channelName;
    private final String timestampChannelName;
    private final Unit<?> unit;
    private final int factor;

    LinkyChannelRegistry(ValueType type, String groupName, String channelName, Unit<?> unit) {
        this(type, groupName, channelName, CHANNEL_NONE, unit, 1);
    }

    LinkyChannelRegistry(ValueType type, String groupName, String channelName, String timestampChannelName,
            Unit<?> unit) {
        this(type, groupName, channelName, timestampChannelName, unit, 1);
    }

    LinkyChannelRegistry(ValueType type, String groupName, String channelName, Unit<?> unit, int factor) {
        this(type, groupName, channelName, CHANNEL_NONE, unit, factor);
    }

    LinkyChannelRegistry(ValueType type, String groupName, String channelName, String timestampChannelName,
            Unit<?> unit, int factor) {
        this.type = type;
        this.groupName = groupName;
        this.channelName = channelName;
        this.timestampChannelName = timestampChannelName;
        this.unit = unit;
        this.factor = factor;
    }

    public ValueType getType() {
        return type;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getTimestampChannelName() {
        return timestampChannelName;
    }

    public Unit<?> getUnit() {
        return unit;
    }

    public int getFactor() {
        return factor;
    }

    public static LinkyChannelRegistry getEnum(String label) {
        String modifiedLabel = label.replace("-", "_MINUS_");
        modifiedLabel = modifiedLabel.replace("+", "_PLUS_");
        return valueOf(modifiedLabel);
    }
}
