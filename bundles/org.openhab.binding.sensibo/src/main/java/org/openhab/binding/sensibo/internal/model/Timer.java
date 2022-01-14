/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sensibo.internal.dto.poddetails.TimerDTO;

/**
 * The {@link Timer} represents a Sensibo Sky unit timer definition
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class Timer {
    public final int secondsRemaining;
    public final AcState acState;
    public final boolean enabled;

    public Timer(TimerDTO dto) {
        this.secondsRemaining = dto.targetTimeSecondsFromNow;
        this.acState = new AcState(dto.acState);
        this.enabled = dto.enabled;
    }
}
