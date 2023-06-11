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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSTimeValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSTimeValue extends WSResourceValue {

    public final int hours;
    public final int minutes;
    public final int seconds;

    public WSTimeValue(int resourceID, int hours, int minutes, int seconds) {
        super(resourceID);
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, hours=%d, minutes=%d, seconds=%d]", super.resourceID, hours, minutes,
                seconds);
    }
}
