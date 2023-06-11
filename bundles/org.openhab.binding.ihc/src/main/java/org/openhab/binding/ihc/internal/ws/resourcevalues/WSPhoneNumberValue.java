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
 * Class for WSPhoneNumberValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSPhoneNumberValue extends WSResourceValue {

    public final String number;

    public WSPhoneNumberValue(int resourceID, String number) {
        super(resourceID);
        this.number = number;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, number=%s]", super.resourceID, number);
    }
}
