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
package org.openhab.io.semp.internal;

/**
 * SEMP consumers capabilities
 *
 * @author Markus Eckhardt - Initial Contribution
 *
 */
public class SEMPCapabilities {
    /*
     * Capability of the device with regard to deriving information about its current power consumption, e.g.
     * measurement or estimation
     */
    private String method;

    /*
     * Bool that indicates if the device is able to deal with absolute timestamps or only with relative timestamps
     */
    private Boolean absoluteTimestamps;

    /*
     * Specifies if a run of the device can be interrupted or not.
     */
    private Boolean interruptionsAllowed;

    /*
     * Specifies options related to planning requests.
     */
    private Boolean optionalEnergy;

    public SEMPCapabilities() {
    }

    /*
     * Setter for method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /*
     * Getter for method
     */
    public String getMethod() {
        return method;
    }

    /*
     * Checks if field method is set
     */
    public boolean isMethodSet() {
        if (method == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for absoluteTimestamps
     */
    public void setAbsoluteTimestamps(boolean absoluteTimestamps) {
        this.absoluteTimestamps = absoluteTimestamps;
    }

    /*
     * Getter for absoluteTimestamps
     */
    public boolean getAbsoluteTimestamps() {
        return absoluteTimestamps;
    }

    /*
     * Checks if field absoluteTimestamps is set
     */
    public boolean isAbsoluteTimestampsSet() {
        if (absoluteTimestamps == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for interruptionsAllowed
     */
    public void setInterruptionsAllowed(boolean interruptionsAllowed) {
        this.interruptionsAllowed = interruptionsAllowed;
    }

    /*
     * Getter for interruptionsAllowed
     */
    public boolean getInterruptionsAllowed() {
        return interruptionsAllowed;
    }

    /*
     * Checks if field interruptionsAllowed is set
     */
    public boolean isInterruptionsAllowedSet() {
        if (interruptionsAllowed == null) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Setter for optionalEnergy
     */
    public void setOptionalEnergy(boolean optionalEnergy) {
        this.optionalEnergy = optionalEnergy;
    }

    /*
     * Getter for optionalEnergy
     */
    public boolean getOptionalEnergy() {
        return optionalEnergy;
    }

    /*
     * Checks if field optionalEnergy is set
     */
    public boolean isOptionalEnergySet() {
        if (optionalEnergy == null) {
            return false;
        } else {
            return true;
        }
    }
}
