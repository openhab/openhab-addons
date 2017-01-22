/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.device.cosem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openhab.binding.dsmr.meter.DSMRMeterConstants;

/**
 * Enumeration Cosem Object types
 * <p>
 * Each Cosem Object type consists of the following attributes:
 * <p>
 * <ul>
 * <li>OBIS Identifier (reduced form)
 * <li>List of value descriptors (See {@link CosemValueDescriptor})
 * <li>List of repeating value descriptors (See {@link CosemValueDescriptor}). Repeating value descriptors will always
 * be the last descriptors.
 * </ul>
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public enum CosemObjectType {
    UNKNOWN(new OBISIdentifier(null, DSMRMeterConstants.UNKNOWN_CHANNEL, -1, -1, null, null),
            new CosemValueDescriptor(CosemString.class, "")),

    /* General messages */
    P1_VERSION_OUTPUT(new OBISIdentifier(1, 3, 0, 2, 8, null), new CosemValueDescriptor(CosemString.class, "")),
    P1_TIMESTAMP(new OBISIdentifier(0, 0, 1, 0, 0, null), new CosemValueDescriptor(CosemDate.class, "")),
    P1_TEXT_CODE(new OBISIdentifier(0, 0, 96, 13, 1, null), new CosemValueDescriptor(CosemString.class, "")),
    P1_TEXT_STRING(new OBISIdentifier(0, 0, 96, 13, 0, null), new CosemValueDescriptor(CosemString.class, "")),

    /* Generic Meter Cosem Object types */
    METER_EQUIPMENT_IDENTIFIER(new OBISIdentifier(0, null, 96, 1, 0, null),
            new CosemValueDescriptor(CosemHexString.class, "")),
    METER_DEVICE_TYPE(new OBISIdentifier(0, null, 24, 1, 0, null), new CosemValueDescriptor(CosemString.class, "")),
    METER_VALVE_SWITCH_POSITION(new OBISIdentifier(0, null, 24, 4, 0, null),
            new CosemValueDescriptor(CosemInteger.class, "")),

    /* Electricity Meter */
    EMETER_EQUIPMENT_IDENTIFIER_V2_X(new OBISIdentifier(0, null, 42, 0, 0, null),
            new CosemValueDescriptor(CosemString.class, "")),
    EMETER_EQUIPMENT_IDENTIFIER(new OBISIdentifier(0, null, 96, 1, 1, null),
            new CosemValueDescriptor(CosemHexString.class, "")),
    EMETER_VALUE(new OBISIdentifier(0, null, 24, 2, 1, null), new CosemValueDescriptor(CosemDate.class, ""),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_DELIVERY_TARIFF0(new OBISIdentifier(1, null, 1, 8, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_DELIVERY_TARIFF1(new OBISIdentifier(1, null, 1, 8, 1, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_DELIVERY_TARIFF2(new OBISIdentifier(1, null, 1, 8, 2, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_DELIVERY_TARIFF0_ANTIFRAUD(new OBISIdentifier(1, null, 15, 8, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_DELIVERY_TARIFF1_ANTIFRAUD(new OBISIdentifier(1, null, 15, 8, 1, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_DELIVERY_TARIFF2_ANTIFRAUD(new OBISIdentifier(1, null, 15, 8, 2, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_PRODUCTION_TARIFF0(new OBISIdentifier(1, null, 2, 8, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_PRODUCTION_TARIFF1(new OBISIdentifier(1, null, 2, 8, 1, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_PRODUCTION_TARIFF2(new OBISIdentifier(1, null, 2, 8, 2, null),
            new CosemValueDescriptor(CosemFloat.class, "kWh")),
    EMETER_TARIFF_INDICATOR(new OBISIdentifier(0, null, 96, 14, 0, null),
            new CosemValueDescriptor(CosemString.class, "")),
    EMETER_ACTIVE_IMPORT_POWER(new OBISIdentifier(1, null, 15, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "W")),
    EMETER_ACTUAL_DELIVERY(new OBISIdentifier(1, null, 1, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_ACTUAL_PRODUCTION(new OBISIdentifier(1, null, 2, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_TRESHOLD_A_V2_1(new OBISIdentifier(1, null, 17, 0, 0, null),
            new CosemValueDescriptor(CosemInteger.class, "A")),
    EMETER_TRESHOLD_A(new OBISIdentifier(0, null, 17, 0, 0, null), new CosemValueDescriptor(CosemInteger.class, "A")),
    EMETER_TRESHOLD_KWH(new OBISIdentifier(0, null, 17, 0, 0, null), new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_SWITCH_POSITION_V2_1(new OBISIdentifier(1, null, 96, 3, 10, null),
            new CosemValueDescriptor(CosemInteger.class, "")),
    EMETER_SWITCH_POSITION(new OBISIdentifier(0, null, 96, 3, 10, null),
            new CosemValueDescriptor(CosemInteger.class, "")),
    EMETER_POWER_FAILURES(new OBISIdentifier(0, null, 96, 7, 21, null), new CosemValueDescriptor(CosemFloat.class, "")),
    EMETER_LONG_POWER_FAILURES(new OBISIdentifier(0, null, 96, 7, 9, null),
            new CosemValueDescriptor(CosemFloat.class, "")),
    EMETER_POWER_FAILURE_LOG(new OBISIdentifier(1, null, 99, 97, 0, null), 2,
            new CosemValueDescriptor(CosemInteger.class, ""), new CosemValueDescriptor(CosemString.class, ""),
            /* Next 2 descriptors are repeating */
            new CosemValueDescriptor(CosemDate.class, ""), new CosemValueDescriptor(CosemInteger.class, "s")),
    EMETER_VOLTAGE_SAGS_L1(new OBISIdentifier(1, null, 32, 32, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "")),
    EMETER_VOLTAGE_SAGS_L2(new OBISIdentifier(1, null, 52, 32, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "")),
    EMETER_VOLTAGE_SAGS_L3(new OBISIdentifier(1, null, 72, 32, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "")),
    EMETER_VOLTAGE_SWELLS_L1(new OBISIdentifier(1, null, 32, 36, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "")),
    EMETER_VOLTAGE_SWELLS_L2(new OBISIdentifier(1, null, 52, 36, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "")),
    EMETER_VOLTAGE_SWELLS_L3(new OBISIdentifier(1, null, 72, 36, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "")),
    EMETER_INSTANT_CURRENT_L1(new OBISIdentifier(1, null, 31, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "A")),
    EMETER_INSTANT_CURRENT_L2(new OBISIdentifier(1, null, 51, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "A")),
    EMETER_INSTANT_CURRENT_L3(new OBISIdentifier(1, null, 71, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "A")),
    EMETER_INSTANT_POWER_DELIVERY_L1(new OBISIdentifier(1, null, 21, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_INSTANT_POWER_DELIVERY_L2(new OBISIdentifier(1, null, 41, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_INSTANT_POWER_DELIVERY_L3(new OBISIdentifier(1, null, 61, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_INSTANT_POWER_PRODUCTION_L1(new OBISIdentifier(1, null, 22, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_INSTANT_POWER_PRODUCTION_L2(new OBISIdentifier(1, null, 42, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_INSTANT_POWER_PRODUCTION_L3(new OBISIdentifier(1, null, 62, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "kW")),
    EMETER_INSTANT_VOLTAGE_L1(new OBISIdentifier(1, null, 32, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "V")),
    EMETER_INSTANT_VOLTAGE_L2(new OBISIdentifier(1, null, 52, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "V")),
    EMETER_INSTANT_VOLTAGE_L3(new OBISIdentifier(1, null, 72, 7, 0, null),
            new CosemValueDescriptor(CosemFloat.class, "V")),

    /* Gas Meter */
    GMETER_EQUIPMENT_IDENTIFIER_V2(new OBISIdentifier(7, 0, 0, 0, 0, null),
            new CosemValueDescriptor(CosemString.class, "")),
    GMETER_24H_DELIVERY_V2(new OBISIdentifier(7, 0, 23, 1, 0, null), new CosemValueDescriptor(CosemString.class, "m3"),
            new CosemValueDescriptor(CosemDate.class, "")),
    GMETER_24H_DELIVERY_COMPENSATED_V2(new OBISIdentifier(7, 0, 23, 2, 0, null),
            new CosemValueDescriptor(CosemString.class, "m3"), new CosemValueDescriptor(CosemDate.class, "")),
    GMETER_VALUE_V3(new OBISIdentifier(0, null, 24, 3, 0, null), 1, // 1 repeating CosemFloat value
            new CosemValueDescriptor(CosemDate.class, ""), // Time stamp off the reading
            new CosemValueDescriptor(CosemString.class, ""), // Specification is not clear what this value is
            new CosemValueDescriptor(CosemInteger.class, ""), // Specification is not clear what this value is
            new CosemValueDescriptor(CosemInteger.class, ""), // Specification is not clear what this value is
            new CosemValueDescriptor(CosemString.class, ""), // String containing a OBIS Identifier
            new CosemValueDescriptor(CosemString.class, ""), // String containing the type (m3)
            new CosemValueDescriptor(CosemFloat.class, "")),
    GMETER_VALVE_POSITION_V2_1(new OBISIdentifier(7, 0, 96, 3, 10, null),
            new CosemValueDescriptor(CosemInteger.class, "")),
    GMETER_VALVE_POSITION_V2_2(new OBISIdentifier(7, 0, 24, 4, 0, null),
            new CosemValueDescriptor(CosemInteger.class, "")),

    /* Heating Meter */
    HMETER_EQUIPMENT_IDENTIFIER_V2_2(new OBISIdentifier(5, 0, 0, 0, 0, null),
            new CosemValueDescriptor(CosemString.class, "")),
    HMETER_VALUE_V2(new OBISIdentifier(5, 0, 1, 0, 0, null), new CosemValueDescriptor(CosemFloat.class, "GJ"),
            new CosemValueDescriptor(CosemDate.class, "")),

    /* Cooling Meter */
    CMETER_EQUIPMENT_IDENTIFIER_V2_2(new OBISIdentifier(6, 0, 0, 0, 0, null),
            new CosemValueDescriptor(CosemString.class, "")),
    CMETER_VALUE_V2(new OBISIdentifier(6, 0, 1, 0, 0, null), new CosemValueDescriptor(CosemFloat.class, "GJ"),
            new CosemValueDescriptor(CosemDate.class, "")),

    /* Water Meter */
    WMETER_EQUIPMENT_IDENTIFIER_V2_2(new OBISIdentifier(8, 0, 0, 0, 0, null),
            new CosemValueDescriptor(CosemString.class, "")),
    WMETER_VALUE_V2(new OBISIdentifier(8, 0, 1, 0, 0, null), new CosemValueDescriptor(CosemFloat.class, "m3"),
            new CosemValueDescriptor(CosemDate.class, "")),
    WMETER_VALUE_V3(new OBISIdentifier(0, null, 24, 3, 0, null), new CosemValueDescriptor(CosemFloat.class, "m3")),

    /* M3 Meter (Gas, Water) */
    M3METER_VALUE(new OBISIdentifier(0, null, 24, 2, 1, null), new CosemValueDescriptor(CosemDate.class, ""),
            new CosemValueDescriptor(CosemFloat.class, "m3")),

    /* GJ Meter (Heating, Cooling) */
    GJMETER_VALUE_V3(new OBISIdentifier(0, null, 24, 3, 0, null), new CosemValueDescriptor(CosemFloat.class, "GJ")),
    GJMETER_VALUE_V4(new OBISIdentifier(0, null, 24, 2, 1, null), new CosemValueDescriptor(CosemDate.class, ""),
            new CosemValueDescriptor(CosemFloat.class, "GJ")),

    /* Generic Meter (DSMR v3 only) */
    GENMETER_VALUE_V3(new OBISIdentifier(0, null, 24, 3, 0, null), new CosemValueDescriptor(CosemFloat.class, ""));

    /** OBIS reduced identifier */
    public final OBISIdentifier obisId;

    /** COSEM value descriptors */
    public final List<CosemValueDescriptor> descriptors;
    public final List<CosemValueDescriptor> repeatingDescriptors;

    /**
     * Constructs a new CosemObjectType
     *
     * @param obisId {@link OBISIdentifier} containing the obisIdentifier for CosemObjectType
     * @param descriptors variable parameter list of {@link CosemValueDescriptor}
     */
    private CosemObjectType(OBISIdentifier obisId, CosemValueDescriptor... descriptors) {
        this(obisId, 0, descriptors);
    }

    /**
     * Constructs a new CosemObjectType
     *
     * @param obisId {@link OBISIdentifier} containing the obisIdentifier for CosemObjectType
     * @param nrOfRepeatingDescriptors nr of repeating descriptors (this are the last n descriptors in the variable list
     *            descriptors)
     * @param descriptors variable parameter list of {@link CosemValueDescriptor}
     */
    private CosemObjectType(OBISIdentifier obisId, int nrOfRepeatingDescriptors, CosemValueDescriptor... descriptors) {
        this.obisId = obisId;
        if (nrOfRepeatingDescriptors == 0) {
            this.descriptors = Arrays.asList(descriptors);
            this.repeatingDescriptors = Collections.emptyList();
        } else {
            List<CosemValueDescriptor> allDescriptors = Arrays.asList(descriptors);

            /*
             * The last nrOfRepeatingDescriptors CosemValueDescriptor will go into the repeatingDescriptor list.
             * The other descriptors will got into the regular list
             */
            this.descriptors = allDescriptors.subList(0, allDescriptors.size() - nrOfRepeatingDescriptors);
            this.repeatingDescriptors = allDescriptors.subList(this.descriptors.size(),
                    this.descriptors.size() + nrOfRepeatingDescriptors);
        }
    }

    /**
     * Returns the {@link CosemValueDescriptor} for the specified index.
     * If the list contains repeating descriptors the specified index will mapped onto the repeating list
     *
     * e.g. If the list contains 4 descriptors and the last 2 are repeating, idx=6 will return the 4th descriptor.
     *
     * The idx is < 0 or outside a non-repeating descriptorslist size null is returned
     *
     * @param idx the CosemValueDescriptor to return
     * @return the CosemValueDescriptor or null if not found.
     */
    public CosemValueDescriptor getDescriptor(int idx) {
        if (idx >= descriptors.size() && repeatingDescriptors.size() > 0) {
            /* We have a repeating list, find the correct repeating descriptor */
            int repeatingIdx = (idx - descriptors.size()) % repeatingDescriptors.size();

            return repeatingDescriptors.get(repeatingIdx);
        } else if (idx < descriptors.size()) {
            return descriptors.get(idx);
        } else {
            return null;
        }
    }

    /**
     * Returns if this CosemObjectType supports the requested number of values.
     *
     * Note that for repeating list the number of values must match the repeating pattern.
     *
     * So if the list contains 4 values and the last 2 are repeating, nrOfValues = 6 will return true,
     * however nrOfvalues = 7 will return false (only 4, 6, 8, etc... is allowed)
     *
     * @param nrOfValues number of values to check.
     *
     * @return true if this CosemObjectType support the requested number of values, false otherwise.
     */
    public boolean supportsNrOfValues(int nrOfValues) {
        if (repeatingDescriptors.size() == 0) {
            return nrOfValues == descriptors.size();
        } else {
            /* There are repeating descriptors */
            return ((nrOfValues - descriptors.size()) % repeatingDescriptors.size()) == 0;
        }
    }
}
