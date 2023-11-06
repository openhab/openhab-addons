/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.revogi.internal.api;

import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * {@link StatusDTO} is the internal data model used to control Revogi's SmartStrip
 *
 * @author Andi Bräu - Initial contribution
 *
 */
public class StatusDTO {
    private final boolean online;
    private final int responseCode;
    @SerializedName("switch")
    private final List<Integer> switchValue;
    private final List<Integer> watt;
    private final List<Integer> amp;

    public StatusDTO() {
        online = false;
        responseCode = 0;
        switchValue = null;
        watt = null;
        amp = null;
    }

    public StatusDTO(boolean online, int responseCode, List<Integer> switchValue, List<Integer> watt,
            List<Integer> amp) {
        this.online = online;
        this.responseCode = responseCode;
        this.switchValue = switchValue;
        this.watt = watt;
        this.amp = amp;
    }

    public boolean isOnline() {
        return online;
    }

    public int getResponseCode() {
        return responseCode;
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatusDTO status = (StatusDTO) o;
        return online == status.online && responseCode == status.responseCode
                && Objects.equals(switchValue, status.switchValue) && Objects.equals(watt, status.watt)
                && Objects.equals(amp, status.amp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(online, responseCode, switchValue, watt, amp);
    }
}
