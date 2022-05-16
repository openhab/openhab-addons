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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.mielecloud.internal.util.ResourceUtil.getResourceAsString;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ActionsCollectionTest {
    @Test
    public void canCreateActionsCollection() throws IOException {
        // given:
        String json = getResourceAsString(
                "/org/openhab/binding/mielecloud/internal/webservice/api/json/actionsCollection.json");

        // when:
        ActionsCollection collection = ActionsCollection.fromJson(json);

        // then:
        assertEquals(Collections.singleton("000123456789"), collection.getDeviceIdentifiers());
        Actions actions = collection.getActions("000123456789");

        assertEquals(List.of(ProcessAction.START, ProcessAction.STOP), actions.getProcessAction());
        assertEquals(Collections.singletonList(Light.DISABLE), actions.getLight());
        assertEquals(Optional.empty(), actions.getStartTime());
        assertEquals(Collections.singletonList(123), actions.getProgramId());
        assertEquals(Optional.of(true), actions.getPowerOn());
        assertEquals(Optional.of(false), actions.getPowerOff());
    }

    @Test
    public void creatingActionsCollectionFromInvalidJsonThrowsMieleSyntaxException() {
        // given:
        String invalidJson = "{\":{}}";

        // when:
        assertThrows(MieleSyntaxException.class, () -> {
            ActionsCollection.fromJson(invalidJson);
        });
    }

    @Test
    public void canCreateActionsCollectionWithLargeProgramID() throws IOException {
        // given:
        String json = "{\"mac-00124B000AE539D6\": {}}";

        // when:
        DeviceCollection collection = DeviceCollection.fromJson(json);

        // then:
        assertEquals(Collections.singleton("mac-00124B000AE539D6"), collection.getDeviceIdentifiers());
    }
}
