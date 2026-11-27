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
package org.openhab.binding.ipcamera.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests XML parsing safeguards used by the IP camera binding.
 *
 * @author Robert D. - Initial contribution
 */
class HelperTest {
    @Test
    void parsesWellFormedXml() {
        assertDoesNotThrow(() -> Helper.loadXMLFromString("<event><value>motion</value></event>"));
    }

    @Test
    void rejectsDocumentsWithDoctypeDeclarations() {
        String xml = """
                <!DOCTYPE event [<!ENTITY external SYSTEM "file:///etc/passwd">]>
                <event>&external;</event>
                """;

        assertThrows(Exception.class, () -> Helper.loadXMLFromString(xml));
    }
}
