package org.openhab.binding.network.discovery;

import org.eclipse.smarthome.model.script.actions.Ping;
import org.openhab.binding.network.service.NetworkUtils;

class PingRunnable implements Runnable {
    final String ip;
    final NetworkDiscoveryService service;

    public PingRunnable(String ip, NetworkDiscoveryService service) {
        this.ip = ip;
        this.service = service;
        if (ip == null) {
            throw new RuntimeException("ip may not be null!");
        }
    }

    @Override
    public void run() {
        try {
            if (Ping.checkVitality(ip, 0, NetworkDiscoveryService.PING_TIMEOUT_IN_MS)) {
                service.newDevice(ip);
            } else if (NetworkUtils.nativePing(ip, 0, NetworkDiscoveryService.PING_TIMEOUT_IN_MS)) {
                service.newDevice(ip);
            }
        } catch (Exception e) {
        }
    }
}
