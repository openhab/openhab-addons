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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GatewayCommandCode} provides a set of supported OpenTherm Gateway commands.
 * 
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class GatewayCommandCode {
    public static final String TEMPERATURETEMPORARY = "TT";
    public static final String TEMPERATURECONSTANT = "TC";
    public static final String TEMPERATUREOUTSIDE = "OT";
    public static final String SETCLOCK = "SC";
    public static final String HOTWATER = "HW";
    public static final String PRINTREPORT = "PR";
    public static final String PRINTSUMMARY = "PS";
    public static final String GATEWAY = "GW";
    public static final String LEDA = "LA";
    public static final String LEDB = "LB";
    public static final String LEDC = "LC";
    public static final String LEDD = "LD";
    public static final String LEDE = "LE";
    public static final String LEDF = "LF";
    public static final String GPIOA = "GA";
    public static final String GPIOB = "GB";
    public static final String SETBACK = "SB";
    public static final String TEMPERATURESENSOR = "TS";
    public static final String ADDALTERNATIVE = "AA";
    public static final String DELETEALTERNATIVE = "DA";
    public static final String UNKNOWNID = "UI";
    public static final String KNOWNID = "KI";
    public static final String PRIORITYMESSAGE = "PM";
    public static final String SETRESPONSE = "SR";
    public static final String CLEARRESPONSE = "CR";
    public static final String SETPOINTHEATING = "SH";
    public static final String SETPOINTWATER = "SW";
    public static final String MAXIMUMMODULATION = "MM";
    public static final String CONTROLSETPOINT = "CS";
    public static final String CONTROLSETPOINT2 = "C2";
    public static final String CENTRALHEATING = "CH";
    public static final String CENTRALHEATING2 = "H2";
    public static final String VENTILATIONSETPOINT = "VS";
    public static final String RESET = "RS";
    public static final String IGNORETRANSITION = "IT";
    public static final String OVERRIDEHIGHBYTE = "OH";
    public static final String FORCETHERMOSTAT = "FT";
    public static final String VOLTAGEREFERENCE = "VR";
    public static final String DEBUGPOINTER = "DP";
}
