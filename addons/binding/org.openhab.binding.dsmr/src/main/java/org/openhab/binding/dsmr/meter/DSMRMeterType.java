/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.meter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.dsmr.DSMRBindingConstants;
import org.openhab.binding.dsmr.device.cosem.CosemHexString;
import org.openhab.binding.dsmr.device.cosem.CosemObject;
import org.openhab.binding.dsmr.device.cosem.CosemObjectType;
import org.openhab.binding.dsmr.device.cosem.CosemString;
import org.openhab.binding.dsmr.device.cosem.CosemValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supported meters
 *
 * @author M. Volaart
 * @since 1.7.0
 */
public enum DSMRMeterType {
    /** DSMR V2 / V3 Device meter type (used for device (and not meter specific) related messages) */
    DEVICE_V2_V3(DSMRMeterKind.DEVICE, CosemObjectType.UNKNOWN, CosemObjectType.P1_TEXT_CODE, CosemObjectType.P1_TEXT_STRING),
    /** DSMR V4 Device meter type (used for device (and not meter specific) related messages) */
    DEVICE_V4(DSMRMeterKind.DEVICE, CosemObjectType.UNKNOWN, CosemObjectType.P1_TEXT_CODE, CosemObjectType.P1_TEXT_STRING, CosemObjectType.P1_VERSION_OUTPUT, CosemObjectType.P1_TIMESTAMP),
    /** DSMR V5 Device meter type (used for device (and not meter specific) related messages) */
    DEVICE_V5(DSMRMeterKind.DEVICE, CosemObjectType.UNKNOWN, CosemObjectType.P1_TEXT_STRING, CosemObjectType.P1_VERSION_OUTPUT, CosemObjectType.P1_TIMESTAMP),

    /** ACE4000 Electricity */
    ELECTRICITY_ACE4000(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF0, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_DELIVERY_TARIFF0_ANTIFRAUD, CosemObjectType.EMETER_DELIVERY_TARIFF1_ANTIFRAUD, CosemObjectType.EMETER_DELIVERY_TARIFF2_ANTIFRAUD, CosemObjectType.EMETER_PRODUCTION_TARIFF0, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_ACTIVE_IMPORT_POWER, CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.EMETER_SWITCH_POSITION),
    /** ACE4000 Gas meter */
    GAS_ACE4000(DSMRMeterKind.GAS, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.GMETER_24H_DELIVERY_V2, CosemObjectType.GMETER_VALVE_POSITION_V2_2),
    /** ACE4000 Heating meter */
    HEATING_ACE4000(DSMRMeterKind.HEATING, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.HMETER_VALUE_V2),
    /** ACE4000 Cooling meter */
    COOLING_ACE4000(DSMRMeterKind.COOLING, CosemObjectType.UNKNOWN, CosemObjectType.CMETER_VALUE_V2),
    /** ACE4000 Water meter */
    WATER_ACE4000(DSMRMeterKind.WATER, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.WMETER_VALUE_V2),
    /** ACE4000 first Slave electricity meter */
    SLAVE_ELECTRICITY1_ACE4000(DSMRMeterKind.SLAVE_ELECTRICITY1, CosemObjectType.UNKNOWN, CosemObjectType.EMETER_DELIVERY_TARIFF0, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF0, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_ACTIVE_IMPORT_POWER, CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.EMETER_SWITCH_POSITION),
    /** ACE4000 second Slave electricity meter */
    SLAVE_ELECTRICITY2_ACE4000(DSMRMeterKind.SLAVE_ELECTRICITY2, CosemObjectType.UNKNOWN, CosemObjectType.EMETER_DELIVERY_TARIFF0, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF0, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_ACTIVE_IMPORT_POWER, CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.EMETER_SWITCH_POSITION),

    /** DSMR V2.1 Electricity meter */
    ELECTRICITY_V2_1(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_TRESHOLD_A_V2_1, CosemObjectType.EMETER_SWITCH_POSITION_V2_1, CosemObjectType.EMETER_ACTUAL_DELIVERY),
    /** DSMR V2.1 Gas meter */
    GAS_V2_1(DSMRMeterKind.GAS, CosemObjectType.GMETER_EQUIPMENT_IDENTIFIER_V2, CosemObjectType.GMETER_EQUIPMENT_IDENTIFIER_V2, CosemObjectType.GMETER_24H_DELIVERY_V2, CosemObjectType.GMETER_24H_DELIVERY_COMPENSATED_V2, CosemObjectType.GMETER_VALVE_POSITION_V2_1),

