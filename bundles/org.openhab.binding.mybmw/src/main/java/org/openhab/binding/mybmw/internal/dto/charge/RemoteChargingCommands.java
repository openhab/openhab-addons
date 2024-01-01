/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.dto.charge;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * derived from the API responses
 * 
 * @author Martin Grassl - initial contribution
 */
public class RemoteChargingCommands {
    private List<String> chargingControl = new ArrayList<>();
    private List<String> flapControl = new ArrayList<>();
    private List<String> plugControl = new ArrayList<>();

    /**
     * @return the chargingControl
     */
    public List<String> getChargingControl() {
        return chargingControl;
    }

    /**
     * @param chargingControl the chargingControl to set
     */
    public void setChargingControl(List<String> chargingControl) {
        this.chargingControl = chargingControl;
    }

    /**
     * @return the flapControl
     */
    public List<String> getFlapControl() {
        return flapControl;
    }

    /**
     * @param flapControl the flapControl to set
     */
    public void setFlapControl(List<String> flapControl) {
        this.flapControl = flapControl;
    }

    /**
     * @return the plugControl
     */
    public List<String> getPlugControl() {
        return plugControl;
    }

    /**
     * @param plugControl the plugControl to set
     */
    public void setPlugControl(List<String> plugControl) {
        this.plugControl = plugControl;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "RemoteChargingCommands [chargingControl=" + chargingControl + ", flapControl=" + flapControl
                + ", plugControl=" + plugControl + "]";
    }
}
