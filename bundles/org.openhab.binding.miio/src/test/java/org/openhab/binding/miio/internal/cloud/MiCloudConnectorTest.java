/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link MiCloudConnector}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiCloudConnectorTest {

    @Test
    public void testMapUrlRequestData() {
        assertEquals("{\"model\":\"roborock.vacuum.a08\",\"obj_name\":\"roboroommap/1234567/2\"}",
                MiCloudConnector.buildMapUrlRequestData("roboroommap%2F1234567%2F2", "roborock.vacuum.a08"));
        assertEquals("{\"obj_name\":\"robomap/1234567/0\"}",
                MiCloudConnector.buildMapUrlRequestData("robomap/1234567/0", ""));
    }
}
