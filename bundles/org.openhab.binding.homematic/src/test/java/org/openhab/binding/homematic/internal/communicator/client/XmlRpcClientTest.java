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
package org.openhab.binding.homematic.internal.communicator.client;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.homematic.internal.common.HomematicConfig;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class XmlRpcClientTest {

    @Test
    public void testGetRpcCallbackUrlDoesNotContainsSpaces() {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HomematicConfig config = new HomematicConfig();
        config.setCallbackHost(" 192. 168.  178.10 ");
        config.setXmlCallbackPort(10);
        XmlRpcClient client = new XmlRpcClient(config, httpClient);
        assertEquals("http://192.168.178.10:10", client.getRpcCallbackUrl());
    }
}
