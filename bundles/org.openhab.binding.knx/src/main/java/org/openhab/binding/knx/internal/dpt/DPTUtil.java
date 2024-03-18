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

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.dptxlator.DPTXlatorString;

/**
 * This class provides support to determine compatibility between KNX DPTs and openHAB data types
 *
 * Parts of this code are based on the openHAB KNXCoreTypeMapper by Kai Kreuzer et al.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DPTUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DPTUtil.class);

    // DPT: "123.001", 1-3 digits main type (no leading zero), optional sub-type 3-4 digits (leading zeros allowed)
    public static final Pattern DPT_PATTERN = Pattern.compile("^(?<main>[1-9][0-9]{0,2})(?:\\.(?<sub>\\d{3,5}))?$");

    // used to map vendor-specific data to standard DPT
    public static final Map<String, String> NORMALIZED_DPT = Map.of(//
            "232.60000", "232.600", "251.60600", "251.600");

    // fall back if no specific type is defined in DPT_TYPE_MAP
    private static final Map<String, Set<Class<? extends Type>>> DPT_MAIN_TYPE_MAP = Map.ofEntries( //
            Map.entry("1", Set.of(OnOffType.class)), //
            Map.entry("2", Set.of(DecimalType.class)), //
            Map.entry("3", Set.of(IncreaseDecreaseType.class)), //
            Map.entry("4", Set.of(StringType.class)), //
            Map.entry("5", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("6", Set.of(QuantityType.class, DecimalType.class, StringType.class)), //
            Map.entry("7", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("8", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("9", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("10", Set.of(DateTimeType.class)), //
            Map.entry("11", Set.of(DateTimeType.class)), //
            Map.entry("12", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("13", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("14", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("16", Set.of(StringType.class)), //
            Map.entry("17", Set.of(DecimalType.class)), //
            Map.entry("18", Set.of(DecimalType.class)), //
            Map.entry("19", Set.of(DateTimeType.class)), //
            Map.entry("20", Set.of(StringType.class, DecimalType.class)), //
            Map.entry("21", Set.of(StringType.class, DecimalType.class)), //
            Map.entry("22", Set.of(StringType.class, DecimalType.class)), //
            Map.entry("28", Set.of(StringType.class)), //
            Map.entry("29", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("229", Set.of(DecimalType.class)), //
            Map.entry("232", Set.of(HSBType.class)), //
            Map.entry("235", Set.of(QuantityType.class, DecimalType.class)), //
            Map.entry("242", Set.of(HSBType.class)), //
            Map.entry("243", Set.of(StringType.class)), //
            Map.entry("249", Set.of(StringType.class)), //
            Map.entry("250", Set.of(StringType.class)), //
            Map.entry("251", Set.of(HSBType.class, PercentType.class)), //
            Map.entry("252", Set.of(StringType.class)), //
            Map.entry("253", Set.of(StringType.class)), //
            Map.entry("254", Set.of(StringType.class)));

    // compatible types for full DPTs
    private static final Map<String, Set<Class<? extends Type>>> DPT_TYPE_MAP = Map.ofEntries(
            Map.entry(DPTXlatorBoolean.DPT_UPDOWN.getID(), Set.of(UpDownType.class)), //
            Map.entry(DPTXlatorBoolean.DPT_OPENCLOSE.getID(), Set.of(OpenClosedType.class)), //
            Map.entry(DPTXlatorBoolean.DPT_START.getID(), Set.of(StopMoveType.class)), //
            Map.entry(DPTXlatorBoolean.DPT_WINDOW_DOOR.getID(), Set.of(OpenClosedType.class)), //
            Map.entry(DPTXlatorBoolean.DPT_SCENE_AB.getID(), Set.of(DecimalType.class)), //
            Map.entry(DPTXlator3BitControlled.DPT_CONTROL_BLINDS.getID(), Set.of(UpDownType.class)), //
            Map.entry(DPTXlator8BitUnsigned.DPT_SCALING.getID(),
                    Set.of(QuantityType.class, DecimalType.class, PercentType.class)), //
            Map.entry(DPTXlator8BitSigned.DPT_STATUS_MODE3.getID(), Set.of(StringType.class)), //
            Map.entry(DPTXlatorString.DPT_STRING_8859_1.getID(), Set.of(StringType.class)), //
            Map.entry(DPTXlatorString.DPT_STRING_ASCII.getID(), Set.of(StringType.class)));

    private DPTUtil() {
        // prevent instantiation
    }

    /**
     * get allowed openHAB types for given DPT
     *
     * @param dptId the datapoint type id
     * @return Set of supported openHAB types (command or state)
     */
    public static Set<Class<? extends Type>> getAllowedTypes(String dptId) {
        Set<Class<? extends Type>> allowedTypes = DPT_TYPE_MAP.get(dptId);
        if (allowedTypes == null) {
            Matcher m = DPT_PATTERN.matcher(dptId);
            if (!m.matches()) {
                LOGGER.warn("getAllowedTypes couldn't identify main number in dptID '{}'", dptId);
                return Set.of();
            }

            allowedTypes = DPT_MAIN_TYPE_MAP.getOrDefault(m.group("main"), Set.of());
        }
        return allowedTypes;
    }
}
