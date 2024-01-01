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

    // Historical labels
    ADCO(ValueType.STRING, NOT_A_CHANNEL, Units.ONE),
    OPTARIF(ValueType.STRING, NOT_A_CHANNEL, Units.ONE),
    BASE(ValueType.INTEGER, CHANNEL_BASE_FRAME_BASE, Units.WATT_HOUR),
    HCHC(ValueType.INTEGER, CHANNEL_HC_FRAME_HCHC, Units.WATT_HOUR),
    HCHP(ValueType.INTEGER, CHANNEL_HC_FRAME_HCHP, Units.WATT_HOUR),
    EJPHN(ValueType.INTEGER, CHANNEL_EJP_FRAME_EJPHN, Units.WATT_HOUR),
    EJPHPM(ValueType.INTEGER, CHANNEL_EJP_FRAME_EJPHPM, Units.WATT_HOUR),
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
    DEMAIN(ValueType.STRING, CHANNEL_TEMPO_FRAME_DEMAIN, Units.ONE),

    // Standard TIC mode labels
    ADSC(ValueType.STRING, NOT_A_CHANNEL, Units.ONE),
    VTIC(ValueType.INTEGER, NOT_A_CHANNEL, Units.ONE),
    DATE(ValueType.STRING, NOT_A_CHANNEL, CHANNEL_LSM_DATE, Units.ONE),
    NGTF(ValueType.STRING, CHANNEL_LSM_NGTF, Units.ONE),
    LTARF(ValueType.STRING, CHANNEL_LSM_LTARF, Units.ONE),
    EAST(ValueType.INTEGER, CHANNEL_LSM_EAST, Units.WATT_HOUR),
    EASF01(ValueType.INTEGER, CHANNEL_LSM_EASF01, Units.WATT_HOUR),
    EASF02(ValueType.INTEGER, CHANNEL_LSM_EASF02, Units.WATT_HOUR),
    EASF03(ValueType.INTEGER, CHANNEL_LSM_EASF03, Units.WATT_HOUR),
    EASF04(ValueType.INTEGER, CHANNEL_LSM_EASF04, Units.WATT_HOUR),
    EASF05(ValueType.INTEGER, CHANNEL_LSM_EASF05, Units.WATT_HOUR),
    EASF06(ValueType.INTEGER, CHANNEL_LSM_EASF06, Units.WATT_HOUR),
    EASF07(ValueType.INTEGER, CHANNEL_LSM_EASF07, Units.WATT_HOUR),
    EASF08(ValueType.INTEGER, CHANNEL_LSM_EASF08, Units.WATT_HOUR),
    EASF09(ValueType.INTEGER, CHANNEL_LSM_EASF09, Units.WATT_HOUR),
    EASF10(ValueType.INTEGER, CHANNEL_LSM_EASF10, Units.WATT_HOUR),
    EASD01(ValueType.INTEGER, CHANNEL_LSM_EASD01, Units.WATT_HOUR),
    EASD02(ValueType.INTEGER, CHANNEL_LSM_EASD02, Units.WATT_HOUR),
    EASD03(ValueType.INTEGER, CHANNEL_LSM_EASD03, Units.WATT_HOUR),
    EASD04(ValueType.INTEGER, CHANNEL_LSM_EASD04, Units.WATT_HOUR),
    EAIT(ValueType.INTEGER, CHANNEL_LSM_EAIT, Units.WATT_HOUR),
    ERQ1(ValueType.INTEGER, CHANNEL_LSM_ERQ1, Units.VOLT_AMPERE_HOUR),
    ERQ2(ValueType.INTEGER, CHANNEL_LSM_ERQ2, Units.VOLT_AMPERE_HOUR),
    ERQ3(ValueType.INTEGER, CHANNEL_LSM_ERQ3, Units.VOLT_AMPERE_HOUR),
    ERQ4(ValueType.INTEGER, CHANNEL_LSM_ERQ4, Units.VOLT_AMPERE_HOUR),
    IRMS1(ValueType.INTEGER, CHANNEL_LSM_IRMS1, Units.AMPERE),
    IRMS2(ValueType.INTEGER, CHANNEL_LSM_IRMS2, Units.AMPERE),
    IRMS3(ValueType.INTEGER, CHANNEL_LSM_IRMS3, Units.AMPERE),
    URMS1(ValueType.INTEGER, CHANNEL_LSM_URMS1, Units.VOLT),
    URMS2(ValueType.INTEGER, CHANNEL_LSM_URMS2, Units.VOLT),
    URMS3(ValueType.INTEGER, CHANNEL_LSM_URMS3, Units.VOLT),
    PREF(ValueType.INTEGER, CHANNEL_LSM_PREF, Units.VOLT_AMPERE, 1000),
    PCOUP(ValueType.INTEGER, CHANNEL_LSM_PCOUP, Units.VOLT_AMPERE, 1000),
    SINSTS(ValueType.INTEGER, CHANNEL_LSM_SINSTS, Units.VOLT_AMPERE),
    SINSTS1(ValueType.INTEGER, CHANNEL_LSM_SINSTS1, Units.VOLT_AMPERE),
    SINSTS2(ValueType.INTEGER, CHANNEL_LSM_SINSTS2, Units.VOLT_AMPERE),
    SINSTS3(ValueType.INTEGER, CHANNEL_LSM_SINSTS3, Units.VOLT_AMPERE),
    SMAXSN(ValueType.INTEGER, CHANNEL_LSM_SMAXSN, CHANNEL_LSM_SMAXSN_DATE, Units.VOLT_AMPERE),
    SMAXSN1(ValueType.INTEGER, CHANNEL_LSM_SMAXSN1, CHANNEL_LSM_SMAXSN1_DATE, Units.VOLT_AMPERE),
    SMAXSN2(ValueType.INTEGER, CHANNEL_LSM_SMAXSN2, CHANNEL_LSM_SMAXSN2_DATE, Units.VOLT_AMPERE),
    SMAXSN3(ValueType.INTEGER, CHANNEL_LSM_SMAXSN3, CHANNEL_LSM_SMAXSN3_DATE, Units.VOLT_AMPERE),
    SMAXSN_MINUS_1(ValueType.INTEGER, CHANNEL_LSM_SMAXSN_MINUS_1, CHANNEL_LSM_SMAXSN_MINUS_1_DATE, Units.VOLT_AMPERE),
    SMAXSN1_MINUS_1(ValueType.INTEGER, CHANNEL_LSM_SMAXSN1_MINUS_1, CHANNEL_LSM_SMAXSN1_MINUS_1_DATE,
            Units.VOLT_AMPERE),
    SMAXSN2_MINUS_1(ValueType.INTEGER, CHANNEL_LSM_SMAXSN2_MINUS_1, CHANNEL_LSM_SMAXSN2_MINUS_1_DATE,
            Units.VOLT_AMPERE),
    SMAXSN3_MINUS_1(ValueType.INTEGER, CHANNEL_LSM_SMAXSN3_MINUS_1, CHANNEL_LSM_SMAXSN3_MINUS_1_DATE,
            Units.VOLT_AMPERE),
    SINSTI(ValueType.INTEGER, CHANNEL_LSM_SINSTI, Units.VOLT_AMPERE),
    SMAXIN(ValueType.INTEGER, CHANNEL_LSM_SMAXIN, CHANNEL_LSM_SMAXIN_DATE, Units.VOLT_AMPERE),
    SMAXIN_MINUS_1(ValueType.INTEGER, CHANNEL_LSM_SMAXIN_MINUS_1, CHANNEL_LSM_SMAXIN_MINUS_1_DATE, Units.VOLT_AMPERE),
    CCASN(ValueType.INTEGER, CHANNEL_LSM_CCASN, CHANNEL_LSM_CCASN_DATE, Units.WATT),
    CCASN_MINUS_1(ValueType.INTEGER, CHANNEL_LSM_CCASN_MINUS_1, CHANNEL_LSM_CCASN_MINUS_1_DATE, Units.WATT),
    CCAIN(ValueType.INTEGER, CHANNEL_LSM_CCAIN, CHANNEL_LSM_CCAIN_DATE, Units.WATT),
    CCAIN_MINUS_1(ValueType.INTEGER, CHANNEL_LSM_CCAIN_MINUS_1, CHANNEL_LSM_CCAIN_MINUS_1_DATE, Units.WATT),
    UMOY1(ValueType.INTEGER, CHANNEL_LSM_UMOY1, CHANNEL_LSM_UMOY1_DATE, Units.VOLT),
    UMOY2(ValueType.INTEGER, CHANNEL_LSM_UMOY2, CHANNEL_LSM_UMOY2_DATE, Units.VOLT),
    UMOY3(ValueType.INTEGER, CHANNEL_LSM_UMOY3, CHANNEL_LSM_UMOY3_DATE, Units.VOLT),
    STGE(ValueType.STRING, CHANNEL_LSM_STGE, Units.ONE),
    DPM1(ValueType.STRING, CHANNEL_LSM_DPM1, CHANNEL_LSM_DPM1_DATE, Units.ONE),
    FPM1(ValueType.STRING, CHANNEL_LSM_FPM1, CHANNEL_LSM_FPM1_DATE, Units.ONE),
    DPM2(ValueType.STRING, CHANNEL_LSM_DPM2, CHANNEL_LSM_DPM2_DATE, Units.ONE),
    FPM2(ValueType.STRING, CHANNEL_LSM_FPM2, CHANNEL_LSM_FPM2_DATE, Units.ONE),
    DPM3(ValueType.STRING, CHANNEL_LSM_DPM3, CHANNEL_LSM_DPM3_DATE, Units.ONE),
    FPM3(ValueType.STRING, CHANNEL_LSM_FPM3, CHANNEL_LSM_FPM3_DATE, Units.ONE),
    MSG1(ValueType.STRING, CHANNEL_LSM_MSG1, Units.ONE),
    MSG2(ValueType.STRING, CHANNEL_LSM_MSG2, Units.ONE),
    PRM(ValueType.STRING, CHANNEL_LSM_PRM, Units.ONE),
    RELAIS(ValueType.STRING, NOT_A_CHANNEL, Units.ONE),
    NTARF(ValueType.STRING, CHANNEL_LSM_NTARF, Units.ONE),
    NJOURF(ValueType.STRING, CHANNEL_LSM_NJOURF, Units.ONE),
    NJOURF_PLUS_1(ValueType.STRING, CHANNEL_LSM_NJOURF_PLUS_1, Units.ONE),
    PJOURF_PLUS_1(ValueType.STRING, CHANNEL_LSM_PJOURF_PLUS_1, Units.ONE),
    PPOINTE(ValueType.STRING, CHANNEL_LSM_PPOINTE, Units.ONE);

    private final ValueType type;
    private final String channelName;
    private final String timestampChannelName;
    private final Unit<?> unit;
    private final int factor;

    Label(ValueType type, String channelName, Unit<?> unit) {
        this(type, channelName, NOT_A_CHANNEL, unit, 1);
    }

    Label(ValueType type, String channelName, String timestampChannelName, Unit<?> unit) {
        this(type, channelName, timestampChannelName, unit, 1);
    }

    Label(ValueType type, String channelName, Unit<?> unit, int factor) {
        this(type, channelName, NOT_A_CHANNEL, unit, factor);
    }

    Label(ValueType type, String channelName, String timestampChannelName, Unit<?> unit, int factor) {
        this.type = type;
        this.channelName = channelName;
        this.timestampChannelName = timestampChannelName;
        this.unit = unit;
        this.factor = factor;
    }

    public ValueType getType() {
        return type;
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

    public static Label getEnum(String label) {
        String modifiedLabel = label.replace("-", "_MINUS_");
        modifiedLabel = modifiedLabel.replace("+", "_PLUS_");
        return valueOf(modifiedLabel);
    }
}
