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
package org.openhab.binding.sleepiq.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.test.AbstractTest;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationOutletRequest;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutlet;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutletOperation;
import org.openhab.binding.sleepiq.internal.api.impl.GsonGenerator;

import com.google.gson.Gson;

/**
 * The {@link SetFoundationOutletTest} tests serialization of the foundation outlet request.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class SetFoundationOutletTest extends AbstractTest {
    private static Gson gson = GsonGenerator.create(true);

    @Test
    public void testSerializeAllFields() throws Exception {
        FoundationOutletRequest preset = new FoundationOutletRequest()
                .withFoundationOutlet(FoundationOutlet.RIGHT_UNDER_BED_LIGHT)
                .withFoundationOutletOperation(FoundationOutletOperation.ON);
        assertEquals(readJson("set-foundation-outlet.json"), gson.toJson(preset));
    }
}
