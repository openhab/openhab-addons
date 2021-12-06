package org.openhab.binding.boschspexor.internal.api.model;

import java.util.ArrayList;
import java.util.List;

public class Status {

    private Energy energy;
    private Connection connection;
    private Firmware firmware;
    private List<ObservationStatus> observation = new ArrayList<>();

    public Energy getEnergy() {
        return energy;
    }

    public void setEnergy(Energy energy) {
        this.energy = energy;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Firmware getFirmware() {
        return firmware;
    }

    public void setFirmware(Firmware firmware) {
        this.firmware = firmware;
    }

    public List<ObservationStatus> getObservation() {
        return observation;
    }

    public void setObservation(List<ObservationStatus> observation) {
        this.observation = observation;
    }
}
