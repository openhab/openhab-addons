package org.openhab.binding.mox.config;

/**
 * Created by sja on 15.04.15.
 */
public class MoxGatewayConfig {

    public String udpHost;
    public Integer udpPort; // TODO Rename: listenUdpPort
    public Integer targetUdpPort = 6670; // TODO Make this configurable

}
