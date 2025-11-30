/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ModbusSungrowBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class ModbusSungrowBindingConstants {

    /**
     * ThingType-ID for Inverter.
     */
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(ModbusBindingConstants.BINDING_ID,
            "sungrow-inverter");

    public static final String PROP_KEY_SERIAL_NUMBER = "SerialNumber";
    public static final String PROP_KEY_DEVICE_TYPE = "DeviceType";
    public static final String PROP_KEY_NOMINAL_OUTPUT_POWER = "NominalOutputPower";
    public static final String PROP_KEY_OUTPUT_TYPE = "OutputType";
    public static final String PROP_KEY_PROTOCOL_NUMBER = "ProtocolNumber";
    public static final String PROP_KEY_PROTOCOL_VERSION = "ProtocolVersion";
    public static final String PROP_KEY_ARM_CERT_VERSION_NUMBER = "ArmCertificationVersionNumber";
    public static final String PROP_KEY_DSP_CERT_VERSION_NUMBER = "DspCertificationVersionNumber";
}
