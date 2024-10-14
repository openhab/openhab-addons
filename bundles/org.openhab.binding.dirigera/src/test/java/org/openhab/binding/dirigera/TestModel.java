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
package org.openhab.binding.dirigera;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.common.ThreadPoolManager;

/**
 * {@link TestModel} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
class TestModel {

    @Test
    void testCustomName() {
        Model model = new Model(mock(Gateway.class));
        String modelString = FileReader.readFileInString("src/test/resources/CustomNameHome.json");
        model.update(modelString);
        // test device with given custom name
        assertEquals("Floor Lamp", model.getCustonNameFor("891790db-8c17-483a-a1a6-c85bffd3a373_1"), "Floor Lamp name");
        // test device without custom name - take model name
        assertEquals("VALLHORN Wireless Motion Sensor",
                model.getCustonNameFor("5ac5e131-44a4-4d75-be78-759a095d31fb_3"), "Motion Sensor name");
        // test device without custom name and no model name
        assertEquals("light", model.getCustonNameFor("c27faa27-4c18-464f-81a0-a31ce57d83d5_1"), "Lamp");
    }

    @Test
    void testModelStress() {
        String mdoelString = FileReader.readFileInString("src/test/resources/NewHome.json");
        ExecutorService scheduler = ThreadPoolManager.getPool("ModelStressTest");
    }
}
