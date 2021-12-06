package org.openhab.binding.boschspexor.internal.api.model;

public class ObservationStatus {
    public enum ObservationType {
        Burglary,
        Fire,
        CO,
        Narcotics
    }

    public enum SensorMode {
        Deactivated,
        InActivation,
        InCalibration,
        Activated,
        Triggered,
        InDeactivation
    }

    private ObservationType observationType;
    private SensorMode sensorMode;

    public ObservationType getObservationType() {
        return observationType;
    }

    public void setObservationType(ObservationType observationType) {
        this.observationType = observationType;
    }

    public SensorMode getSensorMode() {
        return sensorMode;
    }

    public void setSensorMode(SensorMode sensorMode) {
        this.sensorMode = sensorMode;
    }
}
