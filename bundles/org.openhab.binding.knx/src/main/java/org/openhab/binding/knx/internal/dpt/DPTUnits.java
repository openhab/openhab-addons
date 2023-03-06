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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

import tuwien.auto.calimero.dptxlator.DPT;
import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator4ByteUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlator64BitSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitSigned;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;

/**
 * This class provides the units for values depending on the DPT (if available)
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DPTUnits {
    private static final Map<String, String> DPT_UNIT_MAP = new HashMap<>();

    private DPTUnits() {
        // prevent instantiation
    }

    /**
     * get unit string for a given DPT
     *
     * @param dptId the KNX DPT
     * @return unit string
     */
    public static @Nullable String getUnitForDpt(String dptId) {
        return DPT_UNIT_MAP.get(dptId);
    }

    /**
     * for testing purposes only
     *
     * @return stream of all unit strings
     */
    static Stream<String> getAllUnitStrings() {
        return DPT_UNIT_MAP.values().stream();
    }

    static {
        // try to get units from Calimeros "unit" field in DPTXlators
        List<Class<? extends DPTXlator>> translators = List.of(DPTXlator2ByteUnsigned.class, DPTXlator2ByteFloat.class,
                DPTXlator4ByteUnsigned.class, DPTXlator4ByteSigned.class, DPTXlator4ByteFloat.class,
                DPTXlator64BitSigned.class);

        for (Class<? extends DPTXlator> translator : translators) {
            Field[] fields = translator.getFields();
            for (Field field : fields) {
                try {
                    Object o = field.get(null);
                    if (o instanceof DPT) {
                        DPT dpt = (DPT) o;
                        String unit = dpt.getUnit().replaceAll(" ", "");
                        // Calimero provides some units (like "ms⁻²") that can't be parsed by our library because of the
                        // negative exponent
                        // replace with /
                        int index = unit.indexOf("⁻");
                        if (index != -1) {
                            unit = unit.substring(0, index - 1) + "/" + unit.substring(index - 1).replace("⁻", "");
                        }
                        if (!unit.isEmpty()) {
                            DPT_UNIT_MAP.put(dpt.getID(), unit);
                        }
                    }
                } catch (IllegalAccessException e) {
                    // ignore errors
                }
            }
        }

        // override/fix units where Calimero data is unparsable or missing

        // 8 bit unsigned (DPT 5)
        DPT_UNIT_MAP.put(DPTXlator8BitUnsigned.DPT_SCALING.getID(), Units.PERCENT.getSymbol());
        DPT_UNIT_MAP.put(DPTXlator8BitUnsigned.DPT_PERCENT_U8.getID(), Units.PERCENT.getSymbol());

        // 8bit signed (DPT 6)
        DPT_UNIT_MAP.put(DPTXlator8BitSigned.DPT_PERCENT_V8.getID(), Units.PERCENT.getSymbol());

        // two byte unsigned (DPT 7)
        DPT_UNIT_MAP.remove(DPTXlator2ByteUnsigned.DPT_VALUE_2_UCOUNT.getID()); // counts have no unit

        // two byte signed (DPT 8, DPTXlator is missing in calimero 2.5-M1)
        // TODO: 2byte signed (DPT 8) use DptXlator2ByteSigned after 2.5 release of calimero
        DPT_UNIT_MAP.put("8.002", MetricPrefix.MILLI(Units.SECOND).toString());
        DPT_UNIT_MAP.put("8.003", MetricPrefix.MILLI(Units.SECOND).toString());
        DPT_UNIT_MAP.put("8.004", MetricPrefix.MILLI(Units.SECOND).toString());
        DPT_UNIT_MAP.put("8.005", Units.SECOND.toString());
        DPT_UNIT_MAP.put("8.006", Units.MINUTE.toString());
        DPT_UNIT_MAP.put("8.007", Units.HOUR.toString());
        DPT_UNIT_MAP.put("8.010", Units.PERCENT.toString());
        DPT_UNIT_MAP.put("8.011", Units.DEGREE_ANGLE.toString());
        DPT_UNIT_MAP.put("8.012", SIUnits.METRE.toString());

        // 4 byte unsigned (DPT 12)
        DPT_UNIT_MAP.put(DPTXlator4ByteUnsigned.DptVolumeLiquid.getID(), Units.LITRE.toString());
        DPT_UNIT_MAP.remove(DPTXlator4ByteUnsigned.DPT_VALUE_4_UCOUNT.getID()); // counts have no unit

        // 4 byte signed (DPT 13)
        DPT_UNIT_MAP.put(DPTXlator4ByteSigned.DPT_ACTIVE_ENERGY_KWH.getID(), Units.KILOWATT_HOUR.toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteSigned.DPT_REACTIVE_ENERGY.getID(), Units.VAR_HOUR.toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteSigned.DPT_REACTIVE_ENERGY_KVARH.getID(), Units.KILOVAR_HOUR.toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteSigned.DPT_APPARENT_ENERGY_KVAH.getID(), Units.KILOVOLT_AMPERE.toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteSigned.DPT_FLOWRATE.getID(), Units.CUBICMETRE_PER_HOUR.toString());
        DPT_UNIT_MAP.remove(DPTXlator4ByteSigned.DPT_COUNT.getID()); // counts have no unit

        // four byte float (DPT 14)
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_CONDUCTANCE.getID(), Units.SIEMENS.toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_ANGULAR_MOMENTUM.getID(),
                Units.JOULE.multiply(Units.SECOND).toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_ACTIVITY.getID(), Units.BECQUEREL.toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_ELECTRICAL_CONDUCTIVITY.getID(),
                Units.SIEMENS.divide(SIUnits.METRE).toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_TORQUE.getID(), Units.NEWTON.multiply(SIUnits.METRE).toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_RESISTIVITY.getID(), Units.OHM.multiply(SIUnits.METRE).toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_ELECTRIC_DIPOLEMOMENT.getID(),
                Units.COULOMB.multiply(SIUnits.METRE).toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_ELECTRIC_FLUX.getID(), Units.VOLT.multiply(SIUnits.METRE).toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_MAGNETIC_MOMENT.getID(),
                Units.AMPERE.multiply(SIUnits.SQUARE_METRE).toString());
        DPT_UNIT_MAP.put(DPTXlator4ByteFloat.DPT_ELECTROMAGNETIC_MOMENT.getID(),
                Units.AMPERE.multiply(SIUnits.SQUARE_METRE).toString());

        // 64 bit signed (DPT 29)
        DPT_UNIT_MAP.put(DPTXlator64BitSigned.DPT_REACTIVE_ENERGY.getID(), Units.VAR_HOUR.toString());
    }
}
