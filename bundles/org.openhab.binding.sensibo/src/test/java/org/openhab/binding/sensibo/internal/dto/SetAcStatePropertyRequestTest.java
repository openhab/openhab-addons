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
package org.openhab.binding.sensibo.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openhab.binding.sensibo.internal.dto.setacstateproperty.SetAcStatePropertyRequest;

/**
 * @author Arne Seime - Initial contribution
 */
public class SetAcStatePropertyRequestTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testSerializeDeserialize() throws IOException {
        SetAcStatePropertyRequest req = new SetAcStatePropertyRequest("PODID", "targetTemperature", "mode");
        String serializedJson = wireHelper.serialize(req);

        final SetAcStatePropertyRequest deSerializedRequest = wireHelper.deSerializeFromString(serializedJson,
                SetAcStatePropertyRequest.class);

        assertEquals("mode", deSerializedRequest.newValue);
    }
}
