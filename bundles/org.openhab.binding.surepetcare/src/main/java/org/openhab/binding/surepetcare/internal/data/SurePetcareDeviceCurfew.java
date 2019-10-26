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
package org.openhab.binding.surepetcare.internal.data;

/**
 * The {@link SurePetcareDeviceCurfew} class is used to serialize a curfew.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDeviceCurfew {

    public Boolean enabled;
    public String lockTime;
    public String unlockTime;

    public SurePetcareDeviceCurfew() {
        this.enabled = false;
        this.lockTime = "00:00";
        this.unlockTime = "00:00";
    }

    public SurePetcareDeviceCurfew(boolean enabled, String lockTime, String unlockTime) {
        this.enabled = enabled;
        this.lockTime = lockTime;
        this.unlockTime = unlockTime;
    }

    @Override
    public String toString() {
        return enabled.toString() + "," + lockTime + "," + unlockTime;
    }

}
