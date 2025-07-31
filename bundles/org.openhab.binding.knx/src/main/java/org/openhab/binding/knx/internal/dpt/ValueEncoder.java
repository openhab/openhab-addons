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

import static org.openhab.binding.knx.internal.dpt.DPTUtil.NORMALIZED_DPT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Matcher;

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
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Type;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.dptxlator.DPT;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlator1BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.dptxlator.DPTXlatorDate;
import tuwien.auto.calimero.dptxlator.DPTXlatorDateTime;
import tuwien.auto.calimero.dptxlator.DPTXlatorTime;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;

/**
 * This class encodes openHAB data types to strings for sending via Calimero
 *
 * Parts of this code are based on the openHAB KNXCoreTypeMapper by Kai Kreuzer et al.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ValueEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueEncoder.class);

    private ValueEncoder() {
        // prevent instantiation
    }

    /**
     * Formats the given value as String for outputting via Calimero.
     *
     * @param value the value
     * @param dptId the DPT id to use for formatting the string (e.g. 9.001)
     * @return the value formatted as String
     */
    public static @Nullable String encode(Type value, String dptId) {
        Matcher m = DPTUtil.DPT_PATTERN.matcher(dptId);
        if (!m.matches() || m.groupCount() != 2) {
            LOGGER.warn("Couldn't identify main/sub number in dptId '{}'", dptId);
            return null;
        }

        String mainNumber = m.group("main");

        try {
            DPTXlator translator = TranslatorTypes.createTranslator(Integer.parseInt(mainNumber),
                    NORMALIZED_DPT.getOrDefault(dptId, dptId));
            DPT dpt = translator.getType();

            // check for HSBType first, because it extends PercentType as well
            if (value instanceof HSBType type) {
                return handleHSBType(dptId, type);
            } else if (value instanceof OnOffType) {
                return OnOffType.OFF == value ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (value instanceof UpDownType) {
                return UpDownType.UP == value ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (value instanceof IncreaseDecreaseType) {
                DPT valueDPT = ((DPTXlator3BitControlled.DPT3BitControlled) dpt).getControlDPT();
                return IncreaseDecreaseType.DECREASE == value ? valueDPT.getLowerValue() + " 5"
                        : valueDPT.getUpperValue() + " 5";
            } else if (value instanceof OpenClosedType) {
                return OpenClosedType.CLOSED == value ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (value instanceof StopMoveType) {
                return StopMoveType.STOP == value ? dpt.getLowerValue() : dpt.getUpperValue();
            } else if (value instanceof PercentType type) {
                int intValue = type.intValue();
                return "251.600".equals(dptId) ? String.format("- - - %d %%", intValue) : String.valueOf(intValue);
            } else if (value instanceof DecimalType || value instanceof QuantityType<?>) {
                return handleNumericTypes(dptId, mainNumber, dpt, value);
            } else if (value instanceof StringType) {
                if ("243.600".equals(dptId) || "249.600".equals(dptId)) {
                    return value.toString().replace('.', ((DecimalFormat) DecimalFormat.getInstance())
                            .getDecimalFormatSymbols().getDecimalSeparator());
                }
                return value.toString();
            } else if (value instanceof DateTimeType type) {
                return handleDateTimeType(dptId, type);
            }
        } catch (KNXException e) {
            return null;
        } catch (Exception e) {
            LOGGER.warn("An exception occurred converting value {} to dpt id {}: error message={}", value, dptId,
                    e.getMessage());
            return null;
        }

        LOGGER.debug("formatAsDPTString: Couldn't convert value {} to dpt id {} (no mapping).", value, dptId);
        return null;
    }

    /**
     * Formats the given internal <code>dateType</code> to a knx readable String
     * according to the target datapoint type <code>dpt</code>.
     *
     * @param value the input value
     * @param dptId the target datapoint type
     *
     * @return a String which contains either an ISO8601 formatted date (yyyy-mm-dd),
     *         a formatted 24-hour clock with the day of week prepended (Mon, 12:00:00) or
     *         a formatted 24-hour clock (12:00:00)
     */
    private static @Nullable String handleDateTimeType(String dptId, DateTimeType value) {
        if (DPTXlatorDate.DPT_DATE.getID().equals(dptId)) {
            return value.format("%tF");
        } else if (DPTXlatorTime.DPT_TIMEOFDAY.getID().equals(dptId)) {
            return value.format(Locale.US, "%1$ta, %1$tT");
        } else if (DPTXlatorDateTime.DPT_DATE_TIME.getID().equals(dptId)) {
            return value.format(Locale.US, "%tF %1$tT");
        }
        LOGGER.warn("Could not format DateTimeType for datapoint type '{}'", dptId);
        return null;
    }

    private static String handleHSBType(String dptId, HSBType hsb) {
        switch (dptId) {
            case "232.600":
                int[] rgb = ColorUtil.hsbToRgb(hsb);
                return String.format("r:%d g:%d b:%d", rgb[0], rgb[1], rgb[2]);
            case "232.60000":
                // MDT specific: mis-use 232.600 for hsv instead of rgb
                int hue = hsb.getHue().toBigDecimal().multiply(BigDecimal.valueOf(255))
                        .divide(BigDecimal.valueOf(360), 0, RoundingMode.HALF_UP).intValue();
                return "r:" + hue + " g:" + convertPercentToByte(hsb.getSaturation()) + " b:"
                        + convertPercentToByte(hsb.getBrightness());
            case "242.600":
                double[] xyY = ColorUtil.hsbToXY(hsb);
                return String.format("(%,.4f %,.4f) %,.1f %%", xyY[0], xyY[1], xyY[2] * 100.0);
            case "251.600":
                PercentType[] rgbw = ColorUtil.hsbToRgbPercent(hsb);
                return String.format("%,.1f %,.1f %,.1f - %%", rgbw[0].doubleValue(), rgbw[1].doubleValue(),
                        rgbw[2].doubleValue());
            case "251.60600":
                PercentType[] rgbw2 = ColorUtil.hsbToRgbwPercent(hsb);
                return String.format("%,.1f %,.1f %,.1f %,.1f %%", rgbw2[0].doubleValue(), rgbw2[1].doubleValue(),
                        rgbw2[2].doubleValue(), rgbw2[3].doubleValue());
            case "5.003":
                return hsb.getHue().toString();
            default:
                return hsb.getBrightness().toString();
        }
    }

    private static String handleNumericTypes(String dptId, String mainNumber, DPT dpt, Type value) {
        BigDecimal bigDecimal;
        if (value instanceof DecimalType decimalType) {
            bigDecimal = decimalType.toBigDecimal();
        } else {
            String unit = DPTUnits.getUnitForDpt(dptId);

            // exception for DPT using temperature differences
            // - conversion °C or °F to K is wrong for differences,
            // - stick to the unit given, fix the scaling for °F
            // 9.002 DPT_Value_Tempd
            // 9.003 DPT_Value_Tempa
            // 9.023 DPT_KelvinPerPercent
            if (DPTXlator2ByteFloat.DPT_TEMPERATURE_DIFFERENCE.getID().equals(dptId)
                    || DPTXlator2ByteFloat.DPT_TEMPERATURE_GRADIENT.getID().equals(dptId)
                    || DPTXlator2ByteFloat.DPT_KELVIN_PER_PERCENT.getID().equals(dptId)) {
                // match Unicode character or °C
                if (value.toString().contains(SIUnits.CELSIUS.getSymbol()) || value.toString().contains("°C")) {
                    if (unit != null) {
                        unit = unit.replace("K", "°C");
                    }
                } else if (value.toString().contains("°F")) {
                    // A new approach to handle temperature differences was introduced to core
                    // after 4.0, stripping the unit and creating a new QuantityType works
                    // both with core release 4.0 and current snapshot
                    boolean perPercent = value.toString().contains("/%");
                    value = new QuantityType<>(((QuantityType<?>) value).doubleValue() * 5.0 / 9.0, Units.KELVIN);
                    // PercentType needs to be adapted
                    if (perPercent) {
                        value = ((QuantityType<?>) value).multiply(BigDecimal.valueOf(100));
                    }
                }
            } else if (DPTXlator4ByteFloat.DPT_LIGHT_QUANTITY.getID().equals(dptId)) {
                if (!value.toString().contains("J")) {
                    if (unit != null) {
                        unit = unit.replace("J", "lm*s");
                    }
                }
            } else if (DPTXlator4ByteFloat.DPT_ELECTRIC_FLUX.getID().equals(dptId)) {
                // use alternate definition of flux
                if (value.toString().contains("C")) {
                    unit = "C";
                }
            }

            if (unit != null) {
                QuantityType<?> converted = ((QuantityType<?>) value).toUnit(unit);
                if (converted == null) {
                    LOGGER.warn("Could not convert {} to unit {}, stripping unit only. Check your configuration.",
                            value, unit);
                    bigDecimal = ((QuantityType<?>) value).toBigDecimal();
                } else {
                    bigDecimal = converted.toBigDecimal();
                }
            } else {
                bigDecimal = ((QuantityType<?>) value).toBigDecimal();
            }
        }
        switch (mainNumber) {
            case "1":
                if (DPTXlatorBoolean.DPT_SCENE_AB.getID().equals(dptId)) {
                    return (bigDecimal.intValue() == 0) ? dpt.getLowerValue() : dpt.getUpperValue();
                }
                return bigDecimal.stripTrailingZeros().toPlainString();
            case "2":
                DPT valueDPT = ((DPTXlator1BitControlled.DPT1BitControlled) dpt).getValueDPT();
                return switch (bigDecimal.intValue()) {
                    case 0 -> "0 " + valueDPT.getLowerValue();
                    case 1 -> "0 " + valueDPT.getUpperValue();
                    case 2 -> "1 " + valueDPT.getLowerValue();
                    default -> "1 " + valueDPT.getUpperValue();
                };
            case "18":
                int intVal = bigDecimal.intValue();
                if (intVal > 63) {
                    return "learn " + (intVal - 0x80);
                } else {
                    return "activate " + intVal;
                }
            case "8":
                if ("8.010".equals(dptId)) {
                    // 8.010 has a resolution of 0.01 and will be scaled. Calimero expects locale-specific separator.
                    return bigDecimal.stripTrailingZeros().toPlainString().replace('.',
                            ((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols()
                                    .getDecimalSeparator());
                }
                // fallthrough
            default:
                return bigDecimal.stripTrailingZeros().toPlainString();
        }
    }

    /**
     * convert 0...100% to 1 byte 0..255
     *
     * @param percent percentage 0..1
     * @return int 0..255
     */
    private static int convertPercentToByte(PercentType percent) {
        return percent.toBigDecimal().multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).intValue();
    }
}
