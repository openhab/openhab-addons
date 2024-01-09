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
package org.openhab.binding.mybmw.internal.handler.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.util.FileReader;

/**
 * 
 * checks if the response anonymization is successful
 * 
 * @author Martin Grassl - initial contribution
 */
@NonNullByDefault
public class ResponseContentAnonymizerTest {
    @Test
    void testAnonymizeResponseContent() {
        String content = FileReader.fileToString("responses/vehicles.json");
        String anonymous = ResponseContentAnonymizer.anonymizeResponseContent(content);
        assertFalse(anonymous.contains("VIN1234567"), "VIN not deleted!");
        assertFalse(anonymous.contains("Testort"), "Location not deleted!");
    }

    @Test
    void testAnonymizeRandomString() {
        String content = "asdfiulsahföauifhnasdölfam,xöasiocjfsailfunsalifnsaölfkmasdäf.ifnvaskdfnvinlocationasdfiulsdanf";
        String anonymous = ResponseContentAnonymizer.anonymizeResponseContent(content);
        assertEquals(content, anonymous);
    }

    @Test
    void testAnonymizeEmptyString() {
        String content = "";
        String anonymous = ResponseContentAnonymizer.anonymizeResponseContent(content);
        assertEquals(content, anonymous);
    }

    @Test
    void testAnonymizeNullString() {
        String content = null;
        String anonymous = ResponseContentAnonymizer.anonymizeResponseContent(content);
        assertEquals("", anonymous);
    }
}
