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
package org.openhab.binding.revogismartstripcontrol.internal.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * {@link Status} is the internal data model used to control Revogi's SmartStrip
 *
 * @author Andi Bräu - Initial contribution
 *
 */
public class Status {
    @JsonProperty("switch")
    private List<Integer> switchValue;
    private List<Integer> watt;
    private List<Integer> amp;

    public Status() {
    }

    public Status(List<Integer> switchValue, List<Integer> watt, List<Integer> amp) {
        this.switchValue = switchValue;
        this.watt = watt;
        this.amp = amp;
    }

    public List<Integer> getSwitchValue() {
        return switchValue;
    }

    public List<Integer> getWatt() {
        return watt;
    }

    public List<Integer> getAmp() {
        return amp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return Objects.equals(switchValue, status.switchValue) &&
                Objects.equals(watt, status.watt) &&
                Objects.equals(amp, status.amp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(switchValue, watt, amp);
    }
}
