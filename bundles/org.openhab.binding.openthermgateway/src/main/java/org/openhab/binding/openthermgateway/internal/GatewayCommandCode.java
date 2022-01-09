/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GatewayCommandCode} provides a set of supported OpenTherm Gateway commands.
 * 
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class GatewayCommandCode {
    public static final String TemperatureTemporary = "TT";
    public static final String TemperatureConstant = "TC";
    public static final String TemperatureOutside = "OT";
    public static final String SetClock = "ST";
    public static final String HotWater = "HW";
    public static final String PrintReport = "PR";
    public static final String PrintSummary = "PS";
    public static final String GateWay = "GW";
    public static final String LedA = "LA";
    public static final String LedB = "LB";
    public static final String LedC = "LC";
    public static final String LedD = "LD";
    public static final String LedE = "LE";
    public static final String LedF = "LF";
    public static final String GpioA = "GA";
    public static final String GpioB = "GB";
    public static final String SetBack = "SB";
    public static final String TemperatureSensor = "TS";
    public static final String AddAlternative = "AA";
    public static final String DeleteAlternative = "DA";
    public static final String UnknownID = "UI";
    public static final String KnownID = "KI";
    public static final String PriorityMessage = "PM";
    public static final String SetResponse = "SR";
    public static final String ClearResponse = "CR";
    public static final String SetpointHeating = "SH";
    public static final String SetpointWater = "SW";
    public static final String MaximumModulation = "MM";
    public static final String ControlSetpoint = "CS";
    public static final String ControlSetpoint2 = "C2";
    public static final String CentralHeating = "CH";
    public static final String CentralHeating2 = "H2";
    public static final String VentilationSetpoint = "VS";
    public static final String Reset = "RS";
    public static final String IgnoreTransition = "IT";
    public static final String OverrideHighbyte = "OH";
    public static final String ForceThermostat = "FT";
    public static final String VoltageReference = "VR";
    public static final String DebugPointer = "DP";
}
