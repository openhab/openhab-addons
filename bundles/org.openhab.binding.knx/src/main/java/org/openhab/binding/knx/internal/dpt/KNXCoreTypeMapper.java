/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.dpt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;
import tuwien.auto.calimero.KNXIllegalArgumentException;
import tuwien.auto.calimero.dptxlator.DPT;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlator1BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlator64BitSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.dptxlator.DPTXlatorDate;
import tuwien.auto.calimero.dptxlator.DPTXlatorDateTime;
import tuwien.auto.calimero.dptxlator.DPTXlatorRGB;
import tuwien.auto.calimero.dptxlator.DPTXlatorSceneControl;
import tuwien.auto.calimero.dptxlator.DPTXlatorSceneNumber;
import tuwien.auto.calimero.dptxlator.DPTXlatorString;
import tuwien.auto.calimero.dptxlator.DPTXlatorTime;
import tuwien.auto.calimero.dptxlator.DPTXlatorUtf8;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;

/**
 * This class provides type mapping between all openHAB core types and KNX data point types.
 *
 * Each 'MainType' delivered from calimero, has a default mapping
 * for all it's children to an openHAB Typeclass.
 * All these 'MainType' mapping's are put into 'dptMainTypeMap'.
 *
 * Default 'MainType' mapping's we can override by a specific mapping.
 * All specific mapping's are put into 'dptTypeMap'.
 *
 * If for a 'MainType' there is currently no specific mapping registered,
 * you can find a commented example line, with it's correct 'DPTXlator' class.
 *
 * @author Kai Kreuzer - initial contribution
 * @author Volker Daube - improvements
 * @author Jan N. Klug - improvements
 * @author Helmut Lehmeyer - Java8, generic DPT Mapper
 * @author Jan N. Klug - refactor to static class
 */
