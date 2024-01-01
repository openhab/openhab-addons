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
package org.openhab.binding.surepetcare.internal.dto;

import java.time.LocalTime;

/**
 * The {@link SurePetcareDeviceCurfew} class is used to serialize a curfew.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDeviceCurfew {

    public boolean enabled;
    public LocalTime lockTime;
    public LocalTime unlockTime;

    public SurePetcareDeviceCurfew() {
        this.enabled = false;
        this.lockTime = LocalTime.MIDNIGHT;
        this.unlockTime = LocalTime.MIDNIGHT;
    }

    public SurePetcareDeviceCurfew(boolean enabled, LocalTime lockTime, LocalTime unlockTime) {
        this.enabled = enabled;
        this.lockTime = lockTime;
        this.unlockTime = unlockTime;
    }

    @Override
    public String toString() {
        return enabled + "," + lockTime + "," + unlockTime;
    }
}
