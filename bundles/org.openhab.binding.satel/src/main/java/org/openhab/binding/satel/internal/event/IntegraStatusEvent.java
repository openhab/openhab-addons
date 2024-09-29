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
package org.openhab.binding.satel.internal.event;

import java.time.LocalDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event class describing basic status bits and current time.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class IntegraStatusEvent implements SatelEvent {

    private Optional<LocalDateTime> integraTime;
    private boolean serviceMode;
    private boolean troubles;
    private boolean acu100Present;
    private boolean intRxPresent;
    private boolean troublesMemory;
    private boolean grade23Set;
    private byte integraType;

    /**
     * Constructs new event.
     *
     * @param integraTime current Integra date and time
     * @param statusByte1 status bits, byte #1
     * @param statusByte2 status bits, byte #2
     */
    public IntegraStatusEvent(Optional<LocalDateTime> integraTime, byte statusByte1, byte statusByte2) {
        this.integraTime = integraTime;
        this.serviceMode = (statusByte1 & 0x80) != 0;
        this.troubles = (statusByte1 & 0x40) != 0;
        this.acu100Present = (statusByte2 & 0x80) != 0;
        this.intRxPresent = (statusByte2 & 0x40) != 0;
        this.troublesMemory = (statusByte2 & 0x20) != 0;
        this.grade23Set = (statusByte2 & 0x10) != 0;
        this.integraType = (byte) (statusByte2 & 0x0f);
    }

    /**
     * @return current date and time on connected Integra or <code>Optional.empty()</code> if date/time set in the
     *         system is incorrect
     */
    public Optional<LocalDateTime> getIntegraTime() {
        return integraTime;
    }

    /**
     * @return <code>true</code> if service mode is enabled
     */
    public boolean inServiceMode() {
        return serviceMode;
    }

    /**
     * @return <code>true</code> if there are troubles in the system
     */
    public boolean troublesPresent() {
        return troubles;
    }

    /**
     * @return <code>true</code> if the are troubles in the memory
     */
    public boolean troublesMemory() {
        return troublesMemory;
    }

    /**
     * @return <code>true</code> if ACU-100 module is present in the system
     */
    public boolean isAcu100Present() {
        return acu100Present;
    }

    /**
     * @return <code>true</code> if INT-RX module is present in the system
     */
    public boolean isIntRxPresent() {
        return intRxPresent;
    }

    /**
     * @return <code>true</code> if Grade 2 or Grade 3 is enabled in Integra configuration
     */
    public boolean isGrade23Set() {
        return grade23Set;
    }

    /**
     * @return Integra board type
     */
    public int getIntegraType() {
        return integraType;
    }

    @Override
    public String toString() {
        return String.format(
                "IntegraStatusEvent: type = %d, time = %s, service mode = %b, troubles = %b, troubles memory = %b, ACU-100 = %b, INT-RX = %b, grade 2/3 = %b",
                this.integraType, this.integraTime.map(LocalDateTime::toString).orElse("N/A"), this.serviceMode,
                this.troubles, this.troublesMemory, this.acu100Present, this.intRxPresent, this.grade23Set);
    }
}