    /** DSMR V2.2 Electricity meter */
    ELECTRICITY_V2_2(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER_V2_X, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.METER_VALVE_SWITCH_POSITION, CosemObjectType.EMETER_ACTUAL_DELIVERY),
    /** DSMR V2.2 Gas meter */
    GAS_V2_2(DSMRMeterKind.GAS, CosemObjectType.GMETER_EQUIPMENT_IDENTIFIER_V2, CosemObjectType.GMETER_EQUIPMENT_IDENTIFIER_V2, CosemObjectType.GMETER_24H_DELIVERY_V2, CosemObjectType.GMETER_24H_DELIVERY_COMPENSATED_V2, CosemObjectType.GMETER_VALVE_POSITION_V2_2),
    /** DSMR V2.2 Heating meter */
    HEATING_V2_2(DSMRMeterKind.HEATING, CosemObjectType.HMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.HMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.HMETER_VALUE_V2),
    /** DSMR V2.2 Cooling meter */
    COOLING_V2_2(DSMRMeterKind.COOLING, CosemObjectType.CMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.CMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.CMETER_VALUE_V2),
    /** DSMR V2.2 Water meter */
    WATER_V2_2(DSMRMeterKind.WATER, CosemObjectType.WMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.WMETER_EQUIPMENT_IDENTIFIER_V2_2, CosemObjectType.WMETER_VALUE_V2),

    /** DSMR V3.0 Electricity meter */
    ELECTRICITY_V3_0(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_TRESHOLD_A, CosemObjectType.EMETER_SWITCH_POSITION, CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION),
    /** DSMR V3.0 Gas meter */
    GAS_V3_0(DSMRMeterKind.GAS, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.GMETER_VALUE_V3, CosemObjectType.METER_VALVE_SWITCH_POSITION),
    /** DSMR V3.0 Water meter */
    WATER_V3_0(DSMRMeterKind.WATER, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.WMETER_VALUE_V3, CosemObjectType.METER_VALVE_SWITCH_POSITION),
    /** DSMR V3.0 GJ meter (heating, cooling) */
    GJ_V3_0(DSMRMeterKind.GJ, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.GJMETER_VALUE_V3, CosemObjectType.METER_VALVE_SWITCH_POSITION),
    /** DSMR V3.0 Generic meter */
    GENERIC_V3_0(DSMRMeterKind.GENERIC, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.GENMETER_VALUE_V3, CosemObjectType.METER_VALVE_SWITCH_POSITION),

    /** DSMR V4.0 Electricity meter */
    ELECTRICITY_V4_0(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, new CosemObjectType[] { CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_TRESHOLD_KWH, CosemObjectType.EMETER_SWITCH_POSITION, CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION, CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES, CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L1 }, new CosemObjectType[] { CosemObjectType.EMETER_VOLTAGE_SAGS_L2, CosemObjectType.EMETER_VOLTAGE_SAGS_L3, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2, CosemObjectType.EMETER_VOLTAGE_SWELLS_L3 }),
    /** DSMR V4 m3 meter (gas, water) */
    M3_V4(DSMRMeterKind.M3, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.M3METER_VALUE, CosemObjectType.METER_VALVE_SWITCH_POSITION),
    /** DSMR V4 GJ meter (heating, cooling) */
    GJ_V4(DSMRMeterKind.GJ, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.GJMETER_VALUE_V4, CosemObjectType.METER_VALVE_SWITCH_POSITION),
    /** DSMR V4 Slave Electricity meter */
    SLAVE_ELECTRICITY_V4(DSMRMeterKind.SLAVE_ELECTRICITY1, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_VALUE, CosemObjectType.EMETER_SWITCH_POSITION),

    /** DSMR V4.0.4 Electricity meter */
    ELECTRICITY_V4_0_4(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, new CosemObjectType[] { CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_TRESHOLD_KWH, CosemObjectType.EMETER_SWITCH_POSITION, CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION, CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES, CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L1, CosemObjectType.EMETER_INSTANT_CURRENT_L1, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L1, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L1 }, new CosemObjectType[] { CosemObjectType.EMETER_VOLTAGE_SAGS_L2, CosemObjectType.EMETER_VOLTAGE_SAGS_L3, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2, CosemObjectType.EMETER_VOLTAGE_SWELLS_L3, CosemObjectType.EMETER_INSTANT_CURRENT_L2, CosemObjectType.EMETER_INSTANT_CURRENT_L3, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L2, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L2, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L3 }),

