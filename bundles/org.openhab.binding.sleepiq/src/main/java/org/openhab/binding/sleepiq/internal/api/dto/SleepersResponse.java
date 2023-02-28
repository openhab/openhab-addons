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
package org.openhab.binding.sleepiq.internal.api.dto;

import java.util.List;

/**
 * The {@link SleeperResponse} holds the information about the sleepers assigned to the bed sides.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class SleepersResponse {
    private List<Sleeper> sleepers;

    public List<Sleeper> getSleepers() {
        return sleepers;
    }

    public void setSleepers(List<Sleeper> sleepers) {
        this.sleepers = sleepers;
    }

    public SleepersResponse withSleepers(List<Sleeper> sleepers) {
        setSleepers(sleepers);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sleepers == null) ? 0 : sleepers.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SleepersResponse)) {
            return false;
        }
        SleepersResponse other = (SleepersResponse) obj;
        if (sleepers == null) {
            if (other.sleepers != null) {
                return false;
            }
        } else if (!sleepers.equals(other.sleepers)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepersResponse [sleepers=");
        builder.append(sleepers);
        builder.append("]");
        return builder.toString();
    }
}
