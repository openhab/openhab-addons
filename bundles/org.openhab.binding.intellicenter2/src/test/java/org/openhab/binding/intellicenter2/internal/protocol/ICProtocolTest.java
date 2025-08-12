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
package org.openhab.binding.intellicenter2.internal.protocol;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.binding.intellicenter2.internal.IntelliCenter2Configuration;
import org.openhab.binding.intellicenter2.internal.model.Body;
import org.openhab.binding.intellicenter2.internal.model.GetHardwareDefinition;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
@Disabled
public class ICProtocolTest {

    @Nullable
    private static ICProtocol protocol;

    @BeforeAll
    public static void setup() throws Exception {
        var config = new IntelliCenter2Configuration();
        config.hostname = "192.168.1.148";
        protocol = new ICProtocol(config);
    }

    @Test
    public void testGetHardwareDefinition() throws Exception {
        var response = protocol.submit(GetHardwareDefinition.Argument.DEFAULT.getRequest()).get();
        var defn = new GetHardwareDefinition(response);

        var bodies = defn.getPanels().get(0).getBodies();
        assertEquals(2, bodies.size());

        var names = bodies.stream().map(Body::getObjectName).collect(toSet());

        assertTrue(names.contains("B1101"));
        assertTrue(names.contains("B1202"));
    }

    @Test
    @Disabled
    public void testNotifyListener() throws Exception {
        final List<ResponseObject> responses = new ArrayList<>();
        final var listener = new NotifyListListener() {
            @Override
            public void onNotifyList(ResponseObject response) {
                System.err.println(response);
                responses.add(response);
            }
        };
        try {
            protocol.subscribe(listener, new RequestObject("C0003", Attribute.STATUS, Attribute.MODE));
            protocol.subscribe(listener, new RequestObject("C0004", Attribute.STATUS, Attribute.ACT));

            Thread.sleep(10000);

        } finally {
            protocol.unsubscribe(listener);
        }
    }
}
