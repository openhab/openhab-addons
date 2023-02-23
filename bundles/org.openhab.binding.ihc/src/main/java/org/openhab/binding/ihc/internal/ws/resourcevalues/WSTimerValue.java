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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSTimerValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSTimerValue extends WSResourceValue {

    public final long milliseconds;

    public WSTimerValue(int resourceID, long milliseconds) {
        super(resourceID);
        this.milliseconds = milliseconds;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, milliseconds=%b]", super.resourceID, milliseconds);
    }
}
