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
package org.openhab.binding.matter.internal.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MatterBridgeSettings} is the settings configuration for the Matter Bridge service.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterBridgeSettings {
    public boolean enableBridge = true;
    public String runningState = "Stopped";
    public String bridgeName = "openHAB";
    public int port = 5540;
    public int passcode = 20202021;
    public int discriminator = -1;
    public String qrCode = "";
    public String manualPairingCode = "";
    public boolean resetBridge = false;
    public boolean openCommissioningWindow = false;

    public String toString() {
        return "MatterBridgeSettings [name=" + bridgeName + ", port=" + port + ", passcode=" + passcode
                + ", discriminator=" + discriminator + ", qrCode=" + qrCode + ", manualPairingCode=" + manualPairingCode
                + ", resetBridge=" + resetBridge + ", openCommissioningWindow=" + openCommissioningWindow + "]";
    }
}
