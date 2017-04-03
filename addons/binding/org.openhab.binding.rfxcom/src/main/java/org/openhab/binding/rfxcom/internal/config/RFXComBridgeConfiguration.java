/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.config;

/**
 * Configuration class for {@link RfxcomBinding} device.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComBridgeConfiguration {

    // Serial port for manual configuration
    public String serialPort;

    // Configuration for discovered bridge devices
    public String bridgeId;

    // Host for using RFXCOM over TCP/IP
    public String host;

    // Port for using RFXCOM over TCP/IP
    public int port;

    public String transceiverType;

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
}