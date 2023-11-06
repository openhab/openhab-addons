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
package org.openhab.binding.sensibo.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openhab.binding.sensibo.internal.dto.setacstateproperty.SetAcStatePropertyReponse;

/**
 * @author Arne Seime - Initial contribution
 */
public class SetAcStatePropertyResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserialize() throws IOException {
        final SetAcStatePropertyReponse rsp = wireHelper.deSerializeResponse("/set_acstate_response.json",
                SetAcStatePropertyReponse.class);

        assertNotNull(rsp.acState);
        assertTrue(rsp.acState.on);
    }
}
