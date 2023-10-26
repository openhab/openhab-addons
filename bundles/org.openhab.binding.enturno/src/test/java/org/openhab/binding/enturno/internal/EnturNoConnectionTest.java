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
package org.openhab.binding.enturno.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.enturno.internal.connection.EnturNoConnection;

/**
 * The {@link EnturNoConnectionTest} class defines tests for the {@link EnturNoConnection} class
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class EnturNoConnectionTest {

    @Test
    public void getIsoDateTime_WithoutColonInZone() {
        assertEquals("2023-10-25T09:01:00+02:00", EnturNoConnection.getIsoDateTime("2023-10-25T09:01:00+0200"));
    }

    @Test
    public void getIsoDateTime_WithColonInZone() {
        assertEquals("2023-10-25T09:01:00+02:00", EnturNoConnection.getIsoDateTime("2023-10-25T09:01:00+02:00"));
    }

    @Test
    public void getIsoDateTime_WithoutZone() {
        assertEquals("2023-10-25T09:01:00+00:00", EnturNoConnection.getIsoDateTime("2023-10-25T09:01:00"));
    }
}
