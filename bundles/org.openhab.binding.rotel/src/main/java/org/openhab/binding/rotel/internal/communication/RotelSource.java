/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal.communication;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;

/**
 * Represents the different sources available for the Rotel device
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum RotelSource {

    CAT0_CD(0, "CD", "CD", RotelCommand.SOURCE_CD),

    CAT1_CD(1, "CD", "CD", RotelCommand.SOURCE_CD, RotelCommand.RECORD_SOURCE_CD, RotelCommand.MAIN_ZONE_SOURCE_CD,
            RotelCommand.ZONE2_SOURCE_CD, RotelCommand.ZONE3_SOURCE_CD, RotelCommand.ZONE4_SOURCE_CD),
    CAT1_TUNER(1, "TUNER", "TUNER", RotelCommand.SOURCE_TUNER, RotelCommand.RECORD_SOURCE_TUNER,
            RotelCommand.MAIN_ZONE_SOURCE_TUNER, RotelCommand.ZONE2_SOURCE_TUNER, RotelCommand.ZONE3_SOURCE_TUNER,
            RotelCommand.ZONE4_SOURCE_TUNER),
    CAT1_TAPE(1, "TAPE", "TAPE", RotelCommand.SOURCE_TAPE, RotelCommand.RECORD_SOURCE_TAPE,
            RotelCommand.MAIN_ZONE_SOURCE_TAPE, RotelCommand.ZONE2_SOURCE_TAPE, RotelCommand.ZONE3_SOURCE_TAPE,
            RotelCommand.ZONE4_SOURCE_TAPE),
    CAT1_VIDEO1(1, "VIDEO1", "VIDEO 1", RotelCommand.SOURCE_VIDEO1, RotelCommand.RECORD_SOURCE_VIDEO1,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO1, RotelCommand.ZONE2_SOURCE_VIDEO1, RotelCommand.ZONE3_SOURCE_VIDEO1,
            RotelCommand.ZONE4_SOURCE_VIDEO1),
    CAT1_VIDEO2(1, "VIDEO2", "VIDEO 2", RotelCommand.SOURCE_VIDEO2, RotelCommand.RECORD_SOURCE_VIDEO2,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO2, RotelCommand.ZONE2_SOURCE_VIDEO2, RotelCommand.ZONE3_SOURCE_VIDEO2,
            RotelCommand.ZONE4_SOURCE_VIDEO2),
    CAT1_VIDEO3(1, "VIDEO3", "VIDEO 3", RotelCommand.SOURCE_VIDEO3, RotelCommand.RECORD_SOURCE_VIDEO3,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO3, RotelCommand.ZONE2_SOURCE_VIDEO3, RotelCommand.ZONE3_SOURCE_VIDEO3,
            RotelCommand.ZONE4_SOURCE_VIDEO3),
    CAT1_VIDEO4(1, "VIDEO4", "VIDEO 4", RotelCommand.SOURCE_VIDEO4, RotelCommand.RECORD_SOURCE_VIDEO4,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO4, RotelCommand.ZONE2_SOURCE_VIDEO4, RotelCommand.ZONE3_SOURCE_VIDEO4,
            RotelCommand.ZONE4_SOURCE_VIDEO4),
    CAT1_VIDEO5(1, "VIDEO5", "VIDEO 5", RotelCommand.SOURCE_VIDEO5, RotelCommand.RECORD_SOURCE_VIDEO5,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO5, RotelCommand.ZONE2_SOURCE_VIDEO5, RotelCommand.ZONE3_SOURCE_VIDEO5,
            RotelCommand.ZONE4_SOURCE_VIDEO5),
    CAT1_MULTI(1, "MULTI", "MULTI", RotelCommand.SOURCE_MULTI_INPUT, null, RotelCommand.MAIN_ZONE_SOURCE_MULTI_INPUT,
            null, null, null),
    CAT1_FOLLOW_MAIN(1, "MAIN", "Follow Main Zone Source", null, RotelCommand.RECORD_SOURCE_MAIN, null,
            RotelCommand.ZONE2_SOURCE_MAIN, RotelCommand.ZONE3_SOURCE_MAIN, RotelCommand.ZONE4_SOURCE_MAIN),

    CAT2_CD(2, "CD", "CD", RotelCommand.SOURCE_CD, RotelCommand.RECORD_SOURCE_CD, RotelCommand.MAIN_ZONE_SOURCE_CD,
            RotelCommand.ZONE2_SOURCE_CD, RotelCommand.ZONE3_SOURCE_CD, RotelCommand.ZONE4_SOURCE_CD),
    CAT2_TUNER(2, "TUNER", "TUNER", RotelCommand.SOURCE_TUNER, RotelCommand.RECORD_SOURCE_TUNER,
            RotelCommand.MAIN_ZONE_SOURCE_TUNER, RotelCommand.ZONE2_SOURCE_TUNER, RotelCommand.ZONE3_SOURCE_TUNER,
            RotelCommand.ZONE4_SOURCE_TUNER),
    CAT2_VIDEO1(2, "VIDEO1", "VIDEO 1", RotelCommand.SOURCE_VIDEO1, RotelCommand.RECORD_SOURCE_VIDEO1,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO1, RotelCommand.ZONE2_SOURCE_VIDEO1, RotelCommand.ZONE3_SOURCE_VIDEO1,
            RotelCommand.ZONE4_SOURCE_VIDEO1),
    CAT2_VIDEO2(2, "VIDEO2", "VIDEO 2", RotelCommand.SOURCE_VIDEO2, RotelCommand.RECORD_SOURCE_VIDEO2,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO2, RotelCommand.ZONE2_SOURCE_VIDEO2, RotelCommand.ZONE3_SOURCE_VIDEO2,
            RotelCommand.ZONE4_SOURCE_VIDEO2),
    CAT2_VIDEO3(2, "VIDEO3", "VIDEO 3", RotelCommand.SOURCE_VIDEO3, RotelCommand.RECORD_SOURCE_VIDEO3,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO3, RotelCommand.ZONE2_SOURCE_VIDEO3, RotelCommand.ZONE3_SOURCE_VIDEO3,
            RotelCommand.ZONE4_SOURCE_VIDEO3),
    CAT2_VIDEO4(2, "VIDEO4", "VIDEO 4", RotelCommand.SOURCE_VIDEO4, RotelCommand.RECORD_SOURCE_VIDEO4,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO4, RotelCommand.ZONE2_SOURCE_VIDEO4, RotelCommand.ZONE3_SOURCE_VIDEO4,
            RotelCommand.ZONE4_SOURCE_VIDEO4),
    CAT2_VIDEO5(2, "VIDEO5", "VIDEO 5", RotelCommand.SOURCE_VIDEO5, RotelCommand.RECORD_SOURCE_VIDEO5,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO5, RotelCommand.ZONE2_SOURCE_VIDEO5, RotelCommand.ZONE3_SOURCE_VIDEO5,
            RotelCommand.ZONE4_SOURCE_VIDEO5),
    CAT2_VIDEO6(2, "VIDEO6", "VIDEO 6", RotelCommand.SOURCE_VIDEO6, RotelCommand.RECORD_SOURCE_VIDEO6,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO6, RotelCommand.ZONE2_SOURCE_VIDEO6, RotelCommand.ZONE3_SOURCE_VIDEO6,
            RotelCommand.ZONE4_SOURCE_VIDEO6),
    CAT2_USB(2, "USB", "USB", RotelCommand.SOURCE_USB, RotelCommand.RECORD_SOURCE_USB,
            RotelCommand.MAIN_ZONE_SOURCE_USB, RotelCommand.ZONE2_SOURCE_USB, RotelCommand.ZONE3_SOURCE_USB,
            RotelCommand.ZONE4_SOURCE_USB),
    CAT2_MULTI(2, "MULTI", "MULTI", RotelCommand.SOURCE_MULTI_INPUT, null, RotelCommand.MAIN_ZONE_SOURCE_MULTI_INPUT,
            null, null, null),
    CAT2_FOLLOW_MAIN(2, "MAIN", "Follow Main Zone Source", null, RotelCommand.RECORD_SOURCE_MAIN, null,
            RotelCommand.ZONE2_SOURCE_MAIN, RotelCommand.ZONE3_SOURCE_MAIN, RotelCommand.ZONE4_SOURCE_MAIN),

    CAT3_CD(3, "CD", "CD", RotelCommand.SOURCE_CD, RotelCommand.RECORD_SOURCE_CD, RotelCommand.MAIN_ZONE_SOURCE_CD,
            RotelCommand.ZONE2_SOURCE_CD, RotelCommand.ZONE3_SOURCE_CD, RotelCommand.ZONE4_SOURCE_CD),
    CAT3_TUNER(3, "TUNER", "TUNER", RotelCommand.SOURCE_TUNER, RotelCommand.RECORD_SOURCE_TUNER,
            RotelCommand.MAIN_ZONE_SOURCE_TUNER, RotelCommand.ZONE2_SOURCE_TUNER, RotelCommand.ZONE3_SOURCE_TUNER,
            RotelCommand.ZONE4_SOURCE_TUNER),
    CAT3_TAPE(3, "TAPE", "TAPE", RotelCommand.SOURCE_TAPE, RotelCommand.RECORD_SOURCE_TAPE,
            RotelCommand.MAIN_ZONE_SOURCE_TAPE, RotelCommand.ZONE2_SOURCE_TAPE, RotelCommand.ZONE3_SOURCE_TAPE,
            RotelCommand.ZONE4_SOURCE_TAPE),
    CAT3_VIDEO1(3, "VIDEO1", "VIDEO 1", RotelCommand.SOURCE_VIDEO1, RotelCommand.RECORD_SOURCE_VIDEO1,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO1, RotelCommand.ZONE2_SOURCE_VIDEO1, RotelCommand.ZONE3_SOURCE_VIDEO1,
            RotelCommand.ZONE4_SOURCE_VIDEO1),
    CAT3_VIDEO2(3, "VIDEO2", "VIDEO 2", RotelCommand.SOURCE_VIDEO2, RotelCommand.RECORD_SOURCE_VIDEO2,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO2, RotelCommand.ZONE2_SOURCE_VIDEO2, RotelCommand.ZONE3_SOURCE_VIDEO2,
            RotelCommand.ZONE4_SOURCE_VIDEO2),
    CAT3_VIDEO3(3, "VIDEO3", "VIDEO 3", RotelCommand.SOURCE_VIDEO3, RotelCommand.RECORD_SOURCE_VIDEO3,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO3, RotelCommand.ZONE2_SOURCE_VIDEO3, RotelCommand.ZONE3_SOURCE_VIDEO3,
            RotelCommand.ZONE4_SOURCE_VIDEO3),
    CAT3_VIDEO4(3, "VIDEO4", "VIDEO 4", RotelCommand.SOURCE_VIDEO4, RotelCommand.RECORD_SOURCE_VIDEO4,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO4, RotelCommand.ZONE2_SOURCE_VIDEO4, RotelCommand.ZONE3_SOURCE_VIDEO4,
            RotelCommand.ZONE4_SOURCE_VIDEO4),
    CAT3_VIDEO5(3, "VIDEO5", "VIDEO 5", RotelCommand.SOURCE_VIDEO5, RotelCommand.RECORD_SOURCE_VIDEO5,
            RotelCommand.MAIN_ZONE_SOURCE_VIDEO5, RotelCommand.ZONE2_SOURCE_VIDEO5, RotelCommand.ZONE3_SOURCE_VIDEO5,
            RotelCommand.ZONE4_SOURCE_VIDEO5),
    CAT3_MULTI(3, "MULTI", "MULTI", RotelCommand.SOURCE_MULTI_INPUT, null, RotelCommand.MAIN_ZONE_SOURCE_MULTI_INPUT,
            null, null, null),

    CAT4_CD(4, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT4_AUX1(4, "AUX1", "Aux 1", RotelCommand.SOURCE_AUX1),
    CAT4_TUNER(4, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT4_PHONO(4, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT4_BLUETOOTH(4, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),

    CAT5_CD(5, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT5_COAX1(5, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT5_COAX2(5, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT5_OPTICAL1(5, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT5_OPTICAL2(5, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT5_AUX1(5, "AUX1", "Aux 1", RotelCommand.SOURCE_AUX1),
    CAT5_AUX2(5, "AUX2", "Aux 2", RotelCommand.SOURCE_AUX2),
    CAT5_TUNER(5, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT5_PHONO(5, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT5_USB(5, "USB", "Front USB", RotelCommand.SOURCE_USB),
    CAT5_PCUSB(5, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),
    CAT5_BLUETOOTH(5, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),

    CAT6_RCD(6, "RCD", "Rotel CD", RotelCommand.SOURCE_ROTEL_CD),
    CAT6_CD(6, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT6_COAX1(6, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT6_COAX2(6, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT6_OPTICAL1(6, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT6_OPTICAL2(6, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT6_AUX1(6, "AUX1", "Aux 1", RotelCommand.SOURCE_AUX1),
    CAT6_AUX2(6, "AUX2", "Aux 2", RotelCommand.SOURCE_AUX2),
    CAT6_TUNER(6, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT6_PHONO(6, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT6_USB(6, "USB", "Front USB", RotelCommand.SOURCE_USB),

    CAT7_RCD(7, "RCD", "Rotel CD", RotelCommand.SOURCE_ROTEL_CD),
    CAT7_CD(7, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT7_COAX1(7, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT7_COAX2(7, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT7_OPTICAL1(7, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT7_OPTICAL2(7, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT7_AUX1(7, "AUX1", "Aux 1", RotelCommand.SOURCE_AUX1),
    CAT7_AUX2(7, "AUX2", "Aux 2", RotelCommand.SOURCE_AUX2),
    CAT7_TUNER(7, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT7_PHONO(7, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT7_USB(7, "USB", "Front USB", RotelCommand.SOURCE_USB),
    CAT7_PCUSB(7, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),
    CAT7_XLR(7, "XLR", "XLR", RotelCommand.SOURCE_XLR),

    CAT8_CD(8, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT8_COAX1(8, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT8_COAX2(8, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT8_OPTICAL1(8, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT8_OPTICAL2(8, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT8_AUX(8, "AUX", "Aux", RotelCommand.SOURCE_AUX),
    CAT8_TUNER(8, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT8_PHONO(8, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT8_USB(8, "USB", "Front USB", RotelCommand.SOURCE_USB),
    CAT8_PCUSB(8, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),
    CAT8_BLUETOOTH(8, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),
    CAT8_XLR(8, "XLR", "XLR", RotelCommand.SOURCE_XLR),

    CAT9_RCD(9, "RCD", "Rotel CD", RotelCommand.SOURCE_ROTEL_CD),
    CAT9_CD(9, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT9_COAX1(9, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT9_COAX2(9, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT9_COAX3(9, "COAX3", "Coax 3", RotelCommand.SOURCE_COAX3),
    CAT9_OPTICAL1(9, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT9_OPTICAL2(9, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT9_OPTICAL3(9, "OPTICAL3", "Optical 3", RotelCommand.SOURCE_OPT3),
    CAT9_AUX(9, "AUX", "Aux", RotelCommand.SOURCE_AUX),
    CAT9_TUNER(9, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT9_PHONO(9, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT9_USB(9, "USB", "Front USB", RotelCommand.SOURCE_USB),
    CAT9_PCUSB(9, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),
    CAT9_BLUETOOTH(9, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),
    CAT9_XLR(9, "XLR", "XLR", RotelCommand.SOURCE_XLR),

    CAT10_CD(10, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT10_TUNER(10, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT10_PHONO(10, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT10_VIDEO1(10, "VIDEO1", "VIDEO 1", RotelCommand.SOURCE_VIDEO1),
    CAT10_VIDEO2(10, "VIDEO2", "VIDEO 2", RotelCommand.SOURCE_VIDEO2),
    CAT10_VIDEO3(10, "VIDEO3", "VIDEO 3", RotelCommand.SOURCE_VIDEO3),
    CAT10_VIDEO4(10, "VIDEO4", "VIDEO 4", RotelCommand.SOURCE_VIDEO4),
    CAT10_VIDEO5(10, "VIDEO5", "VIDEO 5", RotelCommand.SOURCE_VIDEO5),
    CAT10_VIDEO6(10, "VIDEO6", "VIDEO 6", RotelCommand.SOURCE_VIDEO6),
    CAT10_VIDEO7(10, "VIDEO7", "VIDEO 7", RotelCommand.SOURCE_VIDEO7),
    CAT10_USB(10, "USB", "Front USB", RotelCommand.SOURCE_USB),
    CAT10_PCUSB(10, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),
    CAT10_BLUETOOTH(10, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),
    CAT10_XLR(10, "XLR", "XLR", RotelCommand.SOURCE_XLR),
    CAT10_MULTI(10, "MULTI", "MULTI", RotelCommand.SOURCE_MULTI_INPUT),

    CAT11_CD(11, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT11_TUNER(11, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT11_PHONO(11, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT11_VIDEO1(11, "VIDEO1", "VIDEO 1", RotelCommand.SOURCE_VIDEO1),
    CAT11_VIDEO2(11, "VIDEO2", "VIDEO 2", RotelCommand.SOURCE_VIDEO2),
    CAT11_VIDEO3(11, "VIDEO3", "VIDEO 3", RotelCommand.SOURCE_VIDEO3),
    CAT11_VIDEO4(11, "VIDEO4", "VIDEO 4", RotelCommand.SOURCE_VIDEO4),
    CAT11_VIDEO5(11, "VIDEO5", "VIDEO 5", RotelCommand.SOURCE_VIDEO5),
    CAT11_VIDEO6(11, "VIDEO6", "VIDEO 6", RotelCommand.SOURCE_VIDEO6),
    CAT11_VIDEO7(11, "VIDEO7", "VIDEO 7", RotelCommand.SOURCE_VIDEO7),
    CAT11_VIDEO8(11, "VIDEO8", "VIDEO 8", RotelCommand.SOURCE_VIDEO8),
    CAT11_USB(11, "USB", "Front USB", RotelCommand.SOURCE_USB),
    CAT11_PCUSB(11, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),
    CAT11_BLUETOOTH(11, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),
    CAT11_XLR(11, "XLR", "XLR", RotelCommand.SOURCE_XLR),
    CAT11_MULTI(11, "MULTI", "MULTI", RotelCommand.SOURCE_MULTI_INPUT),

    CAT12_FM(12, "FM", "FM", RotelCommand.SOURCE_FM),
    CAT12_DAB(12, "DAB", "DAB", RotelCommand.SOURCE_DAB),

    CAT13_FM(13, "FM", "FM", RotelCommand.SOURCE_FM),
    CAT13_DAB(13, "DAB", "DAB", RotelCommand.SOURCE_DAB),
    CAT13_PLAYFI(13, "PLAYFI", "PlayFi", RotelCommand.SOURCE_PLAYFI),

    CAT14_FM(14, "FM", "FM", RotelCommand.SOURCE_FM),
    CAT14_DAB(14, "DAB", "DAB", RotelCommand.SOURCE_DAB),
    CAT14_IRADIO(14, "IRADIO", "iRadio", RotelCommand.SOURCE_IRADIO),
    CAT14_NETWORK(14, "NETWORK", "Network", RotelCommand.SOURCE_NETWORK),

    CAT15_COAX1(15, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT15_COAX2(15, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT15_OPTICAL1(15, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT15_OPTICAL2(15, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT15_USB(15, "USB", "Front USB", RotelCommand.SOURCE_USB),
    CAT15_PCUSB(15, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),

    CAT16_IRADIO(16, "IRADIO", "iRadio", RotelCommand.SOURCE_IRADIO),
    CAT16_NETWORK(16, "NETWORK", "Network", RotelCommand.SOURCE_NETWORK),
    CAT16_AUX1_COAX(16, "AUX1_COAX", "Aux 1 Coax", RotelCommand.SOURCE_AUX1_COAX),
    CAT16_AUX1_OPTICAL(16, "AUX1_OPTICAL", "Aux 1 Optical", RotelCommand.SOURCE_AUX1_OPT),
    CAT16_FM(16, "FM", "FM", RotelCommand.SOURCE_FM),
    CAT16_DAB(16, "DAB", "DAB", RotelCommand.SOURCE_DAB),
    CAT16_USB(16, "USB", "USB", RotelCommand.SOURCE_USB),

    CAT17_CD(17, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT17_IRADIO(17, "IRADIO", "iRadio", RotelCommand.SOURCE_IRADIO),
    CAT17_NETWORK(17, "NETWORK", "Network", RotelCommand.SOURCE_NETWORK),
    CAT17_AUX1_COAX(17, "AUX1_COAX", "Aux 1 Coax", RotelCommand.SOURCE_AUX1_COAX),
    CAT17_AUX1_OPTICAL(17, "AUX1_OPTICAL", "Aux 1 Optical", RotelCommand.SOURCE_AUX1_OPT),
    CAT17_AUX2(17, "AUX2", "Aux 2", RotelCommand.SOURCE_AUX2),
    CAT17_FM(17, "FM", "FM", RotelCommand.SOURCE_FM),
    CAT17_DAB(17, "DAB", "DAB", RotelCommand.SOURCE_DAB),
    CAT17_USB(17, "USB", "USB", RotelCommand.SOURCE_USB),

    CAT18_CD(18, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT18_COAX1(18, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT18_COAX2(18, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT18_COAX3(18, "COAX3", "Coax 3", RotelCommand.SOURCE_COAX3),
    CAT18_OPTICAL1(18, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT18_OPTICAL2(18, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT18_OPTICAL3(18, "OPTICAL3", "Optical 3", RotelCommand.SOURCE_OPT3),
    CAT18_AUX(18, "AUX", "Aux", RotelCommand.SOURCE_AUX),
    CAT18_TUNER(18, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT18_PHONO(18, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT18_BLUETOOTH(18, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),
    CAT18_XLR(18, "XLR", "XLR", RotelCommand.SOURCE_XLR),
    CAT18_PCUSB(18, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),

    CAT19_CD(19, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT19_COAX1(19, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT19_COAX2(19, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT19_COAX3(19, "COAX3", "Coax 3", RotelCommand.SOURCE_COAX3),
    CAT19_OPTICAL1(19, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT19_OPTICAL2(19, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT19_OPTICAL3(19, "OPTICAL3", "Optical 3", RotelCommand.SOURCE_OPT3),
    CAT19_AUX1(19, "AUX1", "Aux 1", RotelCommand.SOURCE_AUX1),
    CAT19_AUX2(19, "AUX2", "Aux 2", RotelCommand.SOURCE_AUX2),
    CAT19_TUNER(19, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT19_PHONO(19, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT19_BLUETOOTH(19, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),
    CAT19_XLR(19, "XLR", "XLR", RotelCommand.SOURCE_XLR),
    CAT19_PCUSB(19, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),

    CAT20_CD(20, "CD", "CD", RotelCommand.SOURCE_CD),
    CAT20_COAX1(20, "COAX1", "Coax 1", RotelCommand.SOURCE_COAX1),
    CAT20_COAX2(20, "COAX2", "Coax 2", RotelCommand.SOURCE_COAX2),
    CAT20_COAX3(20, "COAX3", "Coax 3", RotelCommand.SOURCE_COAX3),
    CAT20_OPTICAL1(20, "OPTICAL1", "Optical 1", RotelCommand.SOURCE_OPT1),
    CAT20_OPTICAL2(20, "OPTICAL2", "Optical 2", RotelCommand.SOURCE_OPT2),
    CAT20_OPTICAL3(20, "OPTICAL3", "Optical 3", RotelCommand.SOURCE_OPT3),
    CAT20_AUX1(20, "AUX1", "Aux 1", RotelCommand.SOURCE_AUX1),
    CAT20_AUX2(20, "AUX2", "Aux 2", RotelCommand.SOURCE_AUX2),
    CAT20_TUNER(20, "TUNER", "Tuner", RotelCommand.SOURCE_TUNER),
    CAT20_PHONO(20, "PHONO", "Phono", RotelCommand.SOURCE_PHONO),
    CAT20_BLUETOOTH(20, "BLUETOOTH", "Bluetooth", RotelCommand.SOURCE_BLUETOOTH),
    CAT20_XLR1(20, "XLR1", "XLR 1", RotelCommand.SOURCE_XLR1),
    CAT20_XLR2(20, "XLR2", "XLR 2", RotelCommand.SOURCE_XLR1),
    CAT20_PCUSB(20, "PCUSB", "PC USB", RotelCommand.SOURCE_PCUSB),

    CAT21_INPUTA(21, "INPUTA", "Input A", RotelCommand.SOURCE_INPUT_A, null, RotelCommand.ZONE1_SOURCE_INPUT_A,
            RotelCommand.ZONE2_SOURCE_INPUT_A, RotelCommand.ZONE3_SOURCE_INPUT_A, RotelCommand.ZONE4_SOURCE_INPUT_A),
    CAT21_INPUTB(21, "INPUTB", "Input B", RotelCommand.SOURCE_INPUT_B, null, RotelCommand.ZONE1_SOURCE_INPUT_B,
            RotelCommand.ZONE2_SOURCE_INPUT_B, RotelCommand.ZONE3_SOURCE_INPUT_B, RotelCommand.ZONE4_SOURCE_INPUT_B),
    CAT21_INPUTC(21, "INPUTC", "Input C", RotelCommand.SOURCE_INPUT_C, null, RotelCommand.ZONE1_SOURCE_INPUT_C,
            RotelCommand.ZONE2_SOURCE_INPUT_C, RotelCommand.ZONE3_SOURCE_INPUT_C, RotelCommand.ZONE4_SOURCE_INPUT_C),
    CAT21_INPUTD(21, "INPUTD", "Input D", RotelCommand.SOURCE_INPUT_D, null, RotelCommand.ZONE1_SOURCE_INPUT_D,
            RotelCommand.ZONE2_SOURCE_INPUT_D, RotelCommand.ZONE3_SOURCE_INPUT_D, RotelCommand.ZONE4_SOURCE_INPUT_D);

    private int category;
    private String name;
    private String label;
    private @Nullable RotelCommand command;
    private @Nullable RotelCommand recordCommand;
    private @Nullable RotelCommand zone1Command;
    private @Nullable RotelCommand zone2Command;
    private @Nullable RotelCommand zone3Command;
    private @Nullable RotelCommand zone4Command;

    /**
     * Constructor
     *
     * @param category a category of models for which the source is available
     * @param name the name of the source
     * @param label the label of the source
     * @param command the command to select the source
     */
    private RotelSource(int category, String name, String label, @Nullable RotelCommand command) {
        this(category, name, label, command, null, null, null, null, null);
    }

    /**
     * Constructor
     *
     * @param category a category of models for which the source is available
     * @param name the name of the source
     * @param label the label of the source
     * @param command the command to select the source
     * @param recordCommand the command to select the source as source to be recorded
     * @param zone1Command the command to select the source in the zone 1 or main zone
     * @param zone2Command the command to select the source in the zone 2
     * @param zone3Command the command to select the source in the zone 3
     * @param zone4Command the command to select the source in the zone 4
     */
    private RotelSource(int category, String name, String label, @Nullable RotelCommand command,
            @Nullable RotelCommand recordCommand, @Nullable RotelCommand zone1Command,
            @Nullable RotelCommand zone2Command, @Nullable RotelCommand zone3Command,
            @Nullable RotelCommand zone4Command) {
        this.category = category;
        this.name = name;
        this.label = label;
        this.command = command;
        this.recordCommand = recordCommand;
        this.zone1Command = zone1Command;
        this.zone2Command = zone2Command;
        this.zone3Command = zone3Command;
        this.zone4Command = zone4Command;
    }

    /**
     * Get the category of models for the source
     *
     * @return the category of models
     */
    public int getCategory() {
        return category;
    }

    /**
     * Get the name of the source
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the label of the source
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the command to select the source
     *
     * @return the command
     */
    public @Nullable RotelCommand getCommand() {
        return command;
    }

    /**
     * Get the command to select the source as source to be recorded
     *
     * @return the command
     */
    public @Nullable RotelCommand getRecordCommand() {
        return recordCommand;
    }

    /**
     * Get the command to select the source in a zone
     *
     * @param numZone the zone number, 1 for main zone or zone 1, 2 for zone 2, 3 for zone 3, 4 for zone 4
     *
     * @return the command
     */
    public @Nullable RotelCommand getZoneCommand(int numZone) {
        switch (numZone) {
            case 1:
                return zone1Command;
            case 2:
                return zone2Command;
            case 3:
                return zone3Command;
            case 4:
                return zone4Command;
            default:
                throw new IllegalArgumentException("numZone must be a value between 1 and 4");
        }
    }

    /**
     * Get the list of {@link RotelSource} available for a particular category of models
     *
     * @param category a category of models
     * @param type a source type (0 for global source, 1 for main zone or zone 1, 2 for zone 2, 3 for zone 3, 4 for zone
     *            4 and 5 for record source)
     *
     * @return the list of {@link RotelSource} available in a zone for a provided category of models
     */
    public static List<RotelSource> getSources(int category, int type) {
        List<RotelSource> sources = new ArrayList<>();
        for (RotelSource value : RotelSource.values()) {
            if (value.getCategory() == category && ((type == 0 && value.getCommand() != null)
                    || (type == 1 && value.getZoneCommand(1) != null) || (type == 2 && value.getZoneCommand(2) != null)
                    || (type == 3 && value.getZoneCommand(3) != null) || (type == 4 && value.getZoneCommand(4) != null)
                    || (type == 5 && value.getRecordCommand() != null))) {
                sources.add(value);
            }
        }
        return sources;
    }

    /**
     * Get the source associated to a name for a particular category of models
     *
     * @param category a category of models
     * @param name the name used to identify the source
     *
     * @return the source associated to the searched name for the provided category of models
     *
     * @throws RotelException - If no source is associated to the searched name for the provided category
     */
    public static RotelSource getFromName(int category, String name) throws RotelException {
        for (RotelSource value : RotelSource.values()) {
            if (value.getCategory() == category && value.getName().equals(name)) {
                return value;
            }
        }
        throw new RotelException("Invalid name for a source: " + name);
    }

    /**
     * Get the source associated to a command for a particular category of models
     *
     * @param category a category of models
     * @param command the command used to identify the source
     * @param type a source type (0 for global source, 1 for main zone or zone 1, 2 for zone 2, 3 for zone 3,
     *            4 for zone 4 and 5 for record source)
     *
     * @return the source associated to the searched command for the provided category of models
     *
     * @throws RotelException - If no source is associated to the searched command for the provided category
     */
    public static RotelSource getFromCommand(int category, RotelCommand command, int type) throws RotelException {
        for (RotelSource value : RotelSource.values()) {
            if (value.getCategory() == category && ((type == 0 && value.getCommand() == command)
                    || (type == 1 && value.getZoneCommand(1) == command)
                    || (type == 2 && value.getZoneCommand(2) == command)
                    || (type == 3 && value.getZoneCommand(3) == command)
                    || (type == 4 && value.getZoneCommand(4) == command)
                    || (type == 5 && value.getRecordCommand() == command))) {
                return value;
            }
        }
        throw new RotelException("Invalid command for a source: " + command.getLabel());
    }
}
