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
package org.openhab.binding.sleepiq.internal.api.dto;

/**
 * The {@link PauseModeRequest} holds the request to pause the bed data collection.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class PauseModeRequest {
    private String mode;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public PauseModeRequest withMode(String mode) {
        setMode(mode);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
        if (!(obj instanceof PauseModeRequest)) {
            return false;
        }
        PauseModeRequest other = (PauseModeRequest) obj;
        if (mode == null) {
            if (other.mode != null) {
                return false;
            }
        } else if (!mode.equals(other.mode)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepNumberRequest [mode=");
        builder.append(mode);
        builder.append("]");
        return builder.toString();
    }
}
