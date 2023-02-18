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
package org.openhab.binding.rfxcom.internal.config;

/**
 * Configuration class for RFXComBaseConnector device.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComBridgeConfiguration {
    public static final String SERIAL_PORT = "serialPort";
    public static final String BRIDGE_ID = "bridgeId";

    // Serial port for manual configuration
    public String serialPort;

    // Configuration for discovered bridge devices
    public String bridgeId;

    // Host for using RFXCOM over TCP/IP
    public String host;

    // Port for using RFXCOM over TCP/IP
    public int port;

    public String transceiverType;

    // Prevent unknown devices from being added to the inbox
    public boolean disableDiscovery;

    public int transmitPower;

    // Won't configure protocols to RFXCOM transceiver
    public boolean ignoreConfig;

    public String setMode;

    // Enabled protocols
    public boolean enableUndecoded;
    public boolean enableImagintronixOpus;
    public boolean enableByronSX;
    public boolean enableRSL;
    public boolean enableLighting4;
    public boolean enableFineOffsetViking;
    public boolean enableRubicson;
    public boolean enableAEBlyss;
    public boolean enableBlindsT1T2T3T4;
    public boolean enableBlindsT0;
    public boolean enableProGuard;
    public boolean enableFS20;
    public boolean enableLaCrosse;
    public boolean enableHidekiUPM;
    public boolean enableADLightwaveRF;
    public boolean enableMertik;
    public boolean enableVisonic;
    public boolean enableATI;
    public boolean enableOregonScientific;
    public boolean enableMeiantech;
    public boolean enableHomeEasyEU;
    public boolean enableAC;
    public boolean enableARC;
    public boolean enableX10;
    public boolean enableHomeConfort;
    public boolean enableKEELOQ;
}