    /** DSMR V4.2 Electricity meter (specification not available, implemented by reverse engineering */
    ELECTRICITY_V4_2(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, new CosemObjectType[] { CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION, CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES, CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L1, CosemObjectType.EMETER_INSTANT_CURRENT_L1, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L1, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L1 }, new CosemObjectType[] { CosemObjectType.EMETER_VOLTAGE_SAGS_L2, CosemObjectType.EMETER_VOLTAGE_SAGS_L3, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2, CosemObjectType.EMETER_VOLTAGE_SWELLS_L3, CosemObjectType.EMETER_INSTANT_CURRENT_L2, CosemObjectType.EMETER_INSTANT_CURRENT_L3, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L2, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L2, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L3 }),

    /** DSMR V5.0 Electricity meter */
    ELECTRICITY_V5_0(DSMRMeterKind.MAIN_ELECTRICITY, CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, new CosemObjectType[] { CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION, CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES, CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L1, CosemObjectType.EMETER_VOLTAGE_SWELLS_L1, CosemObjectType.EMETER_INSTANT_CURRENT_L1, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L1, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L1, CosemObjectType.EMETER_INSTANT_VOLTAGE_L1 }, new CosemObjectType[] { CosemObjectType.EMETER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_DELIVERY_TARIFF1, CosemObjectType.EMETER_DELIVERY_TARIFF2, CosemObjectType.EMETER_PRODUCTION_TARIFF1, CosemObjectType.EMETER_PRODUCTION_TARIFF2, CosemObjectType.EMETER_TARIFF_INDICATOR, CosemObjectType.EMETER_ACTUAL_DELIVERY, CosemObjectType.EMETER_ACTUAL_PRODUCTION, CosemObjectType.EMETER_POWER_FAILURES, CosemObjectType.EMETER_LONG_POWER_FAILURES, CosemObjectType.EMETER_POWER_FAILURE_LOG, CosemObjectType.EMETER_VOLTAGE_SAGS_L2, CosemObjectType.EMETER_VOLTAGE_SAGS_L3, CosemObjectType.EMETER_VOLTAGE_SWELLS_L2, CosemObjectType.EMETER_VOLTAGE_SWELLS_L3, CosemObjectType.EMETER_INSTANT_CURRENT_L2, CosemObjectType.EMETER_INSTANT_CURRENT_L3, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L2, CosemObjectType.EMETER_INSTANT_POWER_DELIVERY_L3, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L2, CosemObjectType.EMETER_INSTANT_POWER_PRODUCTION_L3, CosemObjectType.EMETER_INSTANT_VOLTAGE_L2, CosemObjectType.EMETER_INSTANT_VOLTAGE_L3 }),
    /** DSMR V5.0 m3 meter (gas, water) */
    M3_V5_0(DSMRMeterKind.M3, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.M3METER_VALUE),
    /** DSMR V5.0 GJ meter (heating, cooling) */
    GJ_V5_0(DSMRMeterKind.GJ, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.GJMETER_VALUE_V4),
    /** DSMR V5.0 Slave Electricity meter */
    SLAVE_ELECTRICITY_V5_0(DSMRMeterKind.SLAVE_ELECTRICITY1, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.METER_DEVICE_TYPE, CosemObjectType.METER_EQUIPMENT_IDENTIFIER, CosemObjectType.EMETER_VALUE);

    public static final Set<ThingTypeUID> METER_THING_TYPES;
    static {
        METER_THING_TYPES = new HashSet<ThingTypeUID>();
        for (DSMRMeterType meterType : DSMRMeterType.values()) {
            METER_THING_TYPES.add(meterType.getThingTypeUID());
        }
    }

    private final Logger logger = LoggerFactory.getLogger(DSMRMeterType.class);

    /** Meter kind */
    public final DSMRMeterKind meterKind;

    /** Required objects for this meter type */
    public final CosemObjectType[] requiredCosemObjects;

    /** Additional object this meter type can receive */
    public final CosemObjectType[] optionalCosemObjects;

