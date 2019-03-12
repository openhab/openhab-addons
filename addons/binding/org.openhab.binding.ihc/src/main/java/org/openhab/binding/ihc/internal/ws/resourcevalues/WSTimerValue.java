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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSTimerValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSTimerValue extends WSResourceValue {

    protected long milliseconds;

    public WSTimerValue() {
    }

    public WSTimerValue(int resourceID) {
        super(resourceID);
    }

    public WSTimerValue(int resourceID, long milliseconds) {
        super(resourceID);
        this.milliseconds = milliseconds;
    }

    /**
     * Gets the value of the milliseconds property.
     *
     */
    public long getMilliseconds() {
        return milliseconds;
    }

    /**
     * Sets the value of the milliseconds property.
     *
     */
    public void setMilliseconds(long value) {
        this.milliseconds = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, milliseconds=%b]", super.resourceID, milliseconds);
    }
}
