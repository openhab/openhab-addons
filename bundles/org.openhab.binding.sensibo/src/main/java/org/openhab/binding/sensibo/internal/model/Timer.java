/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sensibo.internal.model;

/**
 * The {@link Timer} represents a Sensibo Sky unit timer definition
 *
 * @author Arne Seime - Initial contribution
 */
public class Timer {

    public int secondsRemaining;
    public AcState acState;
    public boolean enabled;

    public Timer(org.openhab.binding.sensibo.internal.dto.poddetails.Timer dto) {
        this.secondsRemaining = dto.targetTimeSecondsFromNow;
        this.acState = new AcState(dto.acState);
        this.enabled = dto.enabled;
    }
}
