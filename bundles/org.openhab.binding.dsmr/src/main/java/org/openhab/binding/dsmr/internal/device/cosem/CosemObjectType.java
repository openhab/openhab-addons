/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.device.cosem;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.measure.quantity.Time;

import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterConstants;

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
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Cosem subclasses made into factory classes and introduced quantity type
 */
public enum CosemObjectType {
    UNKNOWN(new OBISIdentifier(-1, DSMRMeterConstants.UNKNOWN_CHANNEL, -1, -1, -1, null), CosemString.INSTANCE),

    /* General messages */
    P1_VERSION_OUTPUT(new OBISIdentifier(1, 3, 0, 2, 8, null), CosemString.INSTANCE),
    P1_TIMESTAMP(new OBISIdentifier(0, 0, 1, 0, 0, null), new CosemDate("")),
    P1_TEXT_CODE(new OBISIdentifier(0, 0, 96, 13, 1, null), CosemHexString.INSTANCE),
    P1_TEXT_STRING(new OBISIdentifier(0, 0, 96, 13, 0, null), CosemHexString.INSTANCE),

    /* Generic Meter Cosem Object types */
    METER_EQUIPMENT_IDENTIFIER(new OBISIdentifier(0, null, 96, 1, 0, null), CosemHexString.INSTANCE),
    METER_DEVICE_TYPE(new OBISIdentifier(0, null, 24, 1, 0, null), CosemString.INSTANCE),
    METER_VALVE_SWITCH_POSITION(new OBISIdentifier(0, null, 24, 4, 0, null), CosemDecimal.INSTANCE),