    /** All objects this meter type can receive (convenience for {requiredCosemObjects, optionalCosemObjects}) */
    public final CosemObjectType[] supportedCosemObjects;

    /** Which CosemObjectType is used to identify this meter */
    public final CosemObjectType cosemObjectTypeMeterId;

    /**
     * Creates a new enum
     *
     * @param channelKey
     *            String containing the channel configuration for this meter
     */
    private DSMRMeterType(DSMRMeterKind meterKind, CosemObjectType cosemObjectTypeMeterId,
            CosemObjectType... requiredCosemObjects) {
        this(meterKind, cosemObjectTypeMeterId, requiredCosemObjects, new CosemObjectType[0]);
    }

    /**
     * Creates a new enum
     *
     * @param channelKey
     *            String containing the channel configuration for this meter
     */
    private DSMRMeterType(DSMRMeterKind meterKind, CosemObjectType cosemObjectTypeMeterId,
            CosemObjectType[] requiredCosemObjects, CosemObjectType[] optionalCosemObjects) {
        this.meterKind = meterKind;
        this.requiredCosemObjects = requiredCosemObjects;
        this.optionalCosemObjects = optionalCosemObjects;
        this.cosemObjectTypeMeterId = cosemObjectTypeMeterId;

        supportedCosemObjects = new CosemObjectType[requiredCosemObjects.length + optionalCosemObjects.length];
        System.arraycopy(requiredCosemObjects, 0, supportedCosemObjects, 0, requiredCosemObjects.length);
        System.arraycopy(optionalCosemObjects, 0, supportedCosemObjects, requiredCosemObjects.length,
                optionalCosemObjects.length);
    }

    /**
     * Returns if this DSMRMeterType is compatible for the Cosem Objects.
     *
     * If successful the real OBIS identification message (including the actual channel and identification value)
     * is returned
     * If the meter is compatible but the meter type has no identification message, a message is created using the
     * UNKNOWN OBISMsgType and no value
     * If the meter is not compatible, null is returned
     *
     *
     * @param availableCosemObjects the Cosem Objects to detect if the current meter compatible
     * @return {@link DSMRMeterIdentification} containing the identification of the compatible meter
     */
    public DSMRMeterDescriptor isCompatible(Map<CosemObjectType, CosemObject> availableCosemObjects) {
        DSMRMeterDescriptor meterDescriptor = null;
        for (CosemObjectType objectType : requiredCosemObjects) {
            if (!availableCosemObjects.containsKey(objectType)) {
                logger.debug("required objectType {} not found", objectType);
                return null;
            }
            CosemObject cosemObject = availableCosemObjects.get(objectType);

            // Checking by reference is possible here due to comparing enums
            if (cosemObjectTypeMeterId != CosemObjectType.UNKNOWN && objectType == cosemObjectTypeMeterId) {
                // We expect here the identification object has 1 CosemString as value (hence index 0)
                CosemValue<? extends Object> cosemValue = cosemObject.getCosemValue(0);
                String meterID = null;

                if (cosemValue != null) {
                    if (cosemValue instanceof CosemString) {
                        meterID = ((CosemString) cosemValue).getValue();
                    } else if (cosemValue instanceof CosemHexString) {
                        meterID = ((CosemHexString) cosemValue).getValue();
                    }
                }

                if (meterID != null) {
                    meterDescriptor = new DSMRMeterDescriptor(this, cosemObject.getObisIdentifier().getGroupB(),
                            meterID);
                } else {
                    // We expect that the Cosem Object type that identifies a meter type has a Cosem String value
                    logger.warn("Meter identification CosemObject {} is not a CosemString ignore identification");
                }
            }
        }
        // Meter type is compatible, check if an identification exists
        if (meterDescriptor == null) {
            logger.debug("Meter type {} has no identification", this.toString());
            meterDescriptor = new DSMRMeterDescriptor(this, DSMRMeterConstants.UNKNOWN_CHANNEL,
                    DSMRMeterConstants.UNKNOWN_ID);
        }
        logger.debug("Meter type is compatible and has the following descriptor {}", meterDescriptor);

        return meterDescriptor;
    }

    /**
     * Returns the ThingTypeUID for this meterType
     *
     * @return {@link ThingTypeUID} containing the unique identifier for this meter type
     */
    public ThingTypeUID getThingTypeUID() {
        return new ThingTypeUID(DSMRBindingConstants.BINDING_ID, name().toLowerCase());
    }
}
