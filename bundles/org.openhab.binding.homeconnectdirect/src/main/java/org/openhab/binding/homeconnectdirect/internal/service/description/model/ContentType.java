/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.service.description.model;

import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Content type model from the device description.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public enum ContentType {
    UNKNOWN(0, "unknown", ProtocolType.STRING),
    BOOLEAN(0x01, "boolean", ProtocolType.BOOLEAN),
    INTEGER(0x02, "integer", ProtocolType.INTEGER),
    ENUMERATION(0x03, "enumeration", ProtocolType.INTEGER),
    FLOAT(0x04, "float", ProtocolType.FLOAT),
    STRING(0x05, "string", ProtocolType.STRING),
    DATE_TIME(0x06, "dateTime", ProtocolType.STRING),
    TEMPERATURE_CELSIUS(0x07, "temperatureCelsius", ProtocolType.FLOAT),
    TEMPERATURE_FAHRENHEIT(0x08, "temperatureFahrenheit", ProtocolType.FLOAT),
    HEX_BINARY(0x0A, "hexBinary", ProtocolType.STRING),
    TIME_SPAN(0x10, "timeSpan", ProtocolType.INTEGER),
    PERCENT(0x11, "percent", ProtocolType.FLOAT),
    DBM(0x12, "dbm", ProtocolType.INTEGER),
    WEIGHT(0x13, "weight", ProtocolType.INTEGER),
    LIQUID_VOLUME(0x14, "liquidVolume", ProtocolType.INTEGER),
    UID_VALUE(0x15, "uidValue", ProtocolType.INTEGER),
    DATE(0x16, "date", ProtocolType.STRING),
    TIME(0x17, "time", ProtocolType.STRING),
    WATER_HARDNESS(0x18, "waterHardness", ProtocolType.INTEGER),
    POINT_2D(0x19, "point2D", ProtocolType.OBJECT),
    POSE_2D(0x1A, "pose2D", ProtocolType.OBJECT),
    LINE_2D(0x1B, "line2D", ProtocolType.OBJECT),
    RGB(0x1E, "rgb", ProtocolType.STRING),
    RPM(0x1F, "rpm", ProtocolType.INTEGER),
    FLOW_RATE(0x20, "flowRate", ProtocolType.INTEGER),
    LENGTH(0x21, "length", ProtocolType.FLOAT),
    AREA(0x22, "area", ProtocolType.FLOAT),
    POWER(0x23, "power", ProtocolType.FLOAT),
    ENERGY(0x24, "energy", ProtocolType.FLOAT),
    BIG_INTEGER(0x25, "bigInteger", ProtocolType.INTEGER),
    IDENTIFIER(0x26, "identifier", ProtocolType.INTEGER),
    SPEED(0x27, "speed", ProtocolType.FLOAT),
    PROGRAM_INSTRUCTION(0x28, "programInstruction", ProtocolType.OBJECT),
    WEIGHT_POUND(0x29, "weightPound", ProtocolType.FLOAT),
    LOCALE_STRING(0x2A, "localeString", ProtocolType.OBJECT),
    TEASPOON(0x2B, "teaspoon", ProtocolType.FLOAT),
    TABLESPOON(0x2C, "tablespoon", ProtocolType.FLOAT),
    CUP(0x2D, "cup", ProtocolType.FLOAT),
    PIECE(0x2E, "piece", ProtocolType.FLOAT),
    BYTE_LENGTH(0x2F, "byteLength", ProtocolType.INTEGER),
    UUID(0x30, "uuid", ProtocolType.STRING),
    TIMEZONE(0x31, "timezone", ProtocolType.OBJECT),
    CSV(0x32, "csv", ProtocolType.STRING),
    LEAF(0x33, "leaf", ProtocolType.FLOAT),
    BUNCH(0x34, "bunch", ProtocolType.FLOAT),
    CASKET(0x35, "casket", ProtocolType.FLOAT),
    PINCH(0x36, "pinch", ProtocolType.FLOAT),
    STALK(0x37, "stalk", ProtocolType.FLOAT),
    STICK(0x38, "stick", ProtocolType.FLOAT),
    BRANCH(0x39, "branch", ProtocolType.FLOAT),
    TRAY(0x3A, "tray", ProtocolType.INTEGER),
    PORTION(0x3B, "portion", ProtocolType.INTEGER),
    UTC_DATE_TIME(0x3D, "utcDateTime", ProtocolType.STRING),
    PROGRAM_RUN_SUMMARY(0x3E, "programRunSummary", ProtocolType.OBJECT),
    PROGRAM_SESSION_SUMMARY(0x3F, "programSessionSummary", ProtocolType.OBJECT),
    LIQUID_VOLUME_THROUGHPUT(0x40, "liquidVolumeThroughput", ProtocolType.FLOAT),
    WEIGHT_OUNCES(0x41, "weightOunces", ProtocolType.FLOAT),
    RECTANGLE_2D(0x42, "rectangle2D", ProtocolType.OBJECT),
    BOOLEAN_LIST(0x81, "booleanList", ProtocolType.OBJECT),
    INTEGER_LIST(0x82, "integerList", ProtocolType.OBJECT),
    ENUMERATION_LIST(0x83, "enumerationList", ProtocolType.OBJECT),
    FLOAT_LIST(0x84, "floatList", ProtocolType.OBJECT),
    STRING_LIST(0x85, "stringList", ProtocolType.OBJECT),
    DATE_TIME_LIST(0x86, "dateTimeList", ProtocolType.OBJECT),
    TEMPERATURE_CELSIUS_LIST(0x87, "temperatureCelsiusList", ProtocolType.OBJECT),
    TEMPERATURE_FAHRENHEIT_LIST(0x88, "temperatureFahrenheitList", ProtocolType.OBJECT),
    HEX_BINARY_LIST(0x8A, "hexBinaryList", ProtocolType.OBJECT),
    TIME_SPAN_LIST(0x90, "timeSpanList", ProtocolType.OBJECT),
    PERCENT_LIST(0x91, "percentList", ProtocolType.OBJECT),
    DBM_LIST(0x92, "dBmList", ProtocolType.OBJECT),
    WEIGHT_LIST(0x93, "weightList", ProtocolType.OBJECT),
    LIQUID_VOLUME_LIST(0x94, "liquidVolumeList", ProtocolType.OBJECT),
    UID_VALUE_LIST(0x95, "uidValueList", ProtocolType.OBJECT),
    DATE_LIST(0x96, "dateList", ProtocolType.OBJECT),
    TIME_LIST(0x97, "timeList", ProtocolType.OBJECT),
    WATER_HARDNESS_LIST(0x98, "waterHardnessList", ProtocolType.OBJECT),
    POINT_2D_LIST(0x99, "point2DList", ProtocolType.OBJECT),
    POSE_2D_LIST(0x9A, "pose2DList", ProtocolType.OBJECT),
    LINE_2D_LIST(0x9B, "line2DList", ProtocolType.OBJECT),
    PATH(0x9C, "path", ProtocolType.OBJECT),
    POLYGON(0x9D, "polygon", ProtocolType.OBJECT),
    RGB_LIST(0x9E, "rgbList", ProtocolType.OBJECT),
    RPM_LIST(0x9F, "rpmList", ProtocolType.OBJECT),
    FLOW_RATE_LIST(0xA0, "flowRateList", ProtocolType.OBJECT),
    LENGTH_LIST(0xA1, "lengthList", ProtocolType.OBJECT),
    AREA_LIST(0xA2, "areaList", ProtocolType.OBJECT),
    POWER_LIST(0xA3, "powerList", ProtocolType.OBJECT),
    ENERGY_LIST(0xA4, "energyList", ProtocolType.OBJECT),
    BIG_INTEGER_LIST(0xA5, "bigIntegerList", ProtocolType.OBJECT),
    IDENTIFIER_LIST(0xA6, "identifierList", ProtocolType.OBJECT),
    SPEED_LIST(0xA7, "speedList", ProtocolType.OBJECT),
    PROGRAM_INSTRUCTION_LIST(0xA8, "programInstructionList", ProtocolType.OBJECT),
    WEIGHT_POUND_LIST(0xA9, "weightPoundList", ProtocolType.OBJECT),
    LOCALE_STRING_LIST(0xAA, "localeStringList", ProtocolType.OBJECT),
    TEASPOON_LIST(0xAB, "teaspoonList", ProtocolType.OBJECT),
    TABLESPOON_LIST(0xAC, "tablespoonList", ProtocolType.OBJECT),
    CUP_LIST(0xAD, "cupList", ProtocolType.OBJECT),
    PIECE_LIST(0xAE, "pieceList", ProtocolType.OBJECT),
    BYTE_LENGTH_LIST(0xAF, "byteLengthList", ProtocolType.OBJECT),
    UUID_LIST(0xB0, "uuidList", ProtocolType.OBJECT),
    TIMEZONE_LIST(0xB1, "timezoneList", ProtocolType.OBJECT),
    CSV_LIST(0xB2, "csvList", ProtocolType.OBJECT),
    LEAF_LIST(0xB3, "leafList", ProtocolType.OBJECT),
    BUNCH_LIST(0xB4, "bunchList", ProtocolType.OBJECT),
    CASKET_LIST(0xB5, "casketList", ProtocolType.OBJECT),
    PINCH_LIST(0xB6, "pinchList", ProtocolType.OBJECT),
    STALK_LIST(0xB7, "stalkList", ProtocolType.OBJECT),
    STICK_LIST(0xB8, "stickList", ProtocolType.OBJECT),
    BRANCH_LIST(0xB9, "branchList", ProtocolType.OBJECT),
    TRAY_LIST(0xBA, "trayList", ProtocolType.OBJECT),
    PORTION_LIST(0xBB, "portionList", ProtocolType.OBJECT),
    UTC_DATE_TIME_LIST(0xBD, "utcDateTimeList", ProtocolType.OBJECT),
    PROGRAM_RUN_SUMMARY_LIST(0xBE, "programRunSummaryList", ProtocolType.OBJECT),
    PROGRAM_SESSION_SUMMARY_LIST(0xBF, "programSessionSummaryList", ProtocolType.OBJECT),
    LIQUID_VOLUME_THROUGHPUT_LIST(0xC0, "liquidVolumeThroughputList", ProtocolType.OBJECT),
    WEIGHT_OUNCES_LIST(0xC1, "weightOuncesList", ProtocolType.OBJECT),
    RECTANGLE_2D_LIST(0xC2, "rectangle2DList", ProtocolType.OBJECT);

    public final int id;
    public final String type;
    public final ProtocolType protocolType;

    ContentType(int id, String type, ProtocolType protocolType) {
        this.id = id;
        this.type = type;
        this.protocolType = protocolType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ContentType.class.getSimpleName() + "[", "]").add("id=" + id)
                .add("type='" + type + "'").add("protocolType=" + protocolType).toString();
    }
}
