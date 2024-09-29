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
 * Class for WSFloatingPointValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSFloatingPointValue extends WSResourceValue {

    public final double maximumValue;
    public final double minimumValue;
    public final double value;

    public WSFloatingPointValue(int resourceID, double value, double minimumValue, double maximumValue) {
        super(resourceID);
        this.value = value;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, value=%.2f, min=%.2f, max=%.2f]", super.resourceID, value, minimumValue,
                maximumValue);
    }
}
