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
package org.openhab.binding.saicismart.internal.asn1;

import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Markus Heberling - Initial contribution
 */
public final class Util {
    private Util() {
    }

    public static void fillReserved(byte[] reservedBytes) {
        System.arraycopy((ThreadLocalRandom.current().nextLong() + "1111111111111111").getBytes(), 0, reservedBytes, 0,
                16);
    }
}
