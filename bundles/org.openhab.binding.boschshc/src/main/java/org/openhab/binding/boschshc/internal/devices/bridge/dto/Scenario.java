package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

public class Scenario extends BoschSHCServiceState {

    public String name;
    public String id;
    public String lastTimeTriggered;
    public Scenario() {
        super("scenarioTriggered");
    }
}
