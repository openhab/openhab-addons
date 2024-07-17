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
package org.openhab.binding.siemenshvac.internal.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class UidUtilsTest {

    @Test
    public void testSanetizeId() throws Exception {
        assertEquals(UidUtils.sanetizeId("Début heure été"), "debut-heure-ete");
        assertEquals(UidUtils.sanetizeId("App.Ambiance 1"), "app-ambiance-1");
        assertEquals(UidUtils.sanetizeId("Appareil d'ambiance P"), "appareil-d-ambiance-p");
    }
}
