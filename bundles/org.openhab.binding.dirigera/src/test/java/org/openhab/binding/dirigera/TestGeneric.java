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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;

/**
 * {@link TestGeneric} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestGeneric {

    void testJsonChannel() {
        String deviceInspelning = FileReader.readFileInString("src/test/resources/devices/inspelning.json");
        JSONObject inspelningObject = new JSONObject(deviceInspelning);
        final JSONObject newObject = new JSONObject();
        inspelningObject.keySet().forEach(key -> {
            newObject.put(key, inspelningObject.getString(key));
        });
    }
}
