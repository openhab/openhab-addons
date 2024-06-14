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
package org.openhab.binding.emotiva.internal.protocol;

/**
 * The class {@link EmotivaUdpResponse} represents UDP response we expect.
 *
 * @author Andi Bräu - Initial contribution
 * @author Espen Fossen - Adpated to Emotiva binding
 */
public record EmotivaUdpResponse(String answer, String ipAddress) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmotivaUdpResponse that = (EmotivaUdpResponse) o;
        return answer.equals(that.answer) && ipAddress.equals(that.ipAddress);
    }
}
