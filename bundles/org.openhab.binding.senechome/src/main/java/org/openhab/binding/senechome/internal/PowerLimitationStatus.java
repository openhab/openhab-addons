/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
