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
package org.openhab.binding.freeboxos.internal.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.freeboxos.internal.api.login.OpenSessionData;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenSessionDataTest {

    @Test
    public void hmacSha1Test() throws Exception {
        String expected = "25dad1bb5604321f12b755cc9d755d1480cf7989";
        OpenSessionData openSessionData = new OpenSessionData("foo", "Token1234", "Challenge");
        String actual = openSessionData.getPassword();
        assertEquals(expected, actual);
    }
}
