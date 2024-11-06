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
package org.openhab.binding.dirigera.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * {@link TestGeneric} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestGeneric {

    @Test
    void testJsonChannel() {
        String error = String
                .format("{\"http-error-flag\":true,\"http-error-status\":%s,\"http-error-message\":\"%s\"}", "5", null);
        System.out.println(error);
        System.out.println(new JSONObject(error));
    }
}
