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

import static org.mockito.Mockito.mock;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;

/**
 * {@link TestModel} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
class TestModel {

    @Test
    void testCustomName() {
        String modelString = FileReader.readFileInString("src/test/resources/NewHome.json");
        JSONObject modelJson = new JSONObject(modelString);
        Model model = new Model(mock(Gateway.class), modelJson);
        System.out.println(
                "Custom name for light sensor " + model.getCustonNameFor("5ac5e131-44a4-4d75-be78-759a095d31fb_3"));
    }
}
