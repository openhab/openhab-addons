package org.openhab.binding.mysensors.service;

import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;

public class DiscoveryThread implements MySensorsUpdateListener {
    private MySensorsBridgeConnection mysCon;
    private MySensorsDiscoveryService mysDiscoServ;

    public DiscoveryThread(MySensorsBridgeConnection mysCon, MySensorsDiscoveryService mysDiscoServ) {
        this.mysCon = mysCon;
        this.mysDiscoServ = mysDiscoServ;
    }

    public void start() {
        mysCon.addUpdateListener(this);
    }

    public void stop() {
        mysCon.removeUpdateListener(this);
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        mysDiscoServ.newDevicePresented(event.getData());

    }

    @Override
    public void disconnectEvent() {
        // TODO Auto-generated method stub

    }
}
