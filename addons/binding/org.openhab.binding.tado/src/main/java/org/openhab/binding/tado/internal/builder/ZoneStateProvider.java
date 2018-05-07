package org.openhab.binding.tado.internal.builder;

import java.io.IOException;

import org.openhab.binding.tado.handler.TadoZoneHandler;
import org.openhab.binding.tado.internal.api.TadoClientException;
import org.openhab.binding.tado.internal.api.model.ZoneState;

public class ZoneStateProvider {
    private TadoZoneHandler zoneHandler;
    private ZoneState zoneState;

    public ZoneStateProvider(TadoZoneHandler zoneHandler) {
        this.zoneHandler = zoneHandler;
    }

    ZoneState getZoneState() throws IOException, TadoClientException {
        if (this.zoneState == null) {
            ZoneState retrievedZoneState = zoneHandler.getZoneState();
            // empty zone state behaves like a NULL object
            this.zoneState = retrievedZoneState != null ? retrievedZoneState : new ZoneState();
        }

        return this.zoneState;
    }
}
