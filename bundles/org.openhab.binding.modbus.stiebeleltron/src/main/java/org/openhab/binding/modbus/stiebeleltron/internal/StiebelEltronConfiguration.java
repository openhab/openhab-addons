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
 * The {@link StiebelEltronConfiguration} class contains fields mapping
 * thing configuration parameters.
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Added additional configuration items for new WPM compatible heat pumps
 */
@NonNullByDefault
public class StiebelEltronConfiguration {
    public static final int WPM3 = 390;
    public static final int WPM3I = 391;
    public static final int WPMSYSTEM = 449;

    /**
     * Poll interval in seconds. Increase this if you encounter connection errors
     */
    private long refresh = 5;

    /**
     * Number of retries before giving up reading from this thing
     */
    private int maxTries = 3;

    /**
     * Length of the state block in registers to read from bus
     */
    private int stateBlockLength = 7;

    /**
     * Number of heat pumps in a WPMsystem or WPM3 based environment.
     */
    private int nrOfHps = 0;

    /**
     * Default WPM controller id (WPM3 = 390, WPM3I = 391, WPMsystem = 449)
     */
    private int wpmControllerId = WPM3;

    /**
     * Flag to enable polling of the SG Ready registers
     */
    private boolean pollSgReady = false;

    /**
     * Gets refresh period in milliseconds
     */
    public long getRefreshMillis() {
        return refresh * 1000;
    }

    /**
     * Gets the maximal retries number
     */
    public int getMaxTries() {
        return maxTries;
    }

    /**
     * Sets the maximal retries number
     */
    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

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
     * Gets the number of heat pumps (only used with WMPsytem and WPM3 heat pumps)
     */
    public int getNrOfHps() {
        return nrOfHps;
    }

    /**
     * Sets the number of heat pumps (only used with WMPsytem and WPM3 heat pumps)
     */
    public void setSetNrOfHps(int nrOfHps) {
        this.nrOfHps = nrOfHps;
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
