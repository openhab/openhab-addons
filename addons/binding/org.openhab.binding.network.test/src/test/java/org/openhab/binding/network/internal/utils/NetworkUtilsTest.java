/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.internal.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

/**
 * Tests cases for {@see NetworkUtils}
 *
 * @author David Graeff - Initial contribution
 */
public class NetworkUtilsTest {

    @Test
    public void testConvertMethods() throws UnknownHostException {
        InetAddress local = InetAddress.getByName("127.0.0.1");
        InetAddress a = NetworkUtils.int2InetAddress(2130706433);
        assertEquals(local, a);
        assertEquals(2130706433, NetworkUtils.inetAddress2Int(local));
    }

    @Test
    public void testInterfaceIPS() throws SocketException {
        // Overwrite the getNetworkInterfaces() method to return our mock instead
        NetworkUtils networkUtils = spy(new NetworkUtils());
        when(networkUtils.getInterfaceNames()).thenReturn(Collections.singleton("TESTinterface"));

        // Test interface addresses
        Set<String> expectedNames = Collections.singleton("TESTinterface");
        assertEquals(expectedNames, networkUtils.getInterfaceNames());
    }
}
