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
package org.openhab.binding.senechome.internal;

/**
 * The {@link PowerLimitationStatus} class is used as a POJO to
 * in-memory persist the last limitation state change.
 *
 * @author Steven.Schwarznau - Initial contribution
 *
 */
public class PowerLimitationStatus {

    private boolean state = false;

    private long time = 0L;

    public PowerLimitationStatus(boolean state, long time) {
        this.setState(state);
        this.setTime(time);
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
