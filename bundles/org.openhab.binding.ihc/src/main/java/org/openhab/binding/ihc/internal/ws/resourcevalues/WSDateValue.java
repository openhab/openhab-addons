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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSDateValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSDateValue extends WSResourceValue {

    public final short year;
    public final byte month;
    public final byte day;

    public WSDateValue(int resourceID, short year, byte month, byte day) {
        super(resourceID);
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, year=%d, month=%d, day=%d]", super.resourceID, year, month, day);
    }
}
