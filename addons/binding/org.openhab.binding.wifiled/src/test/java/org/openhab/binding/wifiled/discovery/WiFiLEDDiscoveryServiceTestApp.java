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
