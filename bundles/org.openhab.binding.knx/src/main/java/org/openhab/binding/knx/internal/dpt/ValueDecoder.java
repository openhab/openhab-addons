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
package org.openhab.binding.knx.internal.dpt;

import static org.openhab.binding.knx.internal.KNXBindingConstants.disableUoM;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXFormatException;
import tuwien.auto.calimero.KNXIllegalArgumentException;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlator1BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator64BitSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.dptxlator.DPTXlatorDateTime;
import tuwien.auto.calimero.dptxlator.DPTXlatorSceneControl;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;

/**
 * This class decodes raw data received from the KNX bus to an openHAB datatype
 *
 * Parts of this code are based on the openHAB KNXCoreTypeMapper by Kai Kreuzer et al.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ValueDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueDecoder.class);

    private static final String TIME_DAY_FORMAT = "EEE, HH:mm:ss";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    // RGB: "r:123 g:123 b:123" value-range: 0-255
    private static final Pattern RGB_PATTERN = Pattern.compile("r:(?<r>\\d+) g:(?<g>\\d+) b:(?<b>\\d+)");
    // RGBW: "100 27 25 12 %", value range: 0-100, invalid values: "-"
    private static final Pattern RGBW_PATTERN = Pattern
            .compile("(?:(?<r>[\\d,.]+)|-)\\s(?:(?<g>[\\d,.]+)|-)\\s(?:(?<b>[\\d,.]+)|-)\\s(?:(?<w>[\\d,.]+)|-)\\s%");
    // xyY: "(0,123 0,123) 56 %", value range 0-1 for xy (comma or point as decimal point), 0-100 for Y, invalid values
    // omitted
    public static final Pattern XYY_PATTERN = Pattern
            .compile("(?:\\((?<x>\\d+(?:[,.]\\d+)?) (?<y>\\d+(?:[,.]\\d+)?)\\))?\\s*(?:(?<Y>\\d+(?:[,.]\\d+)?)\\s%)?");
    public static final Pattern TSD_SEPARATOR = Pattern.compile("^[0-9]+(?<sep>[,\\.])[0-9][0-9][0-9].*");

    private static boolean check235001(byte[] data) throws KNXException {
        if (data.length != 6) {
            throw new KNXFormatException("DPT235 broken frame");
        }
        if ((data[5] & 2) == 0) {
            LOGGER.trace("DPT235.001 w/o ActiveEnergy ignored");
            return false;
        }
        return true;
    }

    private static boolean check23561001(byte[] data) throws KNXException {
        if (data.length != 6) {
            throw new KNXFormatException("DPT235 broken frame");
        }
        if ((data[5] & 1) == 0) {
            LOGGER.trace("DPT235.61001 w/o Tariff ignored");
            return false;
        }
        return true;
    }

    /**
     * convert the raw value received to the corresponding openHAB value
     *
     * @param dptId the DPT of the given data
     * @param data a byte array containing the value
     * @param preferredType the preferred datatype for this conversion
     * @return the data converted to an openHAB Type (or null if conversion failed)
     */
    public static @Nullable Type decode(String dptId, byte[] data, Class<? extends Type> preferredType) {
        try {
            String value = "";
            String translatorDptId = dptId;
            DPTXlator translator;
            try {
                translator = TranslatorTypes.createTranslator(0, DPTUtil.NORMALIZED_DPT.getOrDefault(dptId, dptId));
                translator.setData(data);
                value = translator.getValue();
                translatorDptId = translator.getType().getID();
            } catch (KNXException e) {
                // special handling for decoding DPTs not yet supported by Calimero
                if ("235.001".equals(dptId)) {
                    if (!check235001(data)) {
                        return null;
                    }
                    translator = TranslatorTypes.createTranslator(0, "13.010");
                    translator.setData(data);
                    value = translator.getValue();
                    dptId = "13.010";
                    translatorDptId = dptId;
                } else if ("235.61001".equals(dptId)) {
                    if (!check23561001(data)) {
                        return null;
                    }
                    translator = TranslatorTypes.createTranslator(0, "5.006");
                    translator.setData(new byte[] { data[4] });
                    value = translator.getValue();
                    dptId = "5.006";
                    translatorDptId = dptId;
                } else {
                    // no known special case, handle unknown translator outer try block
                    throw e;
                }
            }
            String id = dptId; // prefer using the user-supplied DPT

            Matcher m = DPTUtil.DPT_PATTERN.matcher(id);
            if (!m.matches() || m.groupCount() != 2) {
                LOGGER.trace("User-Supplied DPT '{}' did not match for sub-type, using DPT returned from Translator",
                        id);
                id = translatorDptId;
                m = DPTUtil.DPT_PATTERN.matcher(id);
                if (!m.matches() || m.groupCount() != 2) {
                    LOGGER.warn("Couldn't identify main/sub number in dptID '{}'", id);
                    return null;
                }
            }
            LOGGER.trace("Finally using datapoint DPT = {}", id);

            String mainType = m.group("main");
            String subType = m.group("sub");

            switch (mainType) {
                case "1":
                    return handleDpt1(subType, translator, preferredType);
                case "2":
                    DPTXlator1BitControlled translator1BitControlled = (DPTXlator1BitControlled) translator;
                    int decValue = (translator1BitControlled.getControlBit() ? 2 : 0)
                            + (translator1BitControlled.getValueBit() ? 1 : 0);
                    return new DecimalType(decValue);
                case "3":
                    return handleDpt3(subType, translator);
                case "6":
                    if ("020".equals(subType)) {
                        return handleStringOrDecimal(data, value, preferredType, 8);
                    } else {
                        return handleNumericDpt(id, translator, preferredType);
                    }
                case "10":
                    return handleDpt10(value);
                case "11":
                    return DateTimeType.valueOf(new SimpleDateFormat(DateTimeType.DATE_PATTERN)
                            .format(new SimpleDateFormat(DATE_FORMAT).parse(value)));
                case "18":
                    DPTXlatorSceneControl translatorSceneControl = (DPTXlatorSceneControl) translator;
                    int decimalValue = translatorSceneControl.getSceneNumber();
                    if (value.startsWith("learn")) {
                        decimalValue += 0x80;
                    }
                    return new DecimalType(decimalValue);
                case "19":
                    return handleDpt19(translator, data);
                case "20":
                case "21":
                    return handleStringOrDecimal(data, value, preferredType, 8);
                case "22":
                    return handleStringOrDecimal(data, value, preferredType, 16);
                case "16":
                case "28":
                case "250": // Map all combined color transitions to String,
                case "252": // as no native support is planned.
                case "253": // Currently only one subtype 2xx.600
                case "254": // is defined for those DPTs.
                    return StringType.valueOf(value);
                case "243": // color translation, fix regional
                case "249": // settings
                    // workaround for different number formats, this is to fix time>=1000s:
                    // time is last block and may contain . and ,
                    int sep = java.lang.Math.max(value.indexOf(" % "), value.indexOf(" K "));
                    String time = value.substring(sep + 3);
                    Matcher mt = TSD_SEPARATOR.matcher(time);
                    for (; mt.matches(); mt = TSD_SEPARATOR.matcher(time)) {
                        int dp = time.indexOf(mt.group("sep"));
                        time = time.substring(0, dp) + time.substring(dp + 1);
                    }
                    value = value.substring(0, sep + 3) + time;
                    return StringType.valueOf(value.replace(',', '.').replace(". ", ", "));
                case "232":
                    return handleDpt232(value, subType);
                case "242":
                    return handleDpt242(value);
                case "251":
                    return handleDpt251(value, subType, preferredType);
                default:
                    return handleNumericDpt(id, translator, preferredType);
            }
        } catch (NumberFormatException | KNXFormatException | KNXIllegalArgumentException | ParseException e) {
            LOGGER.info("Translator couldn't parse data '{}' for datapoint type '{}' ({}).", data, dptId, e.getClass());
        } catch (KNXException e) {
            // should never happen unless Calimero changes
            LOGGER.warn("Failed creating a translator for datapoint type '{}'. Please open an issue.", dptId, e);
        }

        return null;
    }

    private static Type handleDpt1(String subType, DPTXlator translator, Class<? extends Type> preferredType) {
        DPTXlatorBoolean translatorBoolean = (DPTXlatorBoolean) translator;
        return switch (subType) {
            case "008" -> translatorBoolean.getValueBoolean() ? UpDownType.DOWN : UpDownType.UP;
            case "009", "019" -> {
                // default is OpenClosedType (Contact), but it may be mapped to OnOffType as well
                if (OnOffType.class.equals(preferredType)) {
                    yield OnOffType.from(translatorBoolean.getValueBoolean());
                }

                // This is wrong for DPT 1.009. It should be true -> CLOSE, false -> OPEN, but unfortunately
                // can't be fixed without breaking a lot of working installations.
                // The documentation has been updated to reflect that. / @J-N-K
                yield translatorBoolean.getValueBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;

                // This is wrong for DPT 1.009. It should be true -> CLOSE, false -> OPEN, but unfortunately
                // can't be fixed without breaking a lot of working installations.
                // The documentation has been updated to reflect that. / @J-N-K
            }
            case "010" -> translatorBoolean.getValueBoolean() ? StopMoveType.MOVE : StopMoveType.STOP;
            case "022" -> DecimalType.valueOf(translatorBoolean.getValueBoolean() ? "1" : "0");
            default -> {
                // default is OnOffType (Switch), but it may be mapped to OpenClosedType as well
                if (OpenClosedType.class.equals(preferredType)) {
                    yield translatorBoolean.getValueBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                }

                yield OnOffType.from(translatorBoolean.getValueBoolean());
            }
        };
    }

    private static @Nullable Type handleDpt3(String subType, DPTXlator translator) {
        DPTXlator3BitControlled translator3BitControlled = (DPTXlator3BitControlled) translator;
        if (translator3BitControlled.getStepCode() == 0) {
            LOGGER.debug("convertRawDataToType: KNX DPT_Control_Dimming: break received.");
            return UnDefType.NULL;
        }
        return switch (subType) {
            case "007" -> translator3BitControlled.getControlBit() ? IncreaseDecreaseType.INCREASE
                    : IncreaseDecreaseType.DECREASE;
            case "008" -> translator3BitControlled.getControlBit() ? UpDownType.DOWN : UpDownType.UP;
            default -> {
                // should never happen unless Calimero introduces new subtypes
                LOGGER.warn("DPT3, subtype '{}' is unknown. Please open an issue.", subType);
                yield null;
            }
        };
    }

    private static Type handleDpt10(String value) throws ParseException {
        // Calimero will provide either TIME_DAY_FORMAT or TIME_FORMAT, no-day is not printed
        Date date = null;
        try {
            date = new SimpleDateFormat(TIME_DAY_FORMAT, Locale.US).parse(value);
        } catch (ParseException pe) {
            date = new SimpleDateFormat(TIME_FORMAT, Locale.US).parse(value);
        }
        return DateTimeType.valueOf(new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(date));
    }

    private static @Nullable Type handleDpt19(DPTXlator translator, byte[] data) throws KNXFormatException {
        DPTXlatorDateTime translatorDateTime = (DPTXlatorDateTime) translator;
        if (translatorDateTime.isFaultyClock()) {
            // Not supported: faulty clock
            LOGGER.debug("KNX clock msg ignored: clock faulty bit set, which is not supported");
            return null;
        } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                && translatorDateTime.isValidField(DPTXlatorDateTime.DATE)) {
            // Not supported: "/1/1" (month and day without year)
            LOGGER.debug("KNX clock msg ignored: no year, but day and month, which is not supported");
            return null;
        } else if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                && !translatorDateTime.isValidField(DPTXlatorDateTime.DATE)) {
            // Not supported: "1900" (year without month and day)
            LOGGER.debug("KNX clock msg ignored: no day and month, but year, which is not supported");
            return null;
        } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                && !translatorDateTime.isValidField(DPTXlatorDateTime.DATE)
                && !translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
            // Not supported: No year, no date and no time
            LOGGER.debug("KNX clock msg ignored: no day and month or year, which is not supported");
            return null;
        }

        Calendar cal = Calendar.getInstance();
        if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                && !translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
            // Pure date format, no time information
            try {
                cal.setTimeInMillis(translatorDateTime.getValueMilliseconds());
            } catch (KNXFormatException e) {
                LOGGER.debug("KNX clock msg ignored: {}", e.getMessage());
                throw e;
            }
            String value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
            return DateTimeType.valueOf(value);
        } else if (!translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                && translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
            // Pure time format, no date information
            cal.clear();
            cal.set(Calendar.HOUR_OF_DAY, translatorDateTime.getHour());
            cal.set(Calendar.MINUTE, translatorDateTime.getMinute());
            cal.set(Calendar.SECOND, translatorDateTime.getSecond());
            String value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
            return DateTimeType.valueOf(value);
        } else if (translatorDateTime.isValidField(DPTXlatorDateTime.YEAR)
                && translatorDateTime.isValidField(DPTXlatorDateTime.TIME)) {
            // Date format and time information
            try {
                cal.setTimeInMillis(translatorDateTime.getValueMilliseconds());
            } catch (KNXFormatException ignore) {
                // throws KNXFormatException in case DST (SUTI) flag does not match calendar
                // As the spec regards the SUTI flag as purely informative, flip it and try again.
                if (data.length < 8) {
                    return null;
                }
                data[6] = (byte) (data[6] ^ 0x01);
                translator.setData(data, 0);
                cal.setTimeInMillis(translatorDateTime.getValueMilliseconds());
            }
            String value = new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(cal.getTime());
            return DateTimeType.valueOf(value);
        } else {
            LOGGER.warn("Failed to convert '{}'", translator.getValue());
            return null;
        }
    }

    private static @Nullable Type handleStringOrDecimal(byte[] data, String value, Class<? extends Type> preferredType,
            int bits) {
        if (DecimalType.class.equals(preferredType)) {
            try {
                // need a new translator for 8 bit unsigned, as Calimero handles only the string type
                if (bits == 8) {
                    DPTXlator8BitUnsigned translator = new DPTXlator8BitUnsigned("5.010");
                    translator.setData(data);
                    return new DecimalType(translator.getValueUnsigned());
                } else if (bits == 16) {
                    DPTXlator2ByteUnsigned translator = new DPTXlator2ByteUnsigned("7.001");
                    translator.setData(data);
                    return new DecimalType(translator.getValueUnsigned());
                } else {
                    return null;
                }
            } catch (KNXFormatException e) {
                return null;
            }
        } else {
            return StringType.valueOf(value);
        }
    }

    private static @Nullable Type handleDpt232(String value, String subType) {
        Matcher rgb = RGB_PATTERN.matcher(value);
        if (rgb.matches()) {
            int r = Integer.parseInt(rgb.group("r"));
            int g = Integer.parseInt(rgb.group("g"));
            int b = Integer.parseInt(rgb.group("b"));

            switch (subType) {
                case "600":
                    return HSBType.fromRGB(r, g, b);
                case "60000":
                    // MDT specific: mis-use 232.600 for hsv instead of rgb
                    DecimalType hue = new DecimalType(coerceToRange(r * 360.0 / 255.0, 0.0, 359.9999));
                    PercentType sat = new PercentType(BigDecimal.valueOf(coerceToRange(g / 2.55, 0.0, 100.0)));
                    PercentType bright = new PercentType(BigDecimal.valueOf(coerceToRange(b / 2.55, 0.0, 100.0)));
                    return new HSBType(hue, sat, bright);
                default:
                    LOGGER.warn("Unknown subtype '232.{}', no conversion possible.", subType);
                    return null;
            }
        }
        LOGGER.warn("Failed to convert '{}' (DPT 232): Pattern does not match", value);
        return null;
    }

    private static @Nullable Type handleDpt242(String value) {
        Matcher xyY = XYY_PATTERN.matcher(value);
        if (xyY.matches()) {
            String stringx = xyY.group("x");
            String stringy = xyY.group("y");
            String stringY = xyY.group("Y");

            if (stringx != null && stringy != null) {
                double x = Double.parseDouble(stringx.replace(",", "."));
                double y = Double.parseDouble(stringy.replace(",", "."));
                if (stringY == null) {
                    return ColorUtil.xyToHsb(new double[] { x, y });
                } else {
                    double pY = Double.parseDouble(stringY.replace(",", "."));
                    return ColorUtil.xyToHsb(new double[] { x, y, pY / 100.0 });
                }
            }
        }
        LOGGER.warn("Failed to convert '{}' (DPT 242): Pattern does not match", value);
        return null;
    }

    private static @Nullable Type handleDpt251(String value, String subType, Class<? extends Type> preferredType) {
        Matcher rgbw = RGBW_PATTERN.matcher(value);
        if (rgbw.matches()) {
            String rString = rgbw.group("r");
            String gString = rgbw.group("g");
            String bString = rgbw.group("b");
            String wString = rgbw.group("w");

            switch (subType) {
                case "600":
                    if (rString != null && gString != null && bString != null && HSBType.class.equals(preferredType)) {
                        // does not support PercentType and r,g,b valid -> HSBType
                        int r = coerceToRange((int) (Double.parseDouble(rString.replace(",", ".")) * 2.55), 0, 255);
                        int g = coerceToRange((int) (Double.parseDouble(gString.replace(",", ".")) * 2.55), 0, 255);
                        int b = coerceToRange((int) (Double.parseDouble(bString.replace(",", ".")) * 2.55), 0, 255);

                        return HSBType.fromRGB(r, g, b);
                    } else if (wString != null && PercentType.class.equals(preferredType)) {
                        // does support PercentType and w valid -> PercentType
                        BigDecimal w = new BigDecimal(wString.replace(",", "."));

                        return new PercentType(w);
                    }
                case "60600":
                    // special type used by OH for .600 indicating that RGBW should be handled with a single HSBType,
                    // typically we use HSBType for RGB and PercentType for W.
                    if (rString != null && gString != null && bString != null && wString != null
                            && HSBType.class.equals(preferredType)) {
                        // does support PercentType and w valid -> PercentType
                        int r = coerceToRange((int) (Double.parseDouble(rString.replace(",", ".")) * 2.55), 0, 255);
                        int g = coerceToRange((int) (Double.parseDouble(gString.replace(",", ".")) * 2.55), 0, 255);
                        int b = coerceToRange((int) (Double.parseDouble(bString.replace(",", ".")) * 2.55), 0, 255);
                        int w = coerceToRange((int) (Double.parseDouble(wString.replace(",", ".")) * 2.55), 0, 255);

                        return ColorUtil.rgbToHsb(new int[] { r, g, b, w });
                    }
                default:
                    LOGGER.warn("Unknown subtype '251.{}', no conversion possible.", subType);
                    return null;
            }

        }
        LOGGER.warn("Failed to convert '{}' (DPT 251): Pattern does not match or invalid content", value);
        return null;
    }

    private static @Nullable Type handleNumericDpt(String id, DPTXlator translator, Class<? extends Type> preferredType)
            throws KNXFormatException {
        Set<Class<? extends Type>> allowedTypes = DPTUtil.getAllowedTypes(id);

        double value = translator.getNumericValue();
        if (allowedTypes.contains(PercentType.class)
                && (HSBType.class.equals(preferredType) || PercentType.class.equals(preferredType))) {
            return new PercentType(BigDecimal.valueOf(Math.round(value)));
        }

        if (allowedTypes.contains(QuantityType.class) && !disableUoM) {
            String unit = DPTUnits.getUnitForDpt(id);
            if (unit != null) {
                if (translator instanceof DPTXlator64BitSigned translatorSigned) {
                    // prevent loss of precision, do not represent 64bit decimal using double
                    return new QuantityType<>(translatorSigned.getValueSigned() + " " + unit);
                }
                return new QuantityType<>(value + " " + unit);
            } else {
                LOGGER.trace("Could not determine unit for DPT '{}', fallback to plain decimal", id);
            }
        }

        if (allowedTypes.contains(DecimalType.class)) {
            if (translator instanceof DPTXlator64BitSigned translatorSigned) {
                // prevent loss of precision, do not represent 64bit decimal using double
                return new DecimalType(translatorSigned.getValueSigned());
            }
            return new DecimalType(value);
        }

        LOGGER.warn("Failed to convert '{}' (DPT '{}'): no matching type found", value, id);
        return null;
    }

    private static double coerceToRange(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    private static int coerceToRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
