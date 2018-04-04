/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.discovery;

/**
 * Test app for discovering devices.
 *
 * @author Stefan Endrullis &lt;stefan@endrullis.de&gt;
 */
public class WiFiLEDDiscoveryServiceTestApp {

    public static void main(String[] args) {
        WiFiLEDDiscoveryService discoveryService = new WiFiLEDDiscoveryService();

        discoveryService.startScan();
    }

}