    /* Electricity Meter */
    EMETER_EQUIPMENT_IDENTIFIER_V2_X(new OBISIdentifier(0, 0, 42, 0, 0, null), CosemString.INSTANCE),
    EMETER_EQUIPMENT_IDENTIFIER(new OBISIdentifier(0, null, 96, 1, 1, null), CosemHexString.INSTANCE),
    EMETER_VALUE(new OBISIdentifier(0, null, 24, 2, 1, null), CosemDate.INSTANCE, CosemQuantity.KILO_WATT_HOUR),
    EMETER_DELIVERY_TARIFF0(new OBISIdentifier(1, null, 1, 8, 0, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_DELIVERY_TARIFF1(new OBISIdentifier(1, null, 1, 8, 1, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_DELIVERY_TARIFF2(new OBISIdentifier(1, null, 1, 8, 2, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_DELIVERY_TARIFF0_ANTIFRAUD(new OBISIdentifier(1, null, 15, 8, 0, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_DELIVERY_TARIFF1_ANTIFRAUD(new OBISIdentifier(1, null, 15, 8, 1, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_DELIVERY_TARIFF2_ANTIFRAUD(new OBISIdentifier(1, null, 15, 8, 2, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_PRODUCTION_TARIFF0(new OBISIdentifier(1, null, 2, 8, 0, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_PRODUCTION_TARIFF1(new OBISIdentifier(1, null, 2, 8, 1, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_PRODUCTION_TARIFF2(new OBISIdentifier(1, null, 2, 8, 2, null), CosemQuantity.KILO_WATT_HOUR),
    EMETER_TARIFF_INDICATOR(new OBISIdentifier(0, null, 96, 14, 0, null), CosemString.INSTANCE),
    EMETER_ACTIVE_IMPORT_POWER(new OBISIdentifier(1, null, 15, 7, 0, null), CosemQuantity.WATT),
    EMETER_ACTUAL_DELIVERY(new OBISIdentifier(1, 0, 1, 7, 0, null), CosemQuantity.KILO_WATT),
    EMETER_ACTUAL_PRODUCTION(new OBISIdentifier(1, 0, 2, 7, 0, null), CosemQuantity.KILO_WATT),
    EMETER_TRESHOLD_A_V2_1(new OBISIdentifier(1, 0, 17, 0, 0, null), CosemQuantity.AMPERE),
    EMETER_TRESHOLD_A(new OBISIdentifier(0, 0, 17, 0, 0, null), CosemQuantity.AMPERE),
    EMETER_TRESHOLD_KWH(new OBISIdentifier(0, 0, 17, 0, 0, null), CosemQuantity.KILO_WATT),
    EMETER_SWITCH_POSITION_V2_1(new OBISIdentifier(1, 0, 96, 3, 10, null), CosemDecimal.INSTANCE),
    EMETER_SWITCH_POSITION(new OBISIdentifier(0, 0, 96, 3, 10, null), CosemDecimal.INSTANCE),
    EMETER_POWER_FAILURES(new OBISIdentifier(0, 0, 96, 7, 21, null), CosemDecimal.INSTANCE),
    EMETER_LONG_POWER_FAILURES(new OBISIdentifier(0, 0, 96, 7, 9, null), CosemDecimal.INSTANCE),
    EMETER_POWER_FAILURE_LOG(new OBISIdentifier(1, 0, 99, 97, 0, null), 2, new CosemDecimal("entries"),
            new CosemString("obisId"),
            /* Next 2 descriptors are repeating */
            CosemDate.INSTANCE, new CosemQuantity<Time>(SmartHomeUnits.SECOND, "duration")),
    EMETER_VOLTAGE_SAGS_L1(new OBISIdentifier(1, 0, 32, 32, 0, null), CosemDecimal.INSTANCE),
    EMETER_VOLTAGE_SAGS_L2(new OBISIdentifier(1, 0, 52, 32, 0, null), CosemDecimal.INSTANCE),
    EMETER_VOLTAGE_SAGS_L3(new OBISIdentifier(1, 0, 72, 32, 0, null), CosemDecimal.INSTANCE),
    EMETER_VOLTAGE_SWELLS_L1(new OBISIdentifier(1, 0, 32, 36, 0, null), CosemDecimal.INSTANCE),
    EMETER_VOLTAGE_SWELLS_L2(new OBISIdentifier(1, 0, 52, 36, 0, null), CosemDecimal.INSTANCE),
    EMETER_VOLTAGE_SWELLS_L3(new OBISIdentifier(1, 0, 72, 36, 0, null), CosemDecimal.INSTANCE),
    EMETER_INSTANT_CURRENT_L1(new OBISIdentifier(1, 0, 31, 7, 0, null), CosemQuantity.AMPERE),
    EMETER_INSTANT_CURRENT_L2(new OBISIdentifier(1, 0, 51, 7, 0, null), CosemQuantity.AMPERE),
    EMETER_INSTANT_CURRENT_L3(new OBISIdentifier(1, 0, 71, 7, 0, null), CosemQuantity.AMPERE),
    EMETER_INSTANT_POWER_DELIVERY_L1(new OBISIdentifier(1, 0, 21, 7, 0, null), CosemQuantity.KILO_WATT),
    EMETER_INSTANT_POWER_DELIVERY_L2(new OBISIdentifier(1, 0, 41, 7, 0, null), CosemQuantity.KILO_WATT),
    EMETER_INSTANT_POWER_DELIVERY_L3(new OBISIdentifier(1, 0, 61, 7, 0, null), CosemQuantity.KILO_WATT),
    EMETER_INSTANT_POWER_PRODUCTION_L1(new OBISIdentifier(1, 0, 22, 7, 0, null), CosemQuantity.KILO_WATT),
    EMETER_INSTANT_POWER_PRODUCTION_L2(new OBISIdentifier(1, 0, 42, 7, 0, null), CosemQuantity.KILO_WATT),
    EMETER_INSTANT_POWER_PRODUCTION_L3(new OBISIdentifier(1, 0, 62, 7, 0, null), CosemQuantity.KILO_WATT),
    EMETER_INSTANT_VOLTAGE_L1(new OBISIdentifier(1, 0, 32, 7, 0, null), CosemQuantity.VOLT),
    EMETER_INSTANT_VOLTAGE_L2(new OBISIdentifier(1, 0, 52, 7, 0, null), CosemQuantity.VOLT),
    EMETER_INSTANT_VOLTAGE_L3(new OBISIdentifier(1, 0, 72, 7, 0, null), CosemQuantity.VOLT),

    /* Gas Meter */
    GMETER_EQUIPMENT_IDENTIFIER_V2(new OBISIdentifier(7, 0, 0, 0, 0, null), CosemString.INSTANCE),
    GMETER_24H_DELIVERY_V2(new OBISIdentifier(7, 0, 23, 1, 0, null), CosemQuantity.CUBIC_METRE, CosemDate.INSTANCE),
    GMETER_24H_DELIVERY_COMPENSATED_V2(new OBISIdentifier(7, 0, 23, 2, 0, null), CosemQuantity.CUBIC_METRE,
            CosemDate.INSTANCE),
    GMETER_VALUE_V3(new OBISIdentifier(0, null, 24, 3, 0, null), CosemDate.INSTANCE, // Time stamp off the reading
            new CosemString("val1"), // Specification is not clear what this value is
            new CosemDecimal("val2"), // Specification is not clear what this value is
            new CosemDecimal("val3"), // Specification is not clear what this value is
            new CosemString("obisId"), // String containing a OBIS Identifier
            new CosemString("unit"), // String containing the type (m3)
            CosemDecimal.INSTANCE),
    GMETER_VALVE_POSITION_V2_1(new OBISIdentifier(7, 0, 96, 3, 10, null), CosemDecimal.INSTANCE),
    GMETER_VALVE_POSITION_V2_2(new OBISIdentifier(7, 0, 24, 4, 0, null), CosemDecimal.INSTANCE),

    /* Heating Meter */
    HMETER_EQUIPMENT_IDENTIFIER_V2_2(new OBISIdentifier(5, 0, 0, 0, 0, null), CosemString.INSTANCE),
    HMETER_VALUE_V2(new OBISIdentifier(5, 0, 1, 0, 0, null), CosemQuantity.GIGA_JOULE, CosemDate.INSTANCE),

    /* Cooling Meter */
    CMETER_EQUIPMENT_IDENTIFIER_V2_2(new OBISIdentifier(6, 0, 0, 0, 0, null), CosemString.INSTANCE),
    CMETER_VALUE_V2(new OBISIdentifier(6, 0, 1, 0, 0, null), CosemQuantity.GIGA_JOULE, CosemDate.INSTANCE),

    /* Water Meter */
    WMETER_EQUIPMENT_IDENTIFIER_V2_2(new OBISIdentifier(8, 0, 0, 0, 0, null), CosemString.INSTANCE),
    WMETER_VALUE_V2(new OBISIdentifier(8, 0, 1, 0, 0, null), CosemQuantity.CUBIC_METRE, CosemDate.INSTANCE),
    WMETER_VALUE_V3(new OBISIdentifier(0, null, 24, 3, 0, null), CosemQuantity.CUBIC_METRE),

    /* M3 Meter (Gas, Water) */
    M3METER_VALUE(new OBISIdentifier(0, null, 24, 2, 1, null), CosemDate.INSTANCE, CosemQuantity.CUBIC_METRE),

    /* GJ Meter (Heating, Cooling) */
    GJMETER_VALUE_V3(new OBISIdentifier(0, null, 24, 3, 0, null), CosemQuantity.GIGA_JOULE),
    GJMETER_VALUE_V4(new OBISIdentifier(0, null, 24, 2, 1, null), CosemDate.INSTANCE, CosemQuantity.GIGA_JOULE),

    /* Generic Meter (DSMR v3 only) */
    GENMETER_VALUE_V3(new OBISIdentifier(0, null, 24, 3, 0, null), CosemDecimal.INSTANCE),

    /* Additional Luxembourgish Smarty Electricity */
    EMETER_TOTAL_IMPORTED_ENERGY_REGISTER_Q(new OBISIdentifier(1, null, 3, 8, 0, null), CosemQuantity.KILO_VAR_HOUR),
    EMETER_TOTAL_EXPORTED_ENERGY_REGISTER_Q(new OBISIdentifier(1, null, 4, 8, 0, null), CosemQuantity.KILO_VAR_HOUR),
    // The actual reactive's and threshold have no unit in the data and therefore are not quantity types.
    EMETER_ACTUAL_REACTIVE_DELIVERY(new OBISIdentifier(1, 0, 3, 7, 0, null), CosemDecimal.INSTANCE),
    EMETER_ACTUAL_REACTIVE_PRODUCTION(new OBISIdentifier(1, 0, 4, 7, 0, null), CosemDecimal.INSTANCE),
    EMETER_ACTIVE_THRESHOLD_SMAX(new OBISIdentifier(0, 0, 17, 0, 0, null), CosemDecimal.INSTANCE),
    EMETER_INSTANT_REACTIVE_POWER_DELIVERY_L1(new OBISIdentifier(1, 0, 23, 7, 0, null), CosemQuantity.KILO_VAR),
    EMETER_INSTANT_REACTIVE_POWER_DELIVERY_L2(new OBISIdentifier(1, 0, 43, 7, 0, null), CosemQuantity.KILO_VAR),
    EMETER_INSTANT_REACTIVE_POWER_DELIVERY_L3(new OBISIdentifier(1, 0, 63, 7, 0, null), CosemQuantity.KILO_VAR),
    EMETER_INSTANT_REACTIVE_POWER_PRODUCTION_L1(new OBISIdentifier(1, 0, 24, 7, 0, null), CosemQuantity.KILO_VAR),
    EMETER_INSTANT_REACTIVE_POWER_PRODUCTION_L2(new OBISIdentifier(1, 0, 44, 7, 0, null), CosemQuantity.KILO_VAR),
    EMETER_INSTANT_REACTIVE_POWER_PRODUCTION_L3(new OBISIdentifier(1, 0, 64, 7, 0, null), CosemQuantity.KILO_VAR);

    /** OBIS reduced identifier */
    public final OBISIdentifier obisId;

    /** COSEM value descriptors */
    private final List<CosemValueDescriptor<?>> descriptors;
    private final List<CosemValueDescriptor<?>> repeatingDescriptors;

    /**
     * Constructs a new CosemObjectType
     *
     * @param obisId {@link OBISIdentifier} containing the obisIdentifier for CosemObjectType
     * @param descriptors variable parameter list of {@link CosemValueDescriptor}
     */
    CosemObjectType(OBISIdentifier obisId, CosemValueDescriptor<?>... descriptors) {
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
    CosemObjectType(OBISIdentifier obisId, int nrOfRepeatingDescriptors, CosemValueDescriptor<?>... descriptors) {
        this.obisId = obisId;
        if (nrOfRepeatingDescriptors == 0) {
            this.descriptors = Arrays.asList(descriptors);
            this.repeatingDescriptors = Collections.emptyList();
        } else {
            List<CosemValueDescriptor<?>> allDescriptors = Arrays.asList(descriptors);

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
    public Entry<String, CosemValueDescriptor<?>> getDescriptor(int idx) {
        if (idx >= descriptors.size() && !repeatingDescriptors.isEmpty()) {
            /* We have a repeating list, find the correct repeating descriptor */
            int repeatingIdx = (idx - descriptors.size()) % repeatingDescriptors.size();

            CosemValueDescriptor<?> descriptor = repeatingDescriptors.get(repeatingIdx);

            /* The repeating descriptor must have a specific channel */
            int repeatCount = (idx - descriptors.size()) / repeatingDescriptors.size();

            return new SimpleEntry<>(descriptor.getChannelId() + repeatCount, descriptor);
        } else if (idx < descriptors.size()) {
            CosemValueDescriptor<?> descriptor = descriptors.get(idx);

            return new SimpleEntry<>(descriptor.getChannelId(), descriptor);
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