@NonNullByDefault
public class KNXCoreTypeMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KNXCoreTypeMapper.class);

    private static final String TIME_DAY_FORMAT = "EEE, HH:mm:ss";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final Pattern RGB_PATTERN = Pattern.compile("r:(?<r>\\d+) g:(?<g>\\d+) b:(?<b>\\d+)");
    private static final Pattern DPT_PATTERN = Pattern.compile("^(?<main>[1-9][0-9]{0,2})(?:\\.(?<sub>\\d{3,4}))?$");

    /**
     * stores the openHAB type class for (supported) KNX datapoint types in a generic way.
     * dptTypeMap stores more specific type class and exceptions.
     */
    private static final Map<String, Set<Class<? extends Type>>> DPT_MAIN_TYPE_MAP = Map.ofEntries( //
            Map.entry("1", Set.of(OnOffType.class)), //
            Map.entry("2", Set.of(DecimalType.class)), //
            Map.entry("3", Set.of(IncreaseDecreaseType.class)), //
            Map.entry("4", Set.of(StringType.class)), //
            Map.entry("5", Set.of(DecimalType.class)), //
            Map.entry("6", Set.of(DecimalType.class)), //
            Map.entry("7", Set.of(DecimalType.class, QuantityType.class)), //
            Map.entry("8", Set.of(DecimalType.class, QuantityType.class)), //
            Map.entry("9", Set.of(DecimalType.class, QuantityType.class)), //
            Map.entry("10", Set.of(DateTimeType.class)), //
            Map.entry("11", Set.of(DateTimeType.class)), //
            Map.entry("12", Set.of(DecimalType.class)), //
            Map.entry("13", Set.of(DecimalType.class, QuantityType.class)), //
            Map.entry("14", Set.of(DecimalType.class, QuantityType.class)), //
            Map.entry("16", Set.of(StringType.class)), //
            Map.entry("17", Set.of(DecimalType.class)), //
            Map.entry("18", Set.of(DecimalType.class)), //
            Map.entry("19", Set.of(DateTimeType.class)), //
            Map.entry("20", Set.of(StringType.class)), //
            Map.entry("21", Set.of(StringType.class)), //
            Map.entry("22", Set.of(StringType.class)), //
            Map.entry("28", Set.of(StringType.class)), //
            Map.entry("29", Set.of(DecimalType.class, QuantityType.class)), //
            Map.entry("229", Set.of(DecimalType.class)), //
            Map.entry("232", Set.of(HSBType.class)));

    /** stores the openHAB type class for all (supported) KNX datapoint types */
    private static final Map<String, Set<Class<? extends Type>>> DPT_TYPE_MAP = Map.ofEntries(
            Map.entry(DPTXlatorBoolean.DPT_UPDOWN.getID(), Set.of(UpDownType.class)), //
            Map.entry(DPTXlatorBoolean.DPT_OPENCLOSE.getID(), Set.of(OpenClosedType.class)), //
            Map.entry(DPTXlatorBoolean.DPT_START.getID(), Set.of(StopMoveType.class)), //
            Map.entry(DPTXlatorBoolean.DPT_WINDOW_DOOR.getID(), Set.of(OpenClosedType.class)), //
            Map.entry(DPTXlatorBoolean.DPT_SCENE_AB.getID(), Set.of(DecimalType.class)), //
            Map.entry(DPTXlator3BitControlled.DPT_CONTROL_BLINDS.getID(), Set.of(UpDownType.class)), //
            Map.entry(DPTXlator8BitUnsigned.DPT_SCALING.getID(), Set.of(PercentType.class)), //
            Map.entry(DPTXlator8BitUnsigned.DPT_PERCENT_U8.getID(), Set.of(PercentType.class)), //
            Map.entry(DPTXlator8BitSigned.DPT_PERCENT_V8.getID(), Set.of(PercentType.class)), //
            Map.entry(DPTXlator8BitSigned.DPT_STATUS_MODE3.getID(), Set.of(StringType.class)), //
            Map.entry(DPTXlator2ByteFloat.DPT_HUMIDITY.getID(), Set.of(PercentType.class)), //
            Map.entry(DPTXlatorString.DPT_STRING_8859_1.getID(), Set.of(StringType.class)), //
            Map.entry(DPTXlatorString.DPT_STRING_ASCII.getID(), Set.of(StringType.class)));

    /** stores the default KNX DPT to use for each openHAB type */
    @SuppressWarnings("unused")
    private static final Map<Class<? extends Type>, String> DEFAULT_DPT_MAP = Map.ofEntries(
            Map.entry(OnOffType.class, DPTXlatorBoolean.DPT_SWITCH.getID()), //
            Map.entry(UpDownType.class, DPTXlatorBoolean.DPT_UPDOWN.getID()), //
            Map.entry(StopMoveType.class, DPTXlatorBoolean.DPT_START.getID()), //
            Map.entry(OpenClosedType.class, DPTXlatorBoolean.DPT_WINDOW_DOOR.getID()), //
            Map.entry(IncreaseDecreaseType.class, DPTXlator3BitControlled.DPT_CONTROL_DIMMING.getID()), //
            Map.entry(PercentType.class, DPTXlator8BitUnsigned.DPT_SCALING.getID()), //
            Map.entry(DecimalType.class, DPTXlator2ByteFloat.DPT_TEMPERATURE.getID()), //
            Map.entry(QuantityType.class, DPTXlator2ByteFloat.DPT_TEMPERATURE.getID()), //
            Map.entry(DateTimeType.class, DPTXlatorTime.DPT_TIMEOFDAY.getID()), //
            Map.entry(StringType.class, DPTXlatorString.DPT_STRING_8859_1.getID()), //
            Map.entry(HSBType.class, DPTXlatorRGB.DPT_RGB.getID()));

    @SuppressWarnings("unused")
    private static final List<Class<? extends DPTXlator>> XLATORS = List.of(DPTXlator1BitControlled.class,
            DPTXlator2ByteFloat.class, DPTXlator2ByteUnsigned.class, DPTXlator3BitControlled.class,
            DPTXlator4ByteFloat.class, DPTXlator4ByteSigned.class, DPTXlator4ByteUnsigned.class,
            DPTXlator64BitSigned.class, DPTXlator8BitSigned.class, DPTXlator8BitUnsigned.class, DPTXlatorBoolean.class,
            DPTXlatorDate.class, DPTXlatorDateTime.class, DPTXlatorRGB.class, DPTXlatorSceneControl.class,
            DPTXlatorSceneNumber.class, DPTXlatorString.class, DPTXlatorTime.class, DPTXlatorUtf8.class);

    private KNXCoreTypeMapper() {
        // prevent instantiation
    }

    /*
     * This function computes the target unit for type conversion from OH quantity type to DPT types.
     * Calimero library provides units which can be used for most of the DPTs. There are some deviations
     * from the OH unit scheme which are handled.
     */
    private static String quantityTypeToDPTValue(QuantityType<?> qt, int mainNumber, int subNumber, String dpUnit)
            throws KNXException {
        String targetOhUnit = dpUnit;
        double scaleFactor = 1.0;
        switch (mainNumber) {
            case 7:
                switch (subNumber) {
                    case 3:
                    case 4:
                        targetOhUnit = "ms";
                        break;
                }
                break;
            case 9:
                switch (subNumber) {
                    // special case: temperature deltas specified in different units
                    // ignore the offset, but run a conversion to handle prefixes like mK
                    // scaleFactor is needed to properly handle °F
                    case 2: {
                        final String unit = qt.getUnit().toString();
                        // find out if the unit is based on °C or K, getSystemUnit() does not help here as it always
                        // gives "K"
                        if (unit.contains("°C")) {
                            targetOhUnit = "°C";
                        } else if (unit.contains("°F")) {
                            targetOhUnit = "°F";
                            scaleFactor = 5.0 / 9.0;
                        } else if (unit.contains("K")) {
                            targetOhUnit = "K";
                        } else {
                            targetOhUnit = "";
                        }
                        break;
                    }
                    case 3: {
                        final String unit = qt.getUnit().toString();
                        if (unit.contains("°C")) {
                            targetOhUnit = "°C/h";
                        } else if (unit.contains("°F")) {
                            targetOhUnit = "°F/h";
                            scaleFactor = 5.0 / 9.0;
                        } else if (unit.contains("K")) {
                            targetOhUnit = "K/h";
                        } else {
                            targetOhUnit = "";
                        }
                        break;
                    }
                    case 23: {
                        final String unit = qt.getUnit().toString();
                        if (unit.contains("°C")) {
                            targetOhUnit = "°C/%";
                        } else if (unit.contains("°F")) {
                            targetOhUnit = "°F/%";
                            scaleFactor = 5.0 / 9.0;
                        } else if (unit.contains("K")) {
                            targetOhUnit = "K/%";
                        } else {
                            targetOhUnit = "";
                        }
                        break;
                    }
                }
                break;
            case 12:
                switch (subNumber) {
                    case 1200:
                        // Calimero uses "litre"
                        targetOhUnit = "l";
                        break;
                }
                break;
            case 13:
                switch (subNumber) {
                    case 12:
                    case 15:
                        // Calimero uses VARh, OH uses varh
                        targetOhUnit = targetOhUnit.replace("VARh", "varh");
                        break;
                    case 14:
                        // OH does not accept kVAh, only VAh
                        targetOhUnit = targetOhUnit.replace("kVAh", "VAh");
                        scaleFactor = 1.0 / 1000.0;
                        break;
                }
                break;

            case 14:
                targetOhUnit = targetOhUnit.replace("Ω\u207B¹", "S");
                // Calimero uses a special unicode character to specify units like m*s^-2
                // this needs to be rewritten to m/s²
                final int posMinus = targetOhUnit.indexOf("\u207B");
                if (posMinus > 0) {
                    targetOhUnit = targetOhUnit.substring(0, posMinus - 1) + "/" + targetOhUnit.charAt(posMinus - 1)
                            + targetOhUnit.substring(posMinus + 1);
                }
                switch (subNumber) {
                    case 8:
                        // OH does not support unut Js, need to expand
                        targetOhUnit = "J*s";
                        break;
                    case 21:
                        targetOhUnit = "C*m";
                        break;
                    case 24:
                        targetOhUnit = "C";
                        break;
                    case 29:
                    case 47:
                        targetOhUnit = "A*m²";
                        break;
                    case 40:
                        if (qt.getUnit().toString().contains("J")) {
                            targetOhUnit = "J";
                        } else {
                            targetOhUnit = "lm*s";
                        }
                        break;
                    case 61:
                        targetOhUnit = "Ohm*m";
                        break;
                    case 75:
                        targetOhUnit = "N*m";
                        break;
                }
                break;
            case 29:
                switch (subNumber) {
                    case 12:
                        // Calimero uses VARh, OH uses varh
                        targetOhUnit = targetOhUnit.replace("VARh", "varh");
                        break;
                }
                break;
        }
        // replace e.g. m3 by m³
        targetOhUnit = targetOhUnit.replace("3", "³").replace("2", "²");

        final QuantityType<?> result = qt.toUnit(targetOhUnit);
        if (result == null) {
            throw new KNXException("incompatible types: " + qt.getUnit().toString() + ", " + targetOhUnit);
        }
        return BigDecimal.valueOf(result.doubleValue() * scaleFactor).stripTrailingZeros().toPlainString();
    }

    /**
     * formats the given value as String for outputting via Calimero
     *
     * @param value the value
     * @param dptId the DPT id to use for formatting the string (e.g. 9.001)
     * @return the value formatted as String
     */
    public static @Nullable String formatAsDPTString(Type value, String dptId) {
        DPT dpt;

        Matcher m = DPT_PATTERN.matcher(dptId);
        if (!m.matches() || m.groupCount() != 2) {
            LOGGER.warn("formatAsDPTString couldn't identify main/sub number in dptId '{}'", dptId);
            return null;
        }

        String mainNumber = m.group("main");

        try {
            DPTXlator translator = TranslatorTypes.createTranslator(Integer.parseInt(mainNumber), dptId);
            dpt = translator.getType();
        } catch (KNXException e) {
            return null;
        }

        try {
            // check for HSBType first, because it extends PercentType as well
            if (value instanceof HSBType hsb) {
                // also covers 232.600 (RGB)
                if ("5".equals(mainNumber)) {
                    switch (m.group("sub")) {
                        case "003": // * 5.003: Angle, values: 0...360 °
                            return hsb.getHue().toString();
                        case "001": // * 5.001: Scaling, values: 0...100 %
                        default:
                            return hsb.getBrightness().toString();
                    }
                }
                return "r:" + convertPercentToByte(hsb.getRed()) + " g:" + convertPercentToByte(hsb.getGreen()) + " b:"
                        + convertPercentToByte(hsb.getBlue());
            } else if (value instanceof OnOffType) {
                return value.equals(OnOffType.OFF) ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (value instanceof UpDownType) {
                return value.equals(UpDownType.UP) ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (value instanceof IncreaseDecreaseType) {
                DPT valueDPT = ((DPTXlator3BitControlled.DPT3BitControlled) dpt).getControlDPT();
                return value.equals(IncreaseDecreaseType.DECREASE) ? valueDPT.getLowerValue() + " 5"
                        : valueDPT.getUpperValue() + " 5";
            } else if (value instanceof OpenClosedType) {
                return value.equals(OpenClosedType.CLOSED) ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (value instanceof StopMoveType) {
                return value.equals(StopMoveType.STOP) ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (value instanceof PercentType) {
                return String.valueOf(((DecimalType) value).intValue());
            } else if (value instanceof DecimalType) { // TODO not used yet as we had implemented this in the meantime:
                                                       // || value instanceof QuantityType<?>) {
                BigDecimal bigDecimal = value instanceof DecimalType ? ((DecimalType) value).toBigDecimal()
                        : ((QuantityType<?>) value).toBigDecimal();
                switch (mainNumber) {
                    case "2":
                        DPT valueDPT = ((DPTXlator1BitControlled.DPT1BitControlled) dpt).getValueDPT();
                        switch (bigDecimal.intValue()) {
                            case 0:
                                return "0 " + valueDPT.getLowerValue();
                            case 1:
                                return "0 " + valueDPT.getUpperValue();
                            case 2:
                                return "1 " + valueDPT.getLowerValue();
                            default:
                                return "1 " + valueDPT.getUpperValue();
                        }
                    case "18":
                        int intVal = bigDecimal.intValue();
                        if (intVal > 63) {
                            return "learn " + (intVal - 0x80);
                        } else {
                            return "activate " + intVal;
                        }
                    default:
                        return bigDecimal.stripTrailingZeros().toPlainString();
                }
            } else if (value instanceof StringType) {
                return value.toString();
            } else if (value instanceof DateTimeType dtt) {
                return formatDateTime(dtt, dptId);
            } // TODO remove later
            else if (value instanceof QuantityType<?> qt) {
                return quantityTypeToDPTValue(qt, Integer.parseInt(m.group("main")), Integer.parseInt(m.group("sub")),
                        dpt.getUnit());
            }
        } catch (Exception e) {
            LOGGER.warn("An exception occurred converting value {} to dpt id {}: error message={}", value, dptId,
                    e.getMessage());
            return null;
        }

        LOGGER.debug("toDPTValue: Couldn't convert type {} to dpt id {} (no mapping).", value, dptId);

        return null;
    }

    /**
     * convert the raw value received to the corresponding openHAB value
     *
     * @param dptId the DPT of the given data
     * @param data a byte array containing the value
     * @return the data converted to an openHAB Type (or null if conversion failed)
     */
    public static @Nullable Type convertRawDataToType(String dptId, byte[] data) {
        try {
            DPTXlator translator = TranslatorTypes.createTranslator(0, dptId);
            translator.setData(data);
            String value = translator.getValue();

            String id = translator.getType().getID();
            LOGGER.trace("convertRawDataToType datapoint DPT = {}", dptId);

            Matcher m = DPT_PATTERN.matcher(id);
            if (!m.matches() || m.groupCount() != 2) {
                LOGGER.warn("convertRawDataToType couldn't identify main/sub number in dptID '{}'", id);
                return null;
            }
            /*
             * Following code section deals with specific mapping of values from KNX to openHAB types were the String
             * received from the DPTXlator is not sufficient to set the openHAB type or has bugs
             */
            switch (m.group("main")) {
                case "1":
                    DPTXlatorBoolean translatorBoolean = (DPTXlatorBoolean) translator;
                    switch (m.group("sub")) {
                        case "8":
                            return translatorBoolean.getValueBoolean() ? UpDownType.DOWN : UpDownType.UP;
                        case "9":
                            // This is wrong. It should be true -> CLOSE, false -> OPEN, but can't be fixed without
                            // breaking a lot of working installations.
                            // The documentation has been updated to reflect that.
                            return translatorBoolean.getValueBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                        case "10":
                            return translatorBoolean.getValueBoolean() ? StopMoveType.MOVE : StopMoveType.STOP;
                        case "19":
                            return translatorBoolean.getValueBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                        case "22":
                            return DecimalType.valueOf(translatorBoolean.getValueBoolean() ? "1" : "0");
                        default:
                            return OnOffType.from(translatorBoolean.getValueBoolean());
                    }
                case "2":
                    DPTXlator1BitControlled translator1BitControlled = (DPTXlator1BitControlled) translator;
                    int decValue = (translator1BitControlled.getControlBit() ? 2 : 0)
                            + (translator1BitControlled.getValueBit() ? 1 : 0);
                    return new DecimalType(decValue);
                case "3":
                    DPTXlator3BitControlled translator3BitControlled = (DPTXlator3BitControlled) translator;
                    if (translator3BitControlled.getStepCode() == 0) {
                        LOGGER.debug("toType: KNX DPT_Control_Dimming: break received.");
                        return UnDefType.NULL;
                    }
                    switch (m.group("sub")) {
                        case "7":
                            return translator3BitControlled.getControlBit() ? IncreaseDecreaseType.INCREASE
                                    : IncreaseDecreaseType.DECREASE;
                        case "8":
                            return translator3BitControlled.getControlBit() ? UpDownType.DOWN : UpDownType.UP;
                    }
                    break;
                case "18":
                    DPTXlatorSceneControl translatorSceneControl = (DPTXlatorSceneControl) translator;
                    int decimalValue = translatorSceneControl.getSceneNumber();
                    if (value.startsWith("learn")) {
                        decimalValue += 0x80;
                    }
                    value = String.valueOf(decimalValue);

                    break;
                case "19":
                    DPTXlatorDateTime translatorDateTime = (DPTXlatorDateTime) translator;
                    if (translatorDateTime.isFaultyClock()) {
                        // Not supported: faulty clock
                        LOGGER.debug("toType: KNX clock msg ignored: clock faulty bit set, which is not supported");
                        return null;
                    } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && translatorDateTime.isValidField(DPTXlatorDateTime.DATE)) {
                        // Not supported: "/1/1" (month and day without year)
                        LOGGER.debug(
                                "toType: KNX clock msg ignored: no year, but day and month, which is not supported");
                        return null;
                    } else if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && !translatorDateTime.isValidField(DPTXlatorDateTime.DATE)) {
                        // Not supported: "1900" (year without month and day)
                        LOGGER.debug(
                                "toType: KNX clock msg ignored: no day and month, but year, which is not supported");
                        return null;
                    } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && !translatorDateTime.isValidField(DPTXlatorDateTime.DATE)
                            && !translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
                        // Not supported: No year, no date and no time
                        LOGGER.debug("toType: KNX clock msg ignored: no day and month or year, which is not supported");
                        return null;
                    }

                    Calendar cal = Calendar.getInstance();
                    if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && !translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
                        // Pure date format, no time information
                        cal.setTimeInMillis(translatorDateTime.getValueMilliseconds());
                        value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
                        return DateTimeType.valueOf(value);
                    } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
                        // Pure time format, no date information
                        cal.clear();
                        cal.set(Calendar.HOUR_OF_DAY, translatorDateTime.getHour());
                        cal.set(Calendar.MINUTE, translatorDateTime.getMinute());
                        cal.set(Calendar.SECOND, translatorDateTime.getSecond());
                        value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
                        return DateTimeType.valueOf(value);
                    } else if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                            && translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
                        // Date format and time information
                        cal.setTimeInMillis(translatorDateTime.getValueMilliseconds());
                        value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
                        return DateTimeType.valueOf(value);
                    }
                    break;
            }

            Set<Class<? extends Type>> typeClass = getAllowedTypes(id);
            if (typeClass.contains(PercentType.class)) {
                return new PercentType(BigDecimal.valueOf(Math.round(translator.getNumericValue())));
            }
            if (typeClass.contains(DecimalType.class)) {
                return new DecimalType(translator.getNumericValue());
            }
            if (typeClass.contains(StringType.class)) {
                return StringType.valueOf(value);
            }

            if (typeClass.contains(DateTimeType.class)) {
                String date = formatDateTime(value, dptId);
                if (date.isEmpty()) {
                    LOGGER.debug("convertRawDataToType: KNX clock msg ignored: date object empty {}.", date);
                    return null;
                } else {
                    return DateTimeType.valueOf(date);
                }
            }

            if (typeClass.contains(HSBType.class)) {
                // value has format of "r:<red value> g:<green value> b:<blue value>"
                Matcher rgb = RGB_PATTERN.matcher(value);
                if (rgb.matches()) {
                    int r = Integer.parseInt(rgb.group("r"));
                    int g = Integer.parseInt(rgb.group("g"));
                    int b = Integer.parseInt(rgb.group("b"));

                    return HSBType.fromRGB(r, g, b);
                }
            }
        } catch (NumberFormatException | KNXFormatException | KNXIllegalArgumentException e) {
            LOGGER.info("Translator couldn't parse data '{}'for datapoint type '{}' ({}).", data, dptId, e.getClass());
        } catch (KNXException e) {
            LOGGER.warn("Failed creating a translator for datapoint type '{}'.", dptId, e);
        }

        return null;
    }

    /**
     * get allowed openHAB types for given DPT
     *
     * @param dptId the datapoint type id
     * @return Set of supported openHAB types (command or state)
     */
    public static Set<Class<? extends Type>> getAllowedTypes(String dptId) {
        Set<Class<? extends Type>> ohClass = DPT_TYPE_MAP.get(dptId);
        if (ohClass == null) {
            Matcher m = DPT_PATTERN.matcher(dptId);
            if (!m.matches()) {
                LOGGER.warn("getAllowedTypes couldn't identify main number in dptID '{}'", dptId);
                return Set.of();
            }

            ohClass = DPT_MAIN_TYPE_MAP.getOrDefault(m.group("main"), Set.of());
        }
        return ohClass;
    }

    /**
     * Formats the given <code>value</code> according to the datapoint type
     * <code>dpt</code> to a String which can be processed by {@link DateTimeType}.
     *
     * @param value
     * @param dptId
     *
     * @return a formatted String like </code>yyyy-MM-dd'T'HH:mm:ss</code> which
     *         is target format of the {@link DateTimeType}
     */
    private static String formatDateTime(String value, @Nullable String dptId) {
        Date date = null;

        try {
            if (DPTXlatorDate.DPT_DATE.getID().equals(dptId)) {
                date = new SimpleDateFormat(DATE_FORMAT).parse(value);
            } else if (DPTXlatorTime.DPT_TIMEOFDAY.getID().equals(dptId)) {
                if (value.contains("no-day")) {
                    /*
                     * KNX "no-day" needs special treatment since openHAB's DateTimeType doesn't support "no-day".
                     * Workaround: remove the "no-day" String, parse the remaining time string, which will result in a
                     * date of "1970-01-01".
                     * Replace "no-day" with the current day name
                     */
                    StringBuffer stb = new StringBuffer(value);
                    int start = stb.indexOf("no-day");
                    int end = start + "no-day".length();
                    stb.replace(start, end, String.format(Locale.US, "%1$ta", Calendar.getInstance()));
                    value = stb.toString();
                }
                try {
                    date = new SimpleDateFormat(TIME_DAY_FORMAT, Locale.US).parse(value);
                } catch (ParseException pe) {
                    date = new SimpleDateFormat(TIME_FORMAT, Locale.US).parse(value);
                }
            }
        } catch (ParseException pe) {
            // do nothing but logging
            LOGGER.warn("Could not parse '{}' to a valid date", value);
        }

        return date != null ? new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(date) : "";
    }

    /**
     * Formats the given internal <code>dateType</code> to a knx readable String
     * according to the target datapoint type <code>dpt</code>.
     *
     * @param dateType
     * @param dpt the target datapoint type
     *
     * @return a String which contains either an ISO8601 formatted date (yyyy-mm-dd),
     *         a formatted 24-hour clock with the day of week prepended (Mon, 12:00:00) or
     *         a formatted 24-hour clock (12:00:00)
     *
     * @throws IllegalArgumentException if none of the datapoint types DPT_DATE or
     *             DPT_TIMEOFDAY has been used.
     */
    private static String formatDateTime(DateTimeType dateType, @Nullable String dpt) {
        if (DPTXlatorDate.DPT_DATE.getID().equals(dpt)) {
            return dateType.format("%tF");
        } else if (DPTXlatorTime.DPT_TIMEOFDAY.getID().equals(dpt)) {
            return dateType.format(Locale.US, "%1$ta, %1$tT");
        } else if (DPTXlatorDateTime.DPT_DATE_TIME.getID().equals(dpt)) {
            return dateType.format(Locale.US, "%tF %1$tT");
        } else {
            throw new IllegalArgumentException("Could not format date to datapoint type '" + dpt + "'");
        }
    }

    /**
     * convert 0...100% to 1 byte 0..255
     *
     * @param percent
     * @return int 0..255
     */
    private static int convertPercentToByte(PercentType percent) {
        return percent.toBigDecimal().multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).intValue();
    }
}
