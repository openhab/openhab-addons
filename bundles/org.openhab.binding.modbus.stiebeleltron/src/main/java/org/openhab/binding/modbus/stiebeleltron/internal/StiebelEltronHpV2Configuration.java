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
package org.openhab.binding.modbus.stiebeleltron.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link StiebelEltronHpV2Configuration} class contains configuration parameters for Stiebel Eltron Heat Pump V2
 * @author Thomas Burri - Initial contribution
 * @author Thomas Burri - Added additional configuration items for new WPM compatible heat pumps
 */
@NonNullByDefault
public class StiebelEltronHpV2Configuration extends StiebelEltronConfiguration {
    public static final int WPM3 = 390;
    public static final int WPM3I = 391;
    public static final int WPMSYSTEM = 449;

    /**
     * Length of the state block in registers to read from bus (for future use)
     */
    private int stateBlockLength = 7;

    /**
     * Flag to enable execution of supported reset commands
     */
    private boolean allowResetCmds = false;

    /**
     * Number of heat pumps in a WPMsystem or WPM3 based environment.
     */
    private int heatpumpCount = 0;

    /**
     * Default WPM controller id (WPM3 = 390, WPM3I = 391, WPMsystem = 449)
     */
    private int wpmControllerId = WPM3;

    /**
     * Flag to enable polling of the SG Ready registers
     */
    private boolean pollSgReady = false;

    /**
     * Gets the length of the state block
     */
    public int getStateBlockLength() {
        return stateBlockLength;
    }

    /**
     * Sets the state block length
     */
    public void setStateBlockLength(int stateBlockLength) {
        this.stateBlockLength = stateBlockLength;
    }

    /**
     * Gets the flag if execution of reset commands is enabled
     */
    public boolean getAllowResetCmdsFlag() {
        return allowResetCmds;
    }

    /**
     * Sets the flag if execution of reset commands is enabled
     */
    public void setAllowResetCmdsFlag(boolean allowResetCmds) {
        this.allowResetCmds = allowResetCmds;
    }

    /**
     * Gets the number of heat pumps (only used with WMPsytem and WPM3 heat pumps)
     */
    public int getHeatpumpCount() {
        return heatpumpCount;
    }

    /**
     * Sets the number of heat pumps (only used with WMPsytem and WPM3 heat pumps)
     */
    public void setSetHeatpumpCount(int heatpumpCount) {
        this.heatpumpCount = heatpumpCount;
    }

    /**
     * Gets the WMP controller id
     */
    public int getWpmControllerId() {
        return wpmControllerId;
    }

    /**
     * Sets the WMP controller id
     */
    public void setWpmControllerId(int wpmControllerId) {
        this.wpmControllerId = wpmControllerId;
    }

    /**
     * Gets the flag if polling SG Ready registers is enabled
     */
    public boolean getPollSgReadyFlag() {
        return pollSgReady;
    }

    /**
     * Sets the flag if polling SG Ready registers is enabled
     */
    public void setPollSgReadyFlag(boolean pollSgReady) {
        this.pollSgReady = pollSgReady;
    }
}
