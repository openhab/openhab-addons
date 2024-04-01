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
 * Class for WSIntegerValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSIntegerValue extends WSResourceValue {

    public final int value;
    public final int maximumValue;
    public final int minimumValue;

    public WSIntegerValue(int resourceID, int value, int minimumValue, int maximumValue) {
        super(resourceID);
        this.value = value;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, value=%d, min=%d, max=%d]", super.resourceID, value, minimumValue,
                maximumValue);
    }
}
